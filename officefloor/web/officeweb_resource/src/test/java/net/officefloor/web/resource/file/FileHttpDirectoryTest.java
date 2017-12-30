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
package net.officefloor.web.resource.file;

import java.io.File;
import java.io.IOException;

import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.file.FileHttpDirectory;
import net.officefloor.web.resource.file.FileHttpResourceFactory;
import net.officefloor.web.resource.impl.AbstractHttpDirectoryTestCase;

/**
 * Tests the {@link FileHttpDirectory}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileHttpDirectoryTest extends AbstractHttpDirectoryTestCase {

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
		FileHttpResourceFactory.clearHttpResourceFactories();
		FileHttpResourceFactory factory = FileHttpResourceFactory
				.getHttpResourceFactory(warDirectory, "index.html");

		// Create and return the HTTP Directory
		return (HttpDirectory) factory.getHttpResource(resourcePath);
	}

}