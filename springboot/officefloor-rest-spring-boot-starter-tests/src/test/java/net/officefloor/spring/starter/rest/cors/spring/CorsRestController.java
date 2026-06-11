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

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spring/cors")
public class CorsRestController {

    @GetMapping("/origins")
    @CrossOrigin(origins = "https://example.com")
    public String origins() {
        return "CORS";
    }

    @GetMapping("/origin-pattern")
    @CrossOrigin(originPatterns = "https://*.example.com")
    public String originPattern() {
        return "CORS";
    }

    @DeleteMapping("/allowed-methods")
    @CrossOrigin(origins = "https://example.com")
    public String allowedMethods() {
        return "CORS";
    }

    @GetMapping("/allowed-headers")
    @CrossOrigin(origins = "https://example.com", allowedHeaders = "X-Custom-Header")
    public String allowedHeaders() {
        return "CORS";
    }

    @GetMapping("/exposed-headers")
    @CrossOrigin(origins = "https://example.com", exposedHeaders = "X-Custom-Header")
    public String exposedHeaders() {
        return "CORS";
    }

    @GetMapping("/allow-credentials")
    @CrossOrigin(origins = "https://example.com", allowCredentials = "true")
    public String allowCredentials() {
        return "CORS";
    }

    @GetMapping("/max-age")
    @CrossOrigin(origins = "https://example.com", maxAge = 3600)
    public String maxAge() {
        return "CORS";
    }

    @GetMapping("/mvc-configurer/origin")
    public String mvcConfigurerOrigin() {
        return "CORS";
    }

    @GetMapping("/cors-config-source/origin")
    public String corsConfigSourceOrigin() {
        return "CORS";
    }

}
