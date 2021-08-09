/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.woof.model.teams;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository for obtaining the {@link WoofTeamsModel} for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTeamsRepository {

	/**
	 * Retrieves the {@link WoofTeamsModel} from the {@link ConfigurationItem}.
	 * 
	 * @param teams
	 *            {@link WoofTeamsModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to retrieve the {@link WoofTeamsModel}.
	 */
	void retrieveWoofTeams(WoofTeamsModel teams, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link WoofTeamsModel} within the
	 * {@link WritableConfigurationItem}.
	 * 
	 * @param teams
	 *            {@link WoofTeamsModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link WoofTeamsModel}.
	 */
	void storeWoofTeams(WoofTeamsModel teams, WritableConfigurationItem configuration) throws Exception;

}
