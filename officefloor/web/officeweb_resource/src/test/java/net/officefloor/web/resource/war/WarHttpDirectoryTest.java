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
package net.officefloor.web.resource.war;

import java.io.File;
import java.io.IOException;

import net.officefloor.web.resource.AbstractHttpDirectoryTestCase;
import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.war.WarHttpDirectory;
import net.officefloor.web.resource.war.WarHttpResourceFactory;

/**
 * Tests the {@link WarHttpDirectory}.
 * 
 * @author Daniel Sagenschneider
 */
public class WarHttpDirectoryTest extends AbstractHttpDirectoryTestCase {

	/*
	 * ===================== AbstractHttpDirectoryTestCase ================
	 */

	@Override
	protected HttpDirectory createHttpDirectory(String resourcePath,
			String... defaultFileNames) throws IOException {

		// Find the WAR directory
		File warDirectory = this.findFile(AbstractHttpDirectoryTestCase.class,
				"index.html").getParentFile();

		// Obtain the 'fresh' resource factory
		WarHttpResourceFactory.clearHttpResourceFactories();
		WarHttpResourceFactory factory = WarHttpResourceFactory
				.getHttpResourceFactory(warDirectory, "index.html");

		// Create and return the HTTP Directory
		return (HttpDirectory) factory.createHttpResource(resourcePath);
	}

}