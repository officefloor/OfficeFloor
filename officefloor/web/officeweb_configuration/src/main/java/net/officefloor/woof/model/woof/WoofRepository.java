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

package net.officefloor.woof.model.woof;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository for obtaining the WoOF (Web on OfficeFloor) model.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofRepository {

	/**
	 * Retrieves the {@link WoofModel} from the {@link ConfigurationItem}.
	 * 
	 * @param woof
	 *            {@link WoofModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to retrieve the {@link WoofModel}.
	 */
	void retrieveWoof(WoofModel woof, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link WoofModel} within the
	 * {@link WritableConfigurationItem}.
	 * 
	 * @param woof
	 *            {@link WoofModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link WoofModel}.
	 */
	void storeWoof(WoofModel woof, WritableConfigurationItem configuration) throws Exception;

}
