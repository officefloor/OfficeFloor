/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param delegate
	 *            Delegate {@link SourceContext}.
	 * @param sourceProperties
	 *            {@link SourceProperties}.
	 */
	public ConfigurationSourceContextImpl(boolean isLoadingType, SourceContext delegate,
			SourceProperties sourceProperties) {
		super(isLoadingType, delegate, sourceProperties);

		// Configure the configuration context
		this.configurationContext = new ConfigurationContextImpl((location) -> this.getOptionalResource(location),
				sourceProperties);
	}

	/*
	 * ====================== ConfigurationContext ======================
	 */

	@Override
	public ConfigurationItem getConfigurationItem(String location, PropertyList overrideProperties)
			throws UnknownResourceError, ConfigurationError {
		return this.configurationContext.getConfigurationItem(location, overrideProperties);
	}

	@Override
	public ConfigurationItem getOptionalConfigurationItem(String location, PropertyList overrideProperties)
			throws ConfigurationError {
		return this.configurationContext.getOptionalConfigurationItem(location, overrideProperties);
	}

}