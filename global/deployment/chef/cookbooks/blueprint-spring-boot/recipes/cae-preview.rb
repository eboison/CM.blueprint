include_recipe 'blueprint-spring-boot::_base'

service_name = 'cae-preview'
service_user = service_name
service_group = node['blueprint']['group']
service_dir = "#{node['blueprint']['base_dir']}/#{service_name}"

# use default_unless to allow configuration in recipes run prior to this one
node.default_unless['blueprint']['apps'][service_name]['application.properties']['repository.url'] = 'http://localhost:40180/ior'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['solr.url'] = 'http://localhost:40080/solr'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['solr.collection.cae'] = 'preview'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['elastic.solr.url'] = 'http://localhost:40080/solr'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['mongoDb.clientURI'] = 'mongodb://localhost:27017'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['mongoDb.prefix'] = 'blueprint'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['repository.heapCacheSize'] = 100 * 1024 * 1024
node.default_unless['blueprint']['apps'][service_name]['application.properties']['repository.blobCacheSize'] = 10 * 1024 * 1024 * 1024
node.default_unless['blueprint']['apps'][service_name]['application.properties']['repository.blobStreamingSizeThreshold'] = -1
node.default_unless['blueprint']['apps'][service_name]['application.properties']['repository.blobStreamingThreads'] = -1
node.default_unless['blueprint']['apps'][service_name]['application.properties']['repository.maxCachedBlobSize'] = -1
node.default_unless['blueprint']['apps'][service_name]['application.properties']['link.urlPrefixType'] = 'preview'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['themeImporter.themeDeveloperGroups'] = 'developer'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['spring.http.encoding.force'] = "true"
# make sure that the preview when used behind an apache using http proxying does use https
node.default_unless['blueprint']['apps'][service_name]['application.properties']['server.use-forward-headers'] = 'true'
node.default_unless['blueprint']['apps'][service_name]['application.properties']['repository.blobCachePath'] = "#{node['blueprint']['cache_dir']}/#{service_name}"
# The path where the transformed blobs should be saved persistently. If not set, then the feature is deactivated,
# and all transformed blobs are saved in memory
node.default_unless['blueprint']['apps'][service_name]['application.properties']['com.coremedia.transform.blobCache.basePath'] = "#{node['blueprint']['cache_dir']}/#{service_name}/persistent-transformed-blobcache"
node.default_unless['blueprint']['apps'][service_name]['application.properties']['server.port'] = 40980

application_config_hash = Mash.new
# legacy compatibility step. Here we merge the defaults from old node.json files
application_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(application_config_hash, node['blueprint']['webapps'][service_name]['application.properties']) if node.deep_fetch('blueprint', 'webapps', service_name, 'application.properties')
# and now the new ones
application_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(application_config_hash, node['blueprint']['apps'][service_name]['application.properties'])

blueprint_service_user service_user do
  home service_dir
  group service_group
  notifies :create, "ruby_block[restart_#{service_name}]", :immediately
end

directory node['blueprint']['apps'][service_name]['application.properties']['repository.blobCachePath'] do
  owner service_name
  group service_group
  recursive true
  notifies :create, "ruby_block[restart_#{service_name}]", :immediately
end

boot_opts_config_hash = Mash.new
boot_opts_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(boot_opts_config_hash, node['blueprint']['spring-boot']['boot_opts'])
boot_opts_config_hash = Chef::Mixin::DeepMerge.hash_only_merge!(boot_opts_config_hash, node['blueprint']['spring-boot'][service_name]['boot_opts']) if node.deep_fetch('blueprint', 'spring-boot', service_name, 'boot_opts')

# merge java opts
java_opts_hash = Mash.new
java_opts_hash = Chef::Mixin::DeepMerge.hash_only_merge!(java_opts_hash, node['blueprint']['spring-boot']['java_opts']) if node.deep_fetch('blueprint', 'spring-boot', 'java_opts')
java_opts_hash = Chef::Mixin::DeepMerge.hash_only_merge!(java_opts_hash, node['blueprint']['spring-boot'][service_name]['java_opts']) if service_name && node.deep_fetch('blueprint', 'spring-boot', service_name, 'java_opts')

if spring_boot_default(service_name, 'debug')
  java_opts_hash['debug'] = '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:40906'
end

spring_boot_application service_name do
  path service_dir
  maven_repository_url node['blueprint']['maven_repository_url']
  group_id node['blueprint']['apps'][service_name]['group_id']
  artifact_id node['blueprint']['apps'][service_name]['artifact_id']
  version node['blueprint']['apps'][service_name]['version']
  owner service_name
  group service_group
  java_opts "-Xmx#{node['blueprint']['spring-boot'][service_name]['heap']} #{java_opts_hash.values.join(' ')}"
  java_home spring_boot_default(service_name, 'java_home')
  boot_opts boot_opts_config_hash
  application_properties application_config_hash
  post_start_wait_url "http://localhost:40980/blueprint/servlet/actuator/health"
  log_dir "#{node['blueprint']['log_dir']}/#{service_name}"
  jmx_remote spring_boot_default(service_name, 'jmx_remote')
  jmx_remote_server_name spring_boot_default(service_name, 'jmx_remote_server_name')
  jmx_remote_registry_port 40999
  jmx_remote_server_port 40998
  jmx_remote_authenticate spring_boot_default(service_name, 'jmx_remote_authenticate')
  jmx_remote_control_user spring_boot_default(service_name, 'jmx_remote_control_user')
  jmx_remote_control_password spring_boot_default(service_name, 'jmx_remote_control_password')
  jmx_remote_monitor_user spring_boot_default(service_name, 'jmx_remote_monitor_user')
  jmx_remote_monitor_password spring_boot_default(service_name, 'jmx_remote_monitor_password')
  notifies :create, "ruby_block[restart_#{service_name}]", :immediately
end

service service_name do
  action spring_boot_default(service_name, 'start_service') ? [:enable, :start] : [:enable]
end

ruby_block "restart_#{service_name}" do
  block do
    if spring_boot_default(service_name, 'start_service')
      r = resources(:service => service_name)
      a = Array.new(r.action)

      a << :restart unless a.include?(:restart)
      a.delete(:start) if a.include?(:restart)

      r.action(a)
    end
  end
  action :nothing
end