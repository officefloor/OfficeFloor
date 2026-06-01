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

package net.officefloor.spring.starter.rest.cors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/** CORS configuration for composition. */
@Data
public class ComposeCorsConfiguration {

    @JsonProperty("allowed-origins")
    private List<String> allowedOrigins;

    @JsonProperty("allowed-origin-patterns")
    private List<String> allowedOriginPatterns;

    @JsonProperty("allowed-methods")
    private List<String> allowedMethods;

    @JsonProperty("allowed-headers")
    private List<String> allowedHeaders;

    @JsonProperty("exposed-headers")
    private List<String> exposedHeaders;

    @JsonProperty("allow-credentials")
    private Boolean allowCredentials;

    @JsonProperty("allow-private-network")
    private Boolean allowPrivateNetwork;

    @JsonProperty("max-age")
    private Long maxAge;

}
