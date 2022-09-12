package com.baeldung.common.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.support.ResourcePropertySource;

import com.baeldung.common.GlobalConstants;

/**
 * Helper class to resolve properties based on Spring profiles out of Spring context.
 */
public class SpringPropertiesReader {

    private static final Logger logger = LoggerFactory.getLogger(SpringPropertiesReader.class);

    private static final String[] activeProfiles = Arrays.stream(
        System.getProperty(GlobalConstants.ENV_PROPERTY_SPRING_PROFILE, String.join(",", GlobalConstants.DEFAULT_SPRING_PROFILE))
            .split(",")
    ).map(String::trim).toArray(String[]::new);

    private static final MutablePropertySources propertySources = new MutablePropertySources();
    private static final PropertySourcesPropertyResolver propertyResolver = new PropertySourcesPropertyResolver(propertySources);

    static {
        logger.info("Spring Active Profiles: {}", Arrays.toString(activeProfiles));
        for (String profile : activeProfiles) {
            try {
                propertySources.addFirst(new ResourcePropertySource("classpath:%s.properties".formatted(profile)));
            } catch (FileNotFoundException e) {
                // ignore resource not found
            } catch (IOException e) {
                logger.error("Cannot load property resource", e);
            }
        }
    }

    public static String[] getActiveProfiles() {
        return activeProfiles;
    }

    public static String get(String key) {
        return propertyResolver.getProperty(key);
    }

}
