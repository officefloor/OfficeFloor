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

package net.officefloor.configuration.impl.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.AbstractWritableConfigurationContext;

/**
 * {@link ConfigurationContext} that stores content in memory only (not
 * persisting it).
 * 
 * @author Daniel Sagenschneider
 */
public class MemoryConfigurationContext extends AbstractWritableConfigurationContext {

	/**
	 * Convenience method to create a {@link WritableConfigurationItem}.
	 * 
	 * @param location
	 *            Location.
	 * @return {@link WritableConfigurationItem}.
	 * @throws IOException
	 *             If fails to create {@link WritableConfigurationItem}.
	 */
	public static WritableConfigurationItem createWritableConfigurationItem(String location) throws IOException {
		return new MemoryConfigurationContext().createConfigurationItem("location",
				new ByteArrayInputStream(new byte[0]));
	}

	/**
	 * In memory configuration by location.
	 */
	private final Map<String, byte[]> items = new HashMap<String, byte[]>();

	/**
	 * Instantiate.
	 */
	public MemoryConfigurationContext() {
		this.init((location) -> {

			// Obtain the configuration
			byte[] content = this.items.get(location);
			return (content == null ? null : new ByteArrayInputStream(content));

		}, (location, isCreate, configuration) -> {

			// Obtain the configuration data
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			for (int byteValue = configuration.read(); byteValue >= 0; byteValue = configuration.read()) {
				buffer.write(byteValue);
			}
			buffer.flush();

			// Load the configuration into memory
			this.items.put(location, buffer.toByteArray());

		}, (location) -> {

			// Remove configuration from memory
			this.items.remove(location);
		});
	}

}
