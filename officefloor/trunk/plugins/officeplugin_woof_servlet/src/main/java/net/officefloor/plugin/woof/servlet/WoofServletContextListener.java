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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

/**
 * {@link ServletContextListener} to configure WoOF functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletContextListener implements ServletContextListener {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(WoofServletContextListener.class.getName());

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

		// Add the WoOF Servlet Filter
		try {
			new WoofServletFilter().addFilter(WoofServletFilter.FILTER_NAME,
					context);
		} catch (ServletException ex) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(
						Level.SEVERE,
						"Failed to automatically configure the "
								+ WoofServletFilter.class.getSimpleName()
								+ ". WoOF functionality will not be available.",
						ex);
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

}