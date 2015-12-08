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
package net.officefloor.maven.start;

import java.io.File;
import java.io.IOException;

/**
 * Creates the {@link File}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileCreator {

	/**
	 * Creates the {@link File}.
	 * 
	 * @param filePath
	 *            Path to {@link File} to be created.
	 */
	public void createFile(String filePath) {
		try {
			if (!new File(filePath).createNewFile()) {
				System.err.println("File already exists");
			}
		} catch (IOException ex) {
			System.err.println("Failed to create File");
			ex.printStackTrace();
		}
	}

}