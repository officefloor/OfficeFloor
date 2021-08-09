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

package net.officefloor.woof.teams;

import net.officefloor.woof.model.teams.WoofTeamsModel;

/**
 * Loads the {@link WoofTeamsModel} configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTeamsLoader {

	/**
	 * Loads the {@link WoofTeamsModel} configuration.
	 * 
	 * @param context {@link WoofTeamsLoaderContext}.
	 * @throws Exception If fails to load the configuration.
	 */
	void loadWoofTeamsConfiguration(WoofTeamsLoaderContext context) throws Exception;

	/**
	 * Loads the use of the {@link WoofTeamsModel} configuration.
	 * 
	 * @param context {@link WoofTeamsUsageContext}.
	 * @throws Exception If fails to load the usage.
	 */
	void loadWoofTeamsUsage(WoofTeamsUsageContext context) throws Exception;

}
