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

package net.officefloor.plugin.servlet.log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * {@link Logger} to log to <code>stdout</code>.
 * 
 * @author Daniel Sagenschneider
 */
public class StdoutLogger implements Logger {

	/*
	 * ================== Logger ========================
	 */

	@Override
	public void log(String message) {
		System.out.println(message);
	}

	@Override
	public void log(String message, Throwable failure) {

		// Construct message to log
		StringWriter buffer = new StringWriter();
		buffer.append(message);
		buffer.append("\n");
		failure.printStackTrace(new PrintWriter(buffer));

		// Log the message
		this.log(buffer.toString());
	}

}