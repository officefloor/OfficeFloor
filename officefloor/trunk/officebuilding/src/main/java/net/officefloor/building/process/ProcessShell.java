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

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

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
		ObjectInputStream fromParentPipe = new ObjectInputStream(System.in);
		Object object = fromParentPipe.readObject();
		if (!(object instanceof ManagedProcess)) {
			throw new IllegalArgumentException("First object must be a "
					+ ManagedProcess.class.getName());
		}
		ManagedProcess managedProcess = (ManagedProcess) object;

		// Connect to parent to send responses
		int parentPort = fromParentPipe.readInt();
		Socket parentSocket = new Socket();
		parentSocket.connect(new InetSocketAddress(parentPort));
		ObjectOutputStream toParentPipe = new ObjectOutputStream(parentSocket
				.getOutputStream());

		// Create instance to start processing commands
		ProcessShell context = new ProcessShell(fromParentPipe, toParentPipe,
				managedProcess, arguments);
		context.setDaemon(true);
		context.start();

		// Run the managed process
		managedProcess.run(context);
	}

	/**
	 * {@link ObjectInputStream} to read in the requests.
	 */
	private final ObjectInputStream fromParentPipe;

	/**
	 * {@link ObjectOutputStream} to send the responses.
	 */
	private final ObjectOutputStream toParentPipe;

	/**
	 * {@link ManagedProcess}.
	 */
	private final ManagedProcess managedProcess;

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
	 * @param fromParentPipe
	 *            {@link ObjectInputStream} to read in the requests.
	 * @param toParentPipe
	 *            {@link ObjectOutputStream} to send the responses.
	 * @param managedProcess
	 *            {@link ManagedProcess}.
	 * @param commandArguments
	 *            Command arguments.
	 */
	public ProcessShell(ObjectInputStream fromParentPipe,
			ObjectOutputStream toParentPipe, ManagedProcess managedProcess,
			String[] commandArguments) {
		this.fromParentPipe = fromParentPipe;
		this.toParentPipe = toParentPipe;
		this.managedProcess = managedProcess;
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
				Object object = this.fromParentPipe.readObject();
				if (!(object instanceof ProcessRequest)) {
					throw new IllegalArgumentException("Unknown request: "
							+ object
							+ " ["
							+ (object == null ? "null" : object.getClass()
									.getName()));
				}
				ProcessRequest request = (ProcessRequest) object;

				// Handle request
				if (request.isStop()) {
					// Flag to stop
					this.isContinueProcessing = false;

				} else {
					// Run the command
					long requestId = request.getRequestId();
					Object command = request.getCommand();

					// Create the Command executor and run command
					new CommandExecutor(requestId, command,
							this.managedProcess, this.toParentPipe).start();
				}
			}
		} catch (EOFException ex) {
			// Process is complete
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

	/**
	 * {@link Thread} to do a command on the {@link ManagedProcess}.
	 */
	private static class CommandExecutor extends Thread {

		/**
		 * {@link ProcessRequest} Id to correlate response with request.
		 */
		private final long requestId;

		/**
		 * Command to execute.
		 */
		private final Object command;

		/**
		 * {@link ManagedProcess}.
		 */
		private final ManagedProcess managedProcess;

		/**
		 * {@link ObjectOutputStream} to send the response.
		 */
		private final ObjectOutputStream toParentPipe;

		/**
		 * Initiate.
		 * 
		 * @param requestId
		 *            {@link ProcessRequest} Id to correlate response with
		 *            request.
		 * @param command
		 *            Command to execute.
		 * @param managedProcess
		 *            {@link ManagedProcess}.
		 * @param toParentPipe
		 *            {@link ObjectOutputStream} to send the response.
		 */
		public CommandExecutor(long requestId, Object command,
				ManagedProcess managedProcess, ObjectOutputStream toParentPipe) {
			super("ProcessRequest" + requestId);
			this.requestId = requestId;
			this.command = command;
			this.managedProcess = managedProcess;
			this.toParentPipe = toParentPipe;
		}

		/*
		 * ===================== Thread =================================
		 */

		@Override
		public void run() {

			// Obtain the process response
			ProcessResponse response;
			try {

				// Do the command
				Object commandResponse = this.managedProcess
						.doCommand(this.command);

				// Create the successful command response
				response = new ProcessResponse(this.requestId, commandResponse,
						null);
			} catch (Throwable ex) {
				// Create the failed command response
				response = new ProcessResponse(this.requestId, null, ex);
			}

			// Send the response
			try {
				this.toParentPipe.writeObject(response);
				this.toParentPipe.flush();
			} catch (Throwable ex) {
				// Indicate failure
				ex.printStackTrace();
			}
		}
	}

}