package com.qbaaa.secure.auth.config;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class MailConfig {

  @Bean
  public Configuration freemarkerClassLoaderConfig() {
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_34);
    TemplateLoader templateLoader = new ClassTemplateLoader(this.getClass(), "/templates/mails");
    configuration.setTemplateLoader(templateLoader);
    return configuration;
  }
}
