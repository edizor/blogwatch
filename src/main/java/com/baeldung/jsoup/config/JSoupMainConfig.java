package com.baeldung.jsoup.config;

import com.baeldung.common.config.CommonConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(CommonConfig.class)
@ComponentScan("com.baeldung.jsoup")
@PropertySource("classpath:common.properties")
public class JSoupMainConfig { }
