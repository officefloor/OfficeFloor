/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.spring.starter.rest.cors.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfigurationSourceConfig implements WebMvcConfigurer {

    /**
     * Provides a CorsConfigurationSource bean scoped to the cors-config-source test paths.
     * OfficeFloorHandlerInterceptor picks this up directly for OfficeFloor-managed endpoints.
     * Marked @Primary so ObjectProvider.getIfAvailable() returns this bean.
     */
    @Primary
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("https://example.com");
        config.addAllowedMethod("GET");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/*/cors/cors-config-source/**", config);
        return source;
    }

    /**
     * Second CorsConfigurationSource bean. Spring Security 6 auto-detects a CorsConfigurationSource
     * bean and adds a CorsFilter only when exactly ONE such bean exists. With two beans it falls back
     * to HandlerMappingIntrospectorCorsConfigurationSource, which correctly reads @CrossOrigin
     * annotations for NativeSpring endpoints. Without this, Spring Security's CorsFilter would reject
     * OPTIONS preflights on non-covered paths with 403.
     */
    @Bean
    public CorsConfigurationSource noOpCorsConfigurationSource() {
        return request -> null;
    }

    /**
     * Mirrors the CorsConfigurationSource config via WebMvcConfigurer so that NativeSpring
     * handler mappings (which do not read the bean directly) apply the same CORS policy.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/*/cors/cors-config-source/**")
                .allowedOrigins("https://example.com");
    }

}
