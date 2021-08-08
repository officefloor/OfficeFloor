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

package net.officefloor.woof.model.resources;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository for obtaining the {@link WoofResourcesModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofResourcesRepository {

	/**
	 * Retrieves the {@link WoofResourcesModel} from the
	 * {@link ConfigurationItem}.
	 * 
	 * @param resources
	 *            {@link WoofResourcesModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to retrieve the {@link WoofResourcesModel}.
	 */
	void retrieveWoofResources(WoofResourcesModel resources, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link WoofResourcesModel} within the
	 * {@link WritableConfigurationItem}.
	 * 
	 * @param resources
	 *            {@link WoofResourcesModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link WoofResourcesModel}.
	 */
	void storeWoofResources(WoofResourcesModel resources, WritableConfigurationItem configuration) throws Exception;

}
