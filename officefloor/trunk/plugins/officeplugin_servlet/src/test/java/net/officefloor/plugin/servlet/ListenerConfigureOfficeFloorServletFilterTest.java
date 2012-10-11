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
package net.officefloor.plugin.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import net.officefloor.frame.test.OfficeFrameTestCase;

import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Tests the {@link OfficeFloorServletFilter} being configured via a
 * {@link ServletContextListener}.
 * 
 * @author Daniel Sagenschneider
 */
public class ListenerConfigureOfficeFloorServletFilterTest extends
		AbstractOfficeFloorServletFilterTestCase {

	@Override
	protected void configureFilter(final Filter filter,
			ServletContextHandler context) {
		// Provide servlet context listener to configure the filter
		context.addEventListener(new ServletContextListener() {

			@Override
			public void contextInitialized(ServletContextEvent event) {

				// Obtain the servlet context
				ServletContext context = event.getServletContext();

				// Initialise the OfficeFloor Servlet Filter
				if (filter instanceof OfficeFloorServletFilter) {
					OfficeFloorServletFilter officeFloorFilter = (OfficeFloorServletFilter) filter;
					try {
						officeFloorFilter.addFilter("FILTER", context);
					} catch (ServletException ex) {
						OfficeFrameTestCase.fail(ex);
					}

				} else {
					// Configure the filter
					Dynamic dynamic = context.addFilter("FILTER", filter);
					dynamic.addMappingForUrlPatterns(null, false, "/*");
				}
			}

			@Override
			public void contextDestroyed(ServletContextEvent event) {
				// Do nothing as handled by filter
			}
		});
	}

}