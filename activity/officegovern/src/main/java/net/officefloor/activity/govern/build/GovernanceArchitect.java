/*-
 * #%L
 * Governance
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

package net.officefloor.activity.govern.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeGovernance;

import java.util.Map;

/** Architect for configuring governance. */
public interface GovernanceArchitect {

    /**
     * Adds a specific {@link OfficeGovernance}.
     *
     * @param governanceName     Name of the {@link OfficeGovernance}.
     * @param governanceLocation Location of {@link OfficeGovernance} configuration.
     * @param properties         {@link PropertyList} for configuration.
     * @return {@link OfficeGovernance}.
     * @throws Exception If fails to create {@link OfficeGovernance}.
     */
    OfficeGovernance addGovernance(String governanceName, String governanceLocation,
                                   PropertyList properties) throws Exception;

    /**
     * Adds the {@link OfficeGovernance} instances configured in a directory.
     *
     * @param governanceDirectory Location of the directory containing the {@link OfficeGovernance} configurations.
     * @param properties          {@link PropertyList} for configuration.
     * @return {@link Map} of {@link OfficeGovernance} instances by their name.
     * @throws Exception If fails to create the {@link OfficeGovernance} instances.
     */
    Map<String, OfficeGovernance> addGovernances(String governanceDirectory,
                                                 PropertyList properties) throws Exception;

}
