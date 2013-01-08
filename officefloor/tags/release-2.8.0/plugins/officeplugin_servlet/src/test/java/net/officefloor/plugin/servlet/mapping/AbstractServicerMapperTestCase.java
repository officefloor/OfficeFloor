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
package net.officefloor.plugin.servlet.mapping;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.servlet.context.OfficeServletContext;

/**
 * Abstract {@link ServicerMapper} test functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractServicerMapperTestCase extends
		OfficeFrameTestCase {

	/**
	 * Asserts the {@link ServicerMapper} is correct.
	 * 
	 * @param mapping
	 *            Actual {@link ServicerMapping}.
	 * @param expectedServicer
	 *            Expected {@link HttpServletServicer}.
	 * @param expectedServicerPath
	 *            Expected {@link HttpServletServicer} path.
	 * @param expectedPathInfo
	 *            Expected path info.
	 * @param expectedQueryString
	 *            Expected query string.
	 * @param expectedParameterNameValues
	 *            Expected parameter name values.
	 */
	protected static void assertMapping(ServicerMapping mapping,
			HttpServletServicer expectedServicer, String expectedServicerPath,
			String expectedPathInfo, String expectedQueryString,
			String... expectedParameterNameValues) {
		assertNotNull("Expecting mapping", mapping);
		assertEquals("Incorrect Servicer", expectedServicer, mapping
				.getServicer());
		assertEquals("Incorrect Servicer (Servlet) Path", expectedServicerPath,
				mapping.getServletPath());
		assertEquals("Incorrect Path Info", expectedPathInfo, mapping
				.getPathInfo());
		assertEquals("Incorrect Query String", expectedQueryString, mapping
				.getQueryString());
		for (int i = 0; i < expectedParameterNameValues.length; i += 2) {
			String name = expectedParameterNameValues[i];
			String value = expectedParameterNameValues[i + 1];
			assertEquals("Incorrect parameter '" + name + "'", value, mapping
					.getParameter(name));
		}
	}

	/**
	 * Mock {@link HttpServletServicer} implementation for testing.
	 */
	protected class MockServicer implements HttpServletServicer {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Mappings.
		 */
		private final String[] mappings;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name.
		 * @param mappings
		 *            Mappings.
		 */
		public MockServicer(String name, String... mappings) {
			this.name = name;
			this.mappings = mappings;
		}

		@Override
		public String toString() {
			return this.name;
		}

		/*
		 * =================== Servicer ========================
		 */

		@Override
		public String getServletName() {
			return this.name;
		}

		@Override
		public String[] getServletMappings() {
			return this.mappings;
		}

		@Override
		public void include(OfficeServletContext context,
				HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			fail("Should not be invoked");
		}
	}

}