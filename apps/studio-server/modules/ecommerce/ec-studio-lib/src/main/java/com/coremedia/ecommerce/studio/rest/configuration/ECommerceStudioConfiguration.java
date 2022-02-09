package com.coremedia.ecommerce.studio.rest.configuration;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.BaseCommerceServicesConfiguration;
import com.coremedia.ecommerce.studio.preview.CommerceHeadlessPreviewProvider;
import com.coremedia.ecommerce.studio.rest.filter.EcStudioFilters;
import com.coremedia.rest.cap.CapRestServiceConfiguration;
import com.coremedia.service.previewurl.PreviewProvider;
import com.coremedia.service.previewurl.impl.PreviewUrlServiceConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        EcStudioFilters.class,
        CapRestServiceConfiguration.class,
        BaseCommerceServicesConfiguration.class
})
@EnableConfigurationProperties({
        ECommerceStudioConfigurationProperties.class,
})
@ComponentScan(basePackages = "com.coremedia.ecommerce.studio.rest")
public class ECommerceStudioConfiguration {

  @Bean
  public PreviewProvider commerceHeadlessPreviewProvider(PreviewUrlServiceConfigurationProperties previewUrlServiceConfigurationProperties) {
    return new CommerceHeadlessPreviewProvider(previewUrlServiceConfigurationProperties);
  }

}
