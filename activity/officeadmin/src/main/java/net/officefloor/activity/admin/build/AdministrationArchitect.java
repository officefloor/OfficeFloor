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

package net.officefloor.activity.admin.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeGovernance;

import java.util.Map;

/** Architect for configuring administration. */
public interface AdministrationArchitect {

    /**
     * Adds {@link OfficeGovernance} for the {@link OfficeAdministration}
     *
     * @param governanceName Name to link the {@link OfficeGovernance}.
     * @param goverance      {@link OfficeGovernance}.
     */
    void addGovernance(String governanceName, OfficeGovernance goverance);

    /**
     * Adds a specific {@link OfficeAdministration}.
     *
     * @param administrationName     Name of the {@link OfficeAdministration}.
     * @param administrationLocation Location of {@link OfficeAdministration} configuration.
     * @param properties             {@link PropertyList} for configuration.
     * @return {@link OfficeAdministration}.
     * @throws Exception If fails to create {@link OfficeAdministration}.
     */
    OfficeAdministration addAdministration(String administrationName, String administrationLocation,
                                           PropertyList properties) throws Exception;

    /**
     * Adds the {@link OfficeAdministration} instances configured in a directory.
     *
     * @param administrationDirectory Location of the directory containing the {@link OfficeAdministration} configurations.
     * @param properties              {@link PropertyList} for configuration.
     * @return {@link Map} of {@link OfficeAdministration} instances by their name.
     * @throws Exception If fails to create the {@link OfficeAdministration} instances.
     */
    Map<String, OfficeAdministration> addAdministrations(String administrationDirectory,
                                                         PropertyList properties) throws Exception;

}
