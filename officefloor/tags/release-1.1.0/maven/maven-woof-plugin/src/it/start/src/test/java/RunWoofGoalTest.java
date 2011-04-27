/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Validates that the WoOF is running.
 * 
 * @author Daniel Sagenschneider
 */
public class RunWoofGoalTest extends TestCase {

	/**
	 * Ensure WoOF running.
	 */
	public void testRunning() throws Exception {

		// Connect to obtain 'index.html'
		URL url = new URL("http", "localhost", 7878, "/index.html");
		InputStream input = url.openConnection().getInputStream();

		// Ensure provides 'index.html' to be running
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int character = input.read(); character != -1; character = input
				.read()) {
			buffer.write(character);
		}
		assertEquals("Incorrect content",
				"<html><body>Hello World</body></html>",
				new String(buffer.toByteArray()));
	}

}