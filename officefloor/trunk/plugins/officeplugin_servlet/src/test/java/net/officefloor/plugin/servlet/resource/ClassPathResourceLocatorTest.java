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
package net.officefloor.plugin.servlet.resource;

/**
 * Tests the {@link ClassPathResourceLocator}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathResourceLocatorTest extends
		AbstractResourceLocatorTestCase {

	@Override
	protected ResourceLocator createResourceLocator() throws Exception {

		// Obtain the class path prefix
		String classPathPrefix = this.getClass().getPackage().getName();

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Create and return the resource locator
		return new ClassPathResourceLocator(classPathPrefix, classLoader);
	}

}