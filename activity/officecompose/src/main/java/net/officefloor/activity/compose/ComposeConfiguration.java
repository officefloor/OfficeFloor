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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/** Compose configuration. */
@Data
public class ComposeConfiguration {

    @JsonIgnore
    private String start;

    private CompositionConfiguration composition;

    @JsonIgnore
    private Map<String, FunctionConfiguration> functions = new HashMap<>();

    /**
     * Sets the function configuration.
     *
     * @param functionName   Name of the function.
     * @param functionConfig {@link FunctionConfiguration}.
     */
    @JsonAnySetter
    public void setFunction(String functionName, FunctionConfiguration functionConfig) {

        // Capture the first function as starting function
        if (this.start == null) {
            this.start = functionName;
        }

        // Include the function
        this.functions.put(functionName, functionConfig);
    }
}
