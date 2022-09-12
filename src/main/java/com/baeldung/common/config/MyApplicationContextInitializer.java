package com.baeldung.common.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.baeldung.common.GlobalConstants;

public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public MyApplicationContextInitializer() {
        super();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        final ConfigurableEnvironment environment = applicationContext.getEnvironment();
        // we merge our pre-initialized environment into Spring provided one
        environment.merge(SpringPropertiesReader.getEnvironment());
        final String baseURL = environment.getProperty(GlobalConstants.ENV_PROPERTY_BASE_URL);
        final String targetEnv = environment.getProperty(GlobalConstants.ENV_PROPERTY_TARGET_ENV);
        final String headlessBrowserName = environment.getProperty(GlobalConstants.ENV_PROPERTY_TARGET_ENV);

        if (StringUtils.isBlank(baseURL)) {
            System.setProperty(GlobalConstants.ENV_PROPERTY_BASE_URL, GlobalConstants.BAELDUNG_HOME_PAGE_URL);
        }
        if (StringUtils.isBlank(targetEnv)) {
            System.setProperty(GlobalConstants.ENV_PROPERTY_TARGET_ENV, GlobalConstants.TARGET_ENV_WINDOWS);
        }

        if (StringUtils.isBlank(headlessBrowserName)) {
            System.setProperty(GlobalConstants.ENV_PROPERTY_HEADLESS_BROWSER_NAME, GlobalConstants.TARGET_ENV_DEFAULT_HEADLESS_BROWSER);
        }
    }
}
