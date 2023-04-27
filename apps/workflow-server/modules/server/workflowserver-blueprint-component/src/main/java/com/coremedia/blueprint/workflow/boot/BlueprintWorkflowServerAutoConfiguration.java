package com.coremedia.blueprint.workflow.boot;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import com.coremedia.translate.TranslatablePredicate;
import com.coremedia.translate.workflow.AllMergeablePropertiesPredicateFactory;
import com.coremedia.translate.workflow.AutoMergePredicateFactory;
import com.coremedia.translate.workflow.CleanInTranslation;
import com.coremedia.translate.workflow.WorkflowAutoMergeConfigurationProperties;
import com.coremedia.translate.workflow.synchronization.CopyOver;
import com.coremedia.translate.workflow.DefaultAutoMergePredicateFactory;
import com.coremedia.translate.workflow.DefaultAutoMergeStructListMapKey;
import com.coremedia.translate.workflow.DefaultAutoMergeStructListMapKeyFactory;
import com.coremedia.translate.workflow.DefaultTranslationWorkflowDerivedContentsStrategy;
import com.coremedia.translate.workflow.TranslationWorkflowDerivedContentsStrategy;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration class to be loaded when no customer spring context manager is configured.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WorkflowAutoMergeConfigurationProperties.class)
@Import({
        WorkflowServerElasticProcessArchiveConfiguration.class,
        WorkflowServerMemoryProcessArchiveConfiguration.class,
})
@ImportResource(value = {
        "classpath:/com/coremedia/blueprint/base/multisite/bpbase-sitemodel.xml",
        "classpath:com/coremedia/cap/multisite/multisite-services.xml",
        "classpath:/com/coremedia/blueprint/common/multisite/translation-config.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
@PropertySource("classpath:/com/coremedia/blueprint/base/multisite/bpbase-sitemodel-defaults.properties")
class BlueprintWorkflowServerAutoConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(BlueprintWorkflowServerAutoConfiguration.class);

  @PostConstruct
  void initialize() {
    LOG.info("Configuring blueprint workflow server component.");
  }

  /**
   * A strategy for extracting derived contents from the default translation.xml workflow definition.
   * You can alter this definition and/or add additional strategy beans when using a modified translation workflow.
   */
  @Bean
  DefaultTranslationWorkflowDerivedContentsStrategy defaultTranslationWorkflowDerivedContentsStrategy() {
    DefaultTranslationWorkflowDerivedContentsStrategy strategy = new DefaultTranslationWorkflowDerivedContentsStrategy();
    strategy.setProcessDefinitionName("Translation");
    strategy.setDerivedContentsVariable("derivedContents");
    strategy.setMasterContentObjectsVariable("masterContentObjects");
    return strategy;
  }

  /**
   * Returns an {@link AutoMergePredicateFactory} that decides which properties of a master version are automatically
   * merged into a derived content. The returned factory uses the {@link DefaultAutoMergePredicateFactory} under the
   * hood, and additionally excludes the root channel's segment property from auto-merge.
   *
   * <p>This bean is referenced by name and used by the default for translation workflows.
   * The bean name is the default value of
   * {@link com.coremedia.translate.workflow.AutoMergeTranslationAction#setAutoMergePredicateFactoryName(String)}.
   * Translation workflows can specify a different bean in translation workflows with attribute
   * {@code autoMergePredicateFactory} of action {@code AutoMergeTranslationAction}.
   *
   * @param sitesService the {@link SitesService}
   * @return AutoMergePredicateFactory
   */
  @Bean
  AutoMergePredicateFactory defaultAutoMergePredicateFactory(@NonNull TranslatablePredicate translatablePredicate,
                                                             @NonNull WorkflowAutoMergeConfigurationProperties autoMerge,
                                                             @NonNull SitesService sitesService) {
    DefaultAutoMergePredicateFactory factory = autoMerge.isTranslatable()
            ? new DefaultAutoMergePredicateFactory(true)
            : new DefaultAutoMergePredicateFactory(translatablePredicate);
    return factory
            .and(new ExcludeRootChannelSegmentAutoMergePredicateFactory(sitesService));
  }

  /**
   * Returns an {@link AutoMergePredicateFactory} that decides which properties of a master version are automatically
   * merged into a derived content. This factory is used by default for synchronization workflows, and enables
   * auto-merge for all properties, except the root channel's segment property and properties declared with attribute
   * {@code extensions:automerge="false"} in the content type definition.
   *
   * <p>Note, that this bean is referenced by name in the default workflow definition for synchronization workflows,
   * in file 'synchronization.xml' as attribute value
   * {@link com.coremedia.translate.workflow.synchronization.AutoMergeSyncAction#setAutoMergePredicateFactoryName(String) autoMergePredicateFactoryName}
   * of the {@link com.coremedia.translate.workflow.synchronization.AutoMergeSyncAction AutoMergeSyncAction} action.
   *
   * @param sitesService the {@link SitesService}
   * @return AutoMergePredicateFactory
   */
  @Bean
  AutoMergePredicateFactory allMergeablePropertiesPredicateFactory(@NonNull SitesService sitesService) {
    return new AllMergeablePropertiesPredicateFactory()
            .and(new ExcludeRootChannelSegmentAutoMergePredicateFactory(sitesService));
  }

  /**
   * A factory that returns keys for struct list items, that are used to find corresponding items when merging
   * struct list changes in the {@link com.coremedia.translate.workflow.AutoMergeTranslationAction}.
   *
   * <p>This factory is used by default, if no other bean name is configured in the workflow definition with
   * {@link com.coremedia.translate.workflow.AutoMergeTranslationAction#setAutoMergeStructListMapKeyFactoryName(String)}
   *
   * @param keys auto-wired {@link DefaultAutoMergeStructListMapKey}-s which configure keys for struct lists
   */
  @Bean
  DefaultAutoMergeStructListMapKeyFactory defaultAutoMergeStructListMapKeyFactory(
          Collection<? extends Collection<DefaultAutoMergeStructListMapKey>> keys) {
    List<DefaultAutoMergeStructListMapKey> flattenedKeys = keys.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    return new DefaultAutoMergeStructListMapKeyFactory(flattenedKeys);
  }

  /**
   * This bean just collects separate {@link DefaultAutoMergeStructListMapKey} beans and returns them as one list, so
   * that they're injected into {@link #defaultAutoMergeStructListMapKeyFactory}.
   */
  @Bean
  List<DefaultAutoMergeStructListMapKey> additionalDefaultAutoMergeStructListKeys(ObjectProvider<DefaultAutoMergeStructListMapKey> keys) {
    return keys.stream().collect(Collectors.toList());
  }

  @Bean
  List<DefaultAutoMergeStructListMapKey> defaultAutoMergeStructListKeys() {
    return List.of(
            new DefaultAutoMergeStructListMapKey("CMNavigation", "placement.placements", "section"),
            new DefaultAutoMergeStructListMapKey("CMNavigation", "placement.placements.extendedItems", "target"),
            new DefaultAutoMergeStructListMapKey("CMExternalProduct", "pdpPagegrid.placements", "section"),
            new DefaultAutoMergeStructListMapKey("CMExternalProduct", "pdpPagegrid.placements.extendedItems", "target"),
            new DefaultAutoMergeStructListMapKey("CMAbstractCategory", "pdpPagegrid.placements", "section"),
            new DefaultAutoMergeStructListMapKey("CMAbstractCategory", "pdpPagegrid.placements.extendedItems", "target"),
            new DefaultAutoMergeStructListMapKey("CMCollection", "extendedItems.links", "target"),
            new DefaultAutoMergeStructListMapKey("CMTeaser", "targets.links", "target")

    );
  }

  @Bean
  CleanInTranslation cleanInTranslation(List<TranslationWorkflowDerivedContentsStrategy> strategies,
                                        ContentRepository contentRepository,
                                        SitesService sitesService) {
    return new CleanInTranslation(strategies, contentRepository, sitesService);
  }

  @Bean
  CopyOver copyOver() {
    return new CopyOver();
  }

  @Configuration(proxyBeanMethods = false)
  @EnableScheduling
  static class BlueprintWorkflowServerSchedulingConfiguration {
    private final CleanInTranslation cleanInTranslation;

    BlueprintWorkflowServerSchedulingConfiguration(CleanInTranslation cleanInTranslation) {
      this.cleanInTranslation = cleanInTranslation;
    }

    /**
     * Regularly clean up "in translation" states left over by aborted workflows.
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 5_000)
    void doCleanInTranslation() {
      cleanInTranslation.run();
    }

  }

}
