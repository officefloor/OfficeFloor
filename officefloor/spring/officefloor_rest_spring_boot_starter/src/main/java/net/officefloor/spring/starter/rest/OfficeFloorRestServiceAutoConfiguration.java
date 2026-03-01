package net.officefloor.spring.starter.rest;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(OfficeFloorRestServiceProperties.class)
@ConditionalOnProperty(prefix = "officefloor.rest", name="enabled", havingValue = "true", matchIfMissing = true)
public class OfficeFloorRestServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OfficeFloorRestService officeFloorRestService() {
        return new OfficeFloorRestService();
    }
}
