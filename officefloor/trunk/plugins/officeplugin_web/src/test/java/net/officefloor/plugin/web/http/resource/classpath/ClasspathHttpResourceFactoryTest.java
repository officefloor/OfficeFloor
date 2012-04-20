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

package net.officefloor.plugin.web.http.resource.classpath;

import net.officefloor.plugin.web.http.resource.AbstractHttpResourceFactoryTestCase;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;

/**
 * Tests the {@link ClasspathHttpResourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpResourceFactoryTest extends
		AbstractHttpResourceFactoryTestCase {

	/*
	 * ============== AbstractHttpResourceFactoryTestCase ==================
	 */

	@Override
	protected HttpResourceFactory createHttpResourceFactory(String prefix) {

		// Clear to create new instance
		ClasspathHttpResourceFactory.clearHttpResourceFactories();

		// Create the factory to obtain files from test package
		HttpResourceFactory factory = ClasspathHttpResourceFactory
				.getHttpResourceFactory(prefix, "index.html");

		// Return the factory
		return factory;
	}

}