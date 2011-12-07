/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.woof.servlet;

import java.io.FileNotFoundException;
import java.util.logging.Filter;

import javax.servlet.Servlet;

import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.WoofRepository;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.servlet.OfficeFloorServletFilter;
import net.officefloor.plugin.woof.WoofLoader;
import net.officefloor.plugin.woof.WoofLoaderImpl;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * WoOF (Web on OfficeFloor) {@link Servlet} {@link Filter}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletFilter extends OfficeFloorServletFilter {

	/**
	 * Default WoOF configuration location.
	 */
	public static final String DEFAULT_WOOF_CONFIGUARTION_LOCATION = WoofOfficeFloorSource.DEFAULT_WOOF_CONFIGUARTION_LOCATION;

	/**
	 * Property for the location of the WoOF configuration for the application.
	 */
	public static final String PROPERTY_WOOF_CONFIGURATION_LOCATION = WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION;

	/*
	 * ======================= OfficeFloorServletFilter ====================
	 */

	@Override
	protected void configure() throws Exception {

		// Obtain the location of the configuration
		String location = this.getFilterConfig().getInitParameter(
				PROPERTY_WOOF_CONFIGURATION_LOCATION);
		if ((location == null) || (location.trim().length() == 0)) {
			location = DEFAULT_WOOF_CONFIGUARTION_LOCATION;
		}

		// Obtain the woof configuration (ensuring exists)
		ClassLoader classLoader = this.getOfficeFloorCompiler()
				.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);
		ConfigurationItem configuration = configurationContext
				.getConfigurationItem(location);
		if (configuration == null) {
			throw new FileNotFoundException(
					"Can not find WoOF configuration at " + location);
		}

		// Create the WoOF loader
		WoofRepository repository = new WoofRepositoryImpl(
				new ModelRepositoryImpl());
		WoofLoader woofLoader = new WoofLoaderImpl(repository);

		// Configure this filter
		woofLoader.loadWoofConfiguration(configuration, this);
	}

}