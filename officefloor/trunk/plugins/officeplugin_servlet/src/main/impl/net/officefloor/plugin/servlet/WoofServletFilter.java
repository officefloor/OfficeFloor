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
package net.officefloor.plugin.servlet;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Filter;

import javax.servlet.Servlet;

import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.woof.WoofRepository;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.woof.WoofLoader;
import net.officefloor.plugin.woof.WoofLoaderImpl;

/**
 * WoOF (Web on OfficeFloor) {@link Servlet} {@link Filter}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletFilter extends OfficeFloorServletFilter {

	/**
	 * Default WoOF configuration location.
	 */
	public static final String DEFAULT_WOOF_CONFIGUARTION_LOCATION = "WEB-INF/application.woof";

	/**
	 * Property for the location of the WoOF configuration for the application.
	 */
	public static final String PROPERTY_WOOF_CONFIGURATION_LOCATION = "woof.configuration.location";

	/*
	 * ======================= OfficeFloorServletFilter ====================
	 */

	@Override
	protected void configure() throws Exception {

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Obtain the location of the configuration
		String location = this.getFilterConfig().getInitParameter(
				PROPERTY_WOOF_CONFIGURATION_LOCATION);
		if ((location == null) || (location.trim().length() == 0)) {
			location = DEFAULT_WOOF_CONFIGUARTION_LOCATION;
		}

		// Ensure configuration available
		InputStream available = classLoader.getResourceAsStream(location);
		if (available == null) {
			throw new FileNotFoundException(
					"Can not find WoOF configuration at " + location);
		}
		available.close(); // only need to see if available

		// Create the WoOF loader
		ConfigurationContext context = new ClassLoaderConfigurationContext(
				classLoader);
		WoofRepository repository = new WoofRepositoryImpl(
				new ModelRepositoryImpl());
		WoofLoader woofLoader = new WoofLoaderImpl(classLoader, context,
				repository);

		// Configure this filter
		woofLoader.loadWoofConfiguration(location, this);
	}

}