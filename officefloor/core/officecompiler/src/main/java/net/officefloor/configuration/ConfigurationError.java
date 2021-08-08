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

import net.officefloor.frame.api.source.AbstractSourceError;

/**
 * <p>
 * Indicates a failure in obtaining configuration.
 * <p>
 * This is a critical error as the source is requiring the
 * {@link ConfigurationItem} to initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurationError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Location of the configuration.
	 */
	private final String configurationLocation;

	/**
	 * Name of tag in configuration that is not configured. May be <code>null</code>
	 * to indicate {@link Throwable} cause to configuration issue.
	 */
	private final String nonconfiguredTagName;

	/**
	 * Instantiate.
	 * 
	 * @param missingLocation Location of missing {@link ConfigurationItem}.
	 */
	public ConfigurationError(String missingLocation) {
		super("Can not obtain " + ConfigurationItem.class.getSimpleName() + " at location '" + missingLocation + "'");
		this.configurationLocation = missingLocation;
		this.nonconfiguredTagName = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param configurationLocation Location of the {@link ConfigurationItem}.
	 * @param cause                 {@link Throwable} cause.
	 */
	public ConfigurationError(String configurationLocation, Throwable cause) {
		super("Failed to obtain " + ConfigurationItem.class.getSimpleName() + " at location '" + configurationLocation
				+ "': " + cause.getMessage(), cause);
		this.configurationLocation = configurationLocation;
		this.nonconfiguredTagName = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param configurationLocation Location of the {@link ConfigurationItem}.
	 * @param nonconfiguredTagName  Name of tag in configuration that is not
	 *                              configured.
	 */
	public ConfigurationError(String configurationLocation, String nonconfiguredTagName) {
		super("Can not obtain " + ConfigurationItem.class.getSimpleName() + " at location '" + configurationLocation
				+ "' as missing property '" + nonconfiguredTagName + "'");
		this.configurationLocation = configurationLocation;
		this.nonconfiguredTagName = nonconfiguredTagName;
	}

	/**
	 * Obtains the location of the {@link ConfigurationItem}.
	 * 
	 * @return Location of the {@link ConfigurationItem}.
	 */
	public String getConfigurationLocation() {
		return this.configurationLocation;
	}

	/**
	 * Obtains the non-configured tag name.
	 * 
	 * @return Non-configured tag name.
	 */
	public String getNonconfiguredTagName() {
		return this.nonconfiguredTagName;
	}

}
