/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.servlet.OfficeFloorServlet;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.woof.WoofLoader;
import net.officefloor.plugin.woof.WoofLoaderImpl;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * <p>
 * WoOF (Web on OfficeFloor) {@link Servlet}.
 * <p>
 * This {@link Servlet} enables embedding WoOF functionality within a JEE
 * Servlet Application. To enable handling the appropriate
 * {@link HttpServletRequest} instances this should be configured as a
 * {@link ServletContextListener}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServlet extends OfficeFloorServlet {

	/**
	 * <p>
	 * Name that this {@link WoofServlet} should be registered.
	 * <p>
	 * This enables <code>web-fragment</code> functionality to determine if an
	 * implementation of the {@link WoofServlet} has been configured or whether
	 * one should be automatically configured.
	 */
	public static final String SERVLET_NAME = "WoOF";

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
	 * ======================= OfficeFloorServlet ====================
	 */

	@Override
	public String getServletName() {
		return SERVLET_NAME;
	}

	@Override
	public String getTemplateUriSuffix() {
		return WoofOfficeFloorSource.WOOF_TEMPLATE_URI_SUFFIX;
	}

	@Override
	public boolean configure(WebAutoWireApplication application,
			ServletContext servletContext) throws Exception {

		// Create the configuration context
		ClassLoader classLoader = application.getOfficeFloorCompiler()
				.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);

		// Obtain the woof configuration (ensuring exists)
		String woofLocation = this.getInitParamValue(servletContext,
				PROPERTY_WOOF_CONFIGURATION_LOCATION,
				DEFAULT_WOOF_CONFIGUARTION_LOCATION);
		ConfigurationItem woofConfiguration = configurationContext
				.getConfigurationItem(woofLocation);
		if (woofConfiguration == null) {
			// No configuration file so not configure Servlet
			servletContext.log("No WoOF configuration file at location "
					+ woofLocation
					+ ". WoOF functionality will not be configured.");
			return false; // do not configure
		}

		// Create the Source Context
		SourceContext sourceContext = new SourceContextImpl(false, classLoader);

		// Configure this filter with WoOF functionality
		WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(
				new ModelRepositoryImpl()));
		woofLoader.loadWoofConfiguration(woofConfiguration, application,
				sourceContext);

		// Load the optional configuration
		String objectsLocation = this.getInitParamValue(servletContext,
				PROPERTY_OBJECTS_CONFIGURATION_LOCATION,
				DEFAULT_OBJECTS_CONFIGURATION_LOCATION);
		String teamsLocation = this.getInitParamValue(servletContext,
				PROPERTY_TEAMS_CONFIGURATION_LOCATION,
				DEFAULT_TEAMS_CONFIGURATION_LOCATION);
		WoofOfficeFloorSource.loadOptionalConfiguration(application,
				objectsLocation, teamsLocation, configurationContext, null);

		/*
		 * Note: WoOF Application extensions are not available within JEE
		 * container.
		 * 
		 * Focus of this Servlet is only to providing WoOF functionality within
		 * a JEE container. If extension functionality is required then the
		 * application should be migrated to using OfficeFloor. This is
		 * especially the case as the WoOF application extension is typically
		 * only used to embed a Servlet Container within WoOF.
		 */

		// Configure
		return true;
	}

	/**
	 * Obtains the init parameter value.
	 * 
	 * @param servletContext
	 *            {@link ServletContext}.
	 * @param propertyName
	 *            Name of property.
	 * @param defaultValue
	 *            Default value.
	 * @return Init parameter value or the default value.
	 */
	private String getInitParamValue(ServletContext servletContext,
			String propertyName, String defaultValue) {
		String value = servletContext.getInitParameter(propertyName);
		if ((value == null) || (value.trim().length() == 0)) {
			value = defaultValue;
		}
		return value;
	}

}