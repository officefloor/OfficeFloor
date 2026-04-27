package net.officefloor.spring.starter.rest;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
public class OfficeFloorOpenApiConfiguration {

    @Configuration
    @ConditionalOnClass(name = "org.springdoc.core.customizers.OpenApiCustomizer")
    public static class OptionalOpenApiConfiguration {

        @Bean
        public OpenApiCustomizer officeFloorOpenApiCustomizer(
                OfficeFloorRestSpringBootStarter starter) throws Exception {

            // Ensure started
            starter.startOfficeFloor();

            // Load the paths
            return (openApi) -> {
                starter.getOpenApi().getPaths().forEach(openApi::path);
            };
        }
    }
}
