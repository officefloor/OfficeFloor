/*-
 * #%L
 * Team
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

package net.officefloor.activity.team.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;

import java.util.Map;

/**
 * Deployer to configure {@link OfficeFloorTeam} instances.
 */
public interface TeamDeployer {

    /**
     * Adds a single {@link OfficeFloorTeam}.
     *
     * @param teamName     Name of the team.
     * @param teamLocation Classpath resource path to the configuration.
     * @param properties   {@link PropertyList} for interpolation.
     * @return Configured {@link OfficeFloorTeam}.
     * @throws Exception If fails to load.
     */
    OfficeFloorTeam addTeam(String teamName, String teamLocation, PropertyList properties) throws Exception;

    /**
     * Adds all {@link OfficeFloorTeam} instances from a directory.
     *
     * @param teamsDirectory Classpath directory path to scan for configuration files.
     * @param properties     {@link PropertyList} for interpolation.
     * @return Map of team name to {@link OfficeFloorTeam}.
     * @throws Exception If fails to load.
     */
    Map<String, OfficeFloorTeam> addTeams(String teamsDirectory, PropertyList properties) throws Exception;

}
