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
package net.officefloor.plugin.servlet.container;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ServletConfigImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletConfigTest extends OfficeFrameTestCase {

	/**
	 * Ensure methods return correctly.
	 */
	public void testServletConfig() {

		// Create mocks
		final String servletName = "Serlet";
		final ServletContext context = this.createMock(ServletContext.class);
		final Map<String, String> initParameters = new HashMap<String, String>();
		initParameters.put("available", "value");

		// Create the servlet config
		ServletConfig config = new ServletConfigImpl(servletName, context,
				initParameters);

		// Validate methods
		assertEquals("Incorrect servlet name", servletName, config
				.getServletName());
		assertEquals("Incorrect context", context, config.getServletContext());

		// Validate parameters
		assertEquals("getInitParameter(available)", "value", config
				.getInitParameter("available"));
		assertNull("getInitParameter(none)", config.getInitParameter("none"));
		Enumeration<String> enumeration = config.getInitParameterNames();
		assertTrue("Expect an init parameter name", enumeration
				.hasMoreElements());
		assertEquals("Incorrect parameter name", "available", enumeration
				.nextElement());
		assertFalse("No further init parameter names", enumeration
				.hasMoreElements());
	}

}