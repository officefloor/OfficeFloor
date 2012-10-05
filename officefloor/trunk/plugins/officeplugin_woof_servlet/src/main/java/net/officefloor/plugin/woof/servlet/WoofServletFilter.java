/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.servlet.OfficeFloorServletFilter;
import net.officefloor.plugin.woof.WoofLoader;
import net.officefloor.plugin.woof.WoofLoaderImpl;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * <p>
 * WoOF (Web on OfficeFloor) {@link Servlet} {@link Filter}.
 * <p>
 * This {@link Filter} enables embedding WoOF functionality within a JEE Servlet Application.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletFilter extends OfficeFloorServletFilter {

	/**
	 * Default WoOF configuration location.
	 */
	public static final String DEFAULT_WOOF_CONFIGUARTION_LOCATION = WoofOfficeFloorSource.DEFAULT_WOOF_CONFIGUARTION_LOCATION;

	/**
	 * Property for the location of the WoOF configuration.
	 */
	public static final String PROPERTY_WOOF_CONFIGURATION_LOCATION = WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION;

	/**
	 * Default Objects configuration location.
	 */
	public static final String DEFAULT_OBJECTS_CONFIGURATION_LOCATION = WoofOfficeFloorSource.DEFAULT_OBJECTS_CONFIGURATION_LOCATION;

	/**
	 * Property for the location of the Objects configuration.
	 */
	public static final String PROPERTY_OBJECTS_CONFIGURATION_LOCATION = WoofOfficeFloorSource.PROPERTY_OBJECTS_CONFIGURATION_LOCATION;

	/**
	 * Default Teams configuration location.
	 */
	public static final String DEFAULT_TEAMS_CONFIGURATION_LOCATION = WoofOfficeFloorSource.DEFAULT_TEAMS_CONFIGURATION_LOCATION;

	/**
	 * Property for the location of the Teams configuration.
	 */
	public static final String PROPERTY_TEAMS_CONFIGURATION_LOCATION = WoofOfficeFloorSource.PROPERTY_TEAMS_CONFIGURATION_LOCATION;

	/*
	 * ======================= OfficeFloorServletFilter ====================
	 */

	@Override
	protected void configure() throws Exception {

		// Create the configuration context
		ClassLoader classLoader = this.getOfficeFloorCompiler()
				.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);

		// Obtain the woof configuration (ensuring exists)
		String woofLocation = this.getInitParamValue(
				PROPERTY_WOOF_CONFIGURATION_LOCATION,
				DEFAULT_WOOF_CONFIGUARTION_LOCATION);
		ConfigurationItem woofConfiguration = configurationContext
				.getConfigurationItem(woofLocation);
		if (woofConfiguration == null) {
			throw new FileNotFoundException(
					"Can not find WoOF configuration at " + woofLocation);
		}

		// Configure this filter with WoOF functionality
		WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(
				new ModelRepositoryImpl()));
		woofLoader.loadWoofConfiguration(woofConfiguration, this);

		// Load the optional configuration
		String objectsLocation = this.getInitParamValue(
				PROPERTY_OBJECTS_CONFIGURATION_LOCATION,
				DEFAULT_OBJECTS_CONFIGURATION_LOCATION);
		String teamsLocation = this.getInitParamValue(
				PROPERTY_TEAMS_CONFIGURATION_LOCATION,
				DEFAULT_TEAMS_CONFIGURATION_LOCATION);
		WoofOfficeFloorSource.loadOptionalConfiguration(this, objectsLocation,
				teamsLocation, configurationContext);

		/*
		 * Note: WoOF Application extensions are not available within JEE
		 * container.
		 * 
		 * Focus of this Filter is only to providing WoOF functionality within a
		 * JEE container. If extension functionality is required then the
		 * application should migrated to using OfficeFloor. This is especially
		 * the case as the WoOF application extension is typically only used to
		 * embed a Servlet Container within WoOF.
		 */
	}

	/**
	 * Obtains the init parameter value.
	 * 
	 * @param propertyName
	 *            Name of property.
	 * @param defaultValue
	 *            Default value.
	 * @return Init parameter value or the default value.
	 */
	private String getInitParamValue(String propertyName, String defaultValue) {
		String value = this.getFilterConfig().getInitParameter(propertyName);
		if ((value == null) || (value.trim().length() == 0)) {
			value = defaultValue;
		}
		return value;
	}

}