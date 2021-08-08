/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.model.office;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository of the {@link OfficeModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeRepository {

	/**
	 * Retrieves the {@link OfficeModel} from the {@link ConfigurationItem}.
	 * 
	 * @param office
	 *            {@link OfficeModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} containing the {@link OfficeModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link OfficeModel}.
	 */
	void retrieveOffice(OfficeModel office, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link OfficeModel} into the {@link ConfigurationItem}.
	 * 
	 * @param office
	 *            {@link OfficeModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the
	 *            {@link OfficeModel}.
	 * @throws Exception
	 *             If fails to store the {@link OfficeModel}.
	 */
	void storeOffice(OfficeModel office, WritableConfigurationItem configuration) throws Exception;

}
