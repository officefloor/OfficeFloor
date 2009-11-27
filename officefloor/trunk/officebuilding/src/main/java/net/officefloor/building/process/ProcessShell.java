/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import java.io.ObjectInputStream;

/**
 * Provides the <code>main</code> method of an invoked {@link Process} for a
 * {@link ManagedProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessShell extends Thread implements ManagedProcessContext {

	/**
	 * Entrance point for running the {@link ManagedProcess}.
	 * 
	 * @param arguments
	 *            Arguments.
	 * @throws Throwable
	 *             If failure in running the {@link ManagedProcess}.
	 */
	public static void main(String... arguments) throws Throwable {

		// Obtain the Managed Process (always first)
		ObjectInputStream input = new ObjectInputStream(System.in);
		Object object = input.readObject();
		if (!(object instanceof ManagedProcess)) {
			throw new IllegalArgumentException("First object must be a "
					+ ManagedProcess.class.getName());
		}
		ManagedProcess managedProcess = (ManagedProcess) object;

		// Create instance to start processing requests
		ProcessShell context = new ProcessShell(input, arguments);
		context.setDaemon(true);
		context.start();

		// Run the managed process
		managedProcess.run(context);
	}

	/**
	 * {@link ObjectInputStream} to read in the requests.
	 */
	private final ObjectInputStream input;

	/**
	 * Command arguments.
	 */
	private final String[] commandArguments;

	/**
	 * Flag indicating if should continue processing.
	 */
	private volatile boolean isContinueProcessing = true;

	/**
	 * Initiate.
	 * 
	 * @param input
	 *            {@link ObjectInputStream} to read in the requests.
	 * @param commandArguments
	 *            Command arguments.
	 */
	public ProcessShell(ObjectInputStream input, String[] commandArguments) {
		this.input = input;
		this.commandArguments = commandArguments;
	}

	/*
	 * ========================== Thread ==================================
	 */

	@Override
	public void run() {
		try {
			// Loop forever processing requests
			for (;;) {

				// Read in the request
				Object object = this.input.readObject();

				// TODO read in requests
				System.out.println("TODO: process request: "
						+ object
						+ " ["
						+ (object == null ? "null" : object.getClass()
								.getName()
								+ "]"));

			}
		} catch (Throwable ex) {
			// Indicate failure
			ex.printStackTrace();
		}
	}

	/*
	 * ==================== ManagedProcessContext =========================
	 */

	@Override
	public String[] getCommandArguments() {
		return this.commandArguments;
	}

	@Override
	public boolean continueProcessing() {
		return this.isContinueProcessing;
	}

}