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

package net.officefloor.model.repository;

import java.io.IOException;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.Model;

/**
 * Repository to the {@link Model} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ModelRepository {

	/**
	 * Configures the {@link Model} from the {@link ConfigurationItem}.
	 * 
	 * @param model
	 *            {@link Model} to be configured.
	 * @param configuration
	 *            {@link ConfigurationItem} containing configuration of the
	 *            {@link Model}.
	 * @throws IOException
	 *             If fails to configure the {@link Model}.
	 */
	void retrieve(Object model, ConfigurationItem configuration) throws IOException;

	/**
	 * Stores the {@link Model} within the {@link WritableConfigurationItem}.
	 * 
	 * @param model
	 *            {@link Model} to be stored.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the {@link Model}.
	 * @throws IOException
	 *             If fails to store the {@link Model}.
	 */
	void store(Object model, WritableConfigurationItem configuration) throws IOException;

}
