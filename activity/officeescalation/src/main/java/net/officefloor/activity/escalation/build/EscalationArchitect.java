/*-
 * #%L
 * Escalation
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

package net.officefloor.activity.escalation.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeEscalation;

/**
 * Architect for configuring global escalation handling via composition.
 */
public interface EscalationArchitect {

    /**
     * Adds a single {@link OfficeEscalation} handler loaded from the given resource.
     *
     * @param escalationTypeName Fully-qualified name of the exception type being handled.
     * @param resourceLocation   Classpath location of the composition YAML file.
     * @param properties         {@link PropertyList} for configuration.
     * @return Added {@link OfficeEscalation}.
     * @throws Exception If fails to add the escalation.
     */
    OfficeEscalation addEscalation(String escalationTypeName, String resourceLocation,
                                   PropertyList properties) throws Exception;

    /**
     * Loads all escalation handlers from the given directory.
     * <p>
     * Each YAML file in the directory should be named after the fully-qualified exception class it handles
     * (e.g., {@code java.io.IOException.yml}).
     *
     * @param escalationDirectory Classpath directory containing the escalation composition YAML files.
     * @param properties          {@link PropertyList} for configuration.
     * @throws Exception If fails to add the escalations.
     */
    void addEscalations(String escalationDirectory, PropertyList properties) throws Exception;

}
