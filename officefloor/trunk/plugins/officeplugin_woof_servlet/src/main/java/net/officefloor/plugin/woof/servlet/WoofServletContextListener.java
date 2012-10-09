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

import java.io.InputStream;

import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * {@link ServletContextListener} to configure WoOF functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletContextListener implements ServletContextListener {

	/*
	 * ======================= ServletContextListener =========================
	 */

	@Override
	public void contextInitialized(ServletContextEvent event) {

		// Obtain the Servlet context
		ServletContext context = event.getServletContext();

		// Determine the desired location of the application.woof file
		String applicationWoofLocation = context
				.getInitParameter(WoofServletFilter.PROPERTY_WOOF_CONFIGURATION_LOCATION);
		if (applicationWoofLocation == null) {
			applicationWoofLocation = WoofServletFilter.DEFAULT_WOOF_CONFIGUARTION_LOCATION;
		}

		// Determine if application.woof file for the web application
		InputStream applicationWoofConfiguration = Thread.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(applicationWoofLocation);
		if (applicationWoofConfiguration == null) {
			// No WoOF configuration found so not loading WoOF Servlet Filter
			return;
		}

		// Determine if the WoOF Servlet Filter is already registered
		FilterRegistration existingFilter = context
				.getFilterRegistration(WoofServletFilter.FILTER_NAME);
		if (existingFilter != null) {
			// Already registered
			return;
		}

		// Automatically configure the WoOF Servlet Filter
		Dynamic filterDynamic = context.addFilter(
				WoofServletFilter.FILTER_NAME, WoofServletFilter.class);
		filterDynamic.addMappingForUrlPatterns(null, false, "/*");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

}