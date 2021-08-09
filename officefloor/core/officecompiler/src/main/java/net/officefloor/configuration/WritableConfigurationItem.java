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

import net.officefloor.compile.properties.PropertyList;

/**
 * Writable {@link ConfigurationItem}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WritableConfigurationItem extends ConfigurationItem {

	/**
	 * Obtains the {@link InputStream} to the raw configuration (no
	 * {@link PropertyList} replacement).
	 * 
	 * @return {@link InputStream} to the raw configuration.
	 * @throws IOException
	 *             If fails to load the raw configuration.
	 */
	InputStream getRawConfiguration() throws IOException;

	/**
	 * Specifies the configuration that this is to represent.
	 * 
	 * @param configuration
	 *            Configuration.
	 * @throws IOException
	 *             If fails to set the configuration.
	 */
	void setConfiguration(InputStream configuration) throws IOException;

	/**
	 * Obtains the {@link WritableConfigurationContext} for this
	 * {@link WritableConfigurationItem}.
	 * 
	 * @return {@link WritableConfigurationContext} for this
	 *         {@link WritableConfigurationItem}.
	 */
	WritableConfigurationContext getContext();

}
