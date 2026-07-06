/*-
 * #%L
 * Composition
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

package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** Function configuration. */
@Data
public class FunctionConfiguration {

    /*
     * ======== Procedure =============
     */

    @JsonProperty("class")
    private String className;

    private String method;

    private String next;

    /*
     * ======== Custom ProcedureSource ==========
     */

    private String resource;

    private String procedure;

    /*
     * ======== SectionSource ==========
     */

    private String source;

    private String location;

    private String input;

    private Map<String, String> properties;

    /*
     * ========= Govern ===========
     */

    private List<String> govern;

    /*
     * ========= Composition ===========
     */

    private Map<String, String> outputs;

    private Map<String, String> escalations;
}
