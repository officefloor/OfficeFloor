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

package net.officefloor.configuration.impl;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationError;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link SourceContext} and {@link ConfigurationContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurationSourceContextImpl extends SourceContextImpl implements ConfigurationContext {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * Instantiate.
	 * 
	 * @param sourceName         Name of source.
	 * @param isLoadingType      Indicates if loading type.
	 * @param delegate           Delegate {@link SourceContext}.
	 * @param additionalProfiles Additional profiles.
	 * @param sourceProperties   {@link SourceProperties}.
	 */
	public ConfigurationSourceContextImpl(String sourceName, boolean isLoadingType, SourceContext delegate,
			String[] additionalProfiles, SourceProperties sourceProperties) {
		super(sourceName, isLoadingType, additionalProfiles, delegate, sourceProperties);

		// Configure the configuration context
		this.configurationContext = new ConfigurationContextImpl((location) -> this.getOptionalResource(location),
				sourceProperties);
	}

	/*
	 * ====================== ConfigurationContext ======================
	 */

	@Override
	public ConfigurationItem getConfigurationItem(String location, PropertyList properties)
			throws UnknownResourceError, ConfigurationError {
		return this.configurationContext.getConfigurationItem(location, properties);
	}

	@Override
	public ConfigurationItem getOptionalConfigurationItem(String location, PropertyList properties)
			throws ConfigurationError {
		return this.configurationContext.getOptionalConfigurationItem(location, properties);
	}

}
