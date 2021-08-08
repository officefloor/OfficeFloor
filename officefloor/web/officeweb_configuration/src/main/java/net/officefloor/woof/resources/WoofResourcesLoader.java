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

package net.officefloor.woof.resources;

import net.officefloor.woof.model.resources.WoofResourcesModel;

/**
 * Loads the {@link WoofResourcesModel} configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofResourcesLoader {

	/**
	 * Loads the {@link WoofResourcesModel} configuration.
	 * 
	 * @param context
	 *            {@link WoofResourcesLoaderContext}.
	 * @throws Exception
	 *             If fails to load the configuration.
	 */
	void loadWoofResourcesConfiguration(WoofResourcesLoaderContext context) throws Exception;

}
