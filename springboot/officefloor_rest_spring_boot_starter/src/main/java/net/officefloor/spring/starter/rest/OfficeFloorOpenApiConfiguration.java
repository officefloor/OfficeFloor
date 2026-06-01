package net.officefloor.spring.starter.rest;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Auto-configuration for OfficeFloor OpenAPI integration. */
@AutoConfiguration
public class OfficeFloorOpenApiConfiguration {

    /** Optional OpenAPI configuration loaded only when springdoc is on the classpath. */
    @Configuration
    @ConditionalOnClass(name = "org.springdoc.core.customizers.OpenApiCustomizer")
    public static class OptionalOpenApiConfiguration {

        /**
         * Creates the {@link OpenApiCustomizer} for OfficeFloor.
         *
         * @param starter {@link OfficeFloorRestSpringBootStarter}.
         * @return {@link OpenApiCustomizer}.
         * @throws Exception If fails to create the customizer.
         */
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
