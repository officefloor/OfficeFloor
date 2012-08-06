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
package net.officefloor.tutorials.performance.nio.test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

import net.officefloor.tutorials.performance.JettyServicer;
import net.officefloor.tutorials.performance.Servicer;

/**
 * 
 * 
 * @author Daniel Sagenschneider
 */
public class TODO_REMOVE_ME_Test extends TestCase {

	public void testRemove() throws Exception {
		Servicer servicer = new JettyServicer();
		servicer.start();
		URLConnection connection = new URL("http://localhost:"
				+ servicer.getPort() + "/test.php?v=N").openConnection();
		connection.setReadTimeout(0);
		InputStream stream = connection.getInputStream();
		Reader reader = new InputStreamReader(stream);
		for (int character = reader.read(); character != -1; character = reader
				.read()) {
			System.out.print((char) character);
		}
		System.out.println();
	}

}
