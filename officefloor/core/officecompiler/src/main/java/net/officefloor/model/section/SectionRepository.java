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

package net.officefloor.model.section;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository of {@link SectionModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionRepository {

	/**
	 * Retrieves the {@link SectionModel} from the {@link ConfigurationItem}.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} containing the {@link SectionModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link SectionModel}.
	 */
	void retrieveSection(SectionModel section, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link SectionModel} into the {@link ConfigurationItem}.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the
	 *            {@link SectionModel}.
	 * @throws Exception
	 *             If fails to store the {@link SectionModel}.
	 */
	void storeSection(SectionModel section, WritableConfigurationItem configuration) throws Exception;

}
