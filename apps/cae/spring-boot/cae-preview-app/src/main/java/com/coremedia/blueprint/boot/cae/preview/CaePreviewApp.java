package com.coremedia.blueprint.boot.cae.preview;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * we need to exclude some autoconfigurations:
 * - WMAC to disable the handler listening on /**
 * - FMAC because otherwise we have conflicting FreemarkerConfiguration beans
 *  for CAE and studio for server also exclude JDBC
 */
@EnableWebSecurity
@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class, FreeMarkerAutoConfiguration.class})
public class CaePreviewApp {

  // ... Bean definitions
  public static void main(String[] args) {
    SpringApplication.run(CaePreviewApp.class, args);
  }

  /*
   * Needed for 'redirect:*' view resolving. Usually created in WebMvcAutoConfiguration
   */
  @Bean
  public InternalResourceViewResolver defaultViewResolver() {
    return new InternalResourceViewResolver();
  }

  /*
   * Configure index.html when running embedded tomcat
   *
   * we need to do this as a workaround because we cannot use WebMvcAutoConfiguration
   */
  @Bean
  WebServerFactoryCustomizer<TomcatServletWebServerFactory> indexHtmlConfigurer() {
    return container -> container.addContextCustomizers(context -> context.addWelcomeFile("index.html"));
  }

  /**
   * Workaround to allow CORS also for fonts
   */
  @Bean
  public BeanPostProcessor corsCustomizer(@Value("${livecontext.crossdomain.whitelist}") String[] crossDomainWhiteList, @Value("${cae.crossdomain.whitelist}") String[] caeCrossDomainWhiteList) {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if ("requestMappingHandlerMapping".equals(beanName) && bean instanceof RequestMappingHandlerMapping) {
          CorsConfiguration corsConfiguration = new CorsConfiguration();
          List<String> allowedOrigins = new ArrayList<String>();
          allowedOrigins.addAll(Arrays.asList(crossDomainWhiteList));
          allowedOrigins.addAll(Arrays.asList(caeCrossDomainWhiteList));
          corsConfiguration.setAllowedOrigins(allowedOrigins);
          // to get the ratings via /dynamic
          corsConfiguration.setAllowedHeaders(Arrays.asList("x-requested-with","X-CSRF-Token"));
          // to create ratings
          corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST"));
          corsConfiguration.setAllowCredentials(true);

          ((RequestMappingHandlerMapping) bean).setCorsConfigurations(
                  Collections.singletonMap("{path:.*}", corsConfiguration));
        }

        return bean;
      }

      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
      }
    };
  }

}