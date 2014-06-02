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
package net.officefloor.building.process;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link ProcessOutputStreamFactory} to output to <code>stdout</code> and
 * <code>stderr</code>.
 * 
 * @author Daniel Sagenschneider
 */
public class StdProcessOutputStreamFactory implements
		ProcessOutputStreamFactory {

	/**
	 * INdicates if debug information should be printed.
	 */
	private final boolean isDebug;

	/**
	 * Initiate.
	 */
	public StdProcessOutputStreamFactory() {
		this(false);
	}

	/**
	 * Initiate.
	 * 
	 * @param isDebug
	 *            Should debug information be printed.
	 */
	public StdProcessOutputStreamFactory(boolean isDebug) {
		this.isDebug = isDebug;
	}

	/*
	 * =================== ProcessOutputStreamFactory =====================
	 */

	@Override
	public OutputStream createStandardProcessOutputStream(
			String processNamespace, String[] command) throws IOException {

		// Determine if provide debug information
		if (this.isDebug) {

			// Indicate running process
			System.out.println("Spawning process " + processNamespace
					+ " with command:");

			// Obtain the command
			boolean isFirst = true;
			for (String commandItem : command) {
				if (!isFirst) {
					System.out.print(" ");
				}
				isFirst = false;
				System.out.print(commandItem);
			}
			System.out.println();
		}

		// Return System out
		return System.out;
	}

	@Override
	public OutputStream createErrorProcessOutputStream(String processNamespace)
			throws IOException {
		return System.err;
	}

}