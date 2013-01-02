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
package net.officefloor.building.process.officefloor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Mock work for testing the running of the {@link OfficeFloor} {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWork {

	/**
	 * Message to be written.
	 */
	public static final String MESSAGE = "test message";

	/**
	 * Obtains System property by this name to include message to be written.
	 */
	public static final String INCLUDE_SYSTEM_PROPERTY = "test-system-property";

	/**
	 * Writes the message to the file.
	 * 
	 * @param filePath
	 *            Path to the file to write the message.
	 */
	public void writeMessage(String filePath) {
		try {

			// Obtain the system property value
			String value = System.getProperty(INCLUDE_SYSTEM_PROPERTY, "");

			// Write the message to the file
			Writer writer = new FileWriter(filePath);
			writer.write(MESSAGE);
			writer.write(value);
			writer.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}