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
package net.officefloor.plugin.web.http.resource;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;

import net.officefloor.plugin.web.http.resource.war.WarHttpResourceFactory;

/**
 * Tests the {@link WarHttpResourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO provide implementation of WarHttpResourceFactory")
public class WarHttpResourceFactoryTest extends
		AbstractHttpResourceFactoryTestCase {

	/*
	 * ============== AbstractHttpResourceFactoryTestCase ==================
	 */

	@Override
	protected HttpResourceFactory createHttpResourceFactory(String namespace)
			throws IOException {

		// Find the war directory
		File warDir = this.findFile(this.getClass(), "index.html")
				.getParentFile();

		// Create and return the WAR HTTP ResourceFactory
		return new WarHttpResourceFactory(warDir);
	}

}