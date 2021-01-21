package com.coremedia.blueprint.contenthub.adapters.youtube.connector.videos;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class VideoServiceConfiguration {
  @Bean
  public VideoServiceProvider videoServiceProvider() {
    return new VideoServiceProvider();
  }
}