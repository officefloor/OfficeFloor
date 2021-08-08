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

package net.officefloor.configuration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Writable {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WritableConfigurationContext extends ConfigurationContext {

	/**
	 * Obtains the {@link WritableConfigurationItem} at the location.
	 * 
	 * @param location
	 *            Location of the {@link WritableConfigurationItem} to obtain.
	 * @return {@link WritableConfigurationItem}.
	 * @throws IOException
	 *             If can not obtain a {@link WritableConfigurationItem} at the
	 *             location.
	 */
	WritableConfigurationItem getWritableConfigurationItem(String location) throws IOException;

	/**
	 * Creates a new {@link WritableConfigurationItem} at the relative location.
	 * 
	 * @param location
	 *            Location of the {@link WritableConfigurationItem} to create.
	 * @param configuration
	 *            Configuration for the {@link WritableConfigurationItem}.
	 * @return The created {@link WritableConfigurationItem}.
	 * @throws IOException
	 *             If fails to create the {@link WritableConfigurationItem}.
	 */
	WritableConfigurationItem createConfigurationItem(String location, InputStream configuration) throws IOException;

	/**
	 * Deletes the {@link WritableConfigurationItem} at the relative location.
	 * 
	 * @param location
	 *            Location of the {@link WritableConfigurationItem} to delete.
	 * @throws IOException
	 *             If can not delete the {@link WritableConfigurationItem} at
	 *             the relative location.
	 */
	void deleteConfigurationItem(String location) throws IOException;

}
