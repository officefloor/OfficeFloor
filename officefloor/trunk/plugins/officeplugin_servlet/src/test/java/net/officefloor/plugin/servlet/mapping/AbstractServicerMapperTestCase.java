/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.frame.test.OfficeFrameTestCase;

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
	 *            Expected {@link Servicer}.
	 * @param expectedServicerPath
	 *            Expected {@link Servicer} path.
	 * @param expectedPathInfo
	 *            Expected path info.
	 * @param expectedQueryString
	 *            Expected query string.
	 * @param expectedParameterNameValues
	 *            Expected parameter name values.
	 */
	protected static void assertMapping(ServicerMapping mapping,
			Servicer expectedServicer, String expectedServicerPath,
			String expectedPathInfo, String expectedQueryString,
			String... expectedParameterNameValues) {
		assertNotNull("Expecting mapping", mapping);
		assertEquals("Incorrect Servicer", expectedServicer, mapping
				.getServicer());
		assertEquals("Incorrect Servicer (Servlet) Path", expectedServicerPath,
				mapping.getServicerPath());
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
	 * Mock {@link Servicer} implementation for testing.
	 */
	protected class MockServicer implements Servicer {

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
		public String getServicerName() {
			return this.name;
		}

		@Override
		public String[] getServicerMappings() {
			return this.mappings;
		}
	}

}