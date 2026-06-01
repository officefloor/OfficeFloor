/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.build.rest;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.Map;

/** HTTP security configuration. */
@Data
public class HttpSecurityConfiguration {

    /**
     * Provides the access configuration for a particular {@link net.officefloor.web.spi.security.HttpSecurity}.
     */
    @JsonAnySetter
    private Map<String, HttpAccessConfiguration> accesses;

}
