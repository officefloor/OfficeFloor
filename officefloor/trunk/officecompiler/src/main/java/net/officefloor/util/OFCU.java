/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.build.BuildException;

/**
 * {@link OfficeFloorCompiler} util.
 * 
 * @author Daniel
 */
public class OFCU {

	/**
	 * Returns the input object ensuring it is not <code>null</code>.
	 * 
	 * @param object
	 *            Object to check not <code>null</code>.
	 * @param message
	 *            Message with ${index} of parameters being replaced.
	 * @param parameters
	 *            Parameters.
	 * @return Not <code>null</code> object.
	 * @throws BuildException
	 *             If object is <code>null</code>.
	 */
	public static <T> T get(T object, String message, Object... parameters)
			throws BuildException {

		// Return if not null
		if (object != null) {
			return object;
		}

		// Construct the message
		for (int i = 0; i < parameters.length; i++) {
			Object parameter = parameters[i];
			message = message.replace("${" + i + "}",
					(parameter == null ? "null" : parameter.toString()));
		}

		// Throw failure
		throw new BuildException(message);
	}

	/**
	 * Generates an stack trace message from the input {@link Throwable}.
	 * 
	 * @param ex
	 *            {@link Throwable}.
	 * @return Stack trace of message.
	 */
	public static String exMsg(Throwable ex) {
		StringWriter buffer = new StringWriter();
		buffer.append("propagation of another exception:\n   cause: ");
		ex.printStackTrace(new PrintWriter(buffer));
		return buffer.toString();
	}

	/**
	 * All access via static methods.
	 */
	private OFCU() {
	}

}
