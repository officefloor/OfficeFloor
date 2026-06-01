/*-
 * #%L
 * Administration
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

package net.officefloor.activity.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Configuration for the {@link net.officefloor.compile.spi.office.OfficeAdministration}.
 */
@Data
public class AdministrationConfiguration {

    /*
     * ========== Class Administration ===========
     */

    @JsonProperty("class")
    private String className;

    /*
     * ========== Administration Source ==========
     */

    private String source;

    private Map<String, String> properties;

    /*
     * ========== Governance ===========
     */

    private Map<String, String> governance;

    /*
     * ========== Composition ===========
     */

    private Map<String, String> outputs;

    private Map<String, String> escalations;

}
