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

import java.io.File;

/**
 * Tests the {@link FileSystemResourceLocator}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileSystemResourceLocatorTest extends
		AbstractResourceLocatorTestCase {

	@Override
	protected ResourceLocator createResourceLocator() throws Exception {

		// Obtain file system root
		File root = this.findFile(this.getClass(), ".");

		// Create and return the resource locator
		return new FileSystemResourceLocator(root);
	}

}