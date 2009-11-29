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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages a {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessManager {

	/**
	 * {@link Timer}.
	 */
	private static Timer TIMER = new Timer(true);

	/**
	 * Starts the {@link Process} for the {@link ManagedProcess} returning the
	 * managing {@link ProcessManager}.
	 * 
	 * @param managedProcess
	 *            {@link ManagedProcess}.
	 * @return {@link ProcessManager}.
	 * @throws ProcessException
	 *             If fails to start the {@link Process}.
	 */
	public static ProcessManager startProcess(ManagedProcess managedProcess)
			throws ProcessException {

		// Obtain the java bin directory location
		String javaHome = System.getProperty("java.home");
		File javaBinDir = new File(javaHome, "bin");
		if (!javaBinDir.isDirectory()) {
			throw new ProcessException("Can not find java bin directory at "
					+ javaBinDir.getAbsolutePath());
		}

		// Obtain the java executable
		File javaExecutable = new File(javaBinDir, "java");
		if (!javaExecutable.isFile()) {
			// Not unix/linux, so try for windows
			javaExecutable = new File(javaBinDir, "javaw.exe");
			if (!javaExecutable.isFile()) {
				// Not windows either, so can not find
				throw new ProcessException("Can not find java executable in "
						+ javaBinDir.getAbsolutePath());
			}
		}

		// Obtain the class path
		String classPath = System.getProperty("java.class.path");

		// Create the command to invoke process
		List<String> command = new LinkedList<String>();
		command.add(javaExecutable.getAbsolutePath());
		command.add("-cp");
		command.add(classPath);
		command.add(ProcessShell.class.getName());

		try {
			// Create the from process communication pipe
			ServerSocket fromProcessServerSocket = new ServerSocket();
			fromProcessServerSocket.bind(null); // bind to any available port
			int fromProcessPort = fromProcessServerSocket.getLocalPort();

			// Invoke the process
			ProcessBuilder builder = new ProcessBuilder(command);
			Process process = builder.start();

			// Create the process manager for the process
			ObjectOutputStream toProcessPipe = new ObjectOutputStream(process
					.getOutputStream());
			ProcessManager processManager = new ProcessManager(process,
					toProcessPipe);

			// Gobble the process's stdout and stderr
			new StreamGobbler(process.getInputStream(), processManager).start();
			new StreamGobbler(process.getErrorStream(), processManager).start();

			// Handle process responses
			new ProcessResponseHandler(processManager, fromProcessServerSocket)
					.start();

			// Send managed process and response port to process
			toProcessPipe.writeObject(managedProcess);
			toProcessPipe.writeInt(fromProcessPort);
			toProcessPipe.flush();

			// Return the manager for the process
			return processManager;

		} catch (IOException ex) {
			// Propagate failure
			throw new ProcessException(ex);
		}
	}

	/**
	 * Map of active commands by request id.
	 */
	private final Map<Long, CommandCallback> activeCommands = new HashMap<Long, CommandCallback>();

	/**
	 * {@link Process} being managed.
	 */
	private final Process process;

	/**
	 * {@link ObjectOutputStream} to send the {@link ProcessRequest} instances
	 * to the {@link Process}.
	 */
	private final ObjectOutputStream toProcessPipe;

	/**
	 * Next {@link ProcessRequest} Id.
	 */
	private long nextRequestId = 1;

	/**
	 * Flag indicating if the {@link Process} is complete.
	 */
	private volatile boolean isComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param process
	 *            {@link Process} being managed.
	 * @param toProcessPipe
	 *            {@link ObjectOutputStream} to send the {@link ProcessRequest}
	 *            instances to the {@link Process}.
	 */
	private ProcessManager(Process process, ObjectOutputStream toProcessPipe) {
		this.process = process;
		this.toProcessPipe = toProcessPipe;
	}

	/**
	 * <p>
	 * Triggers for a graceful shutdown of the {@link Process}.
	 * <p>
	 * This is a non-blocking call to allow a timeout on graceful shutdown.
	 * 
	 * @throws ProcessException
	 *             If fails to trigger stopping the {@link Process}.
	 */
	public synchronized void triggerStopProcess() throws ProcessException {

		// Ignore if already complete
		if (this.isComplete) {
			return;
		}

		// Send stop request
		this.sendRequest(ProcessRequest.STOP_REQUEST);
	}

	/**
	 * Triggers forcibly destroying the {@link Process}.
	 */
	public void destroyProcess() {
		this.process.destroy();
	}

	/**
	 * Determines if the {@link ManagedProcess} is complete.
	 * 
	 * @return <code>true</code> if the {@link ManagedProcess} is complete.
	 */
	public boolean isProcessComplete() {
		// Return whether process complete
		return this.isComplete;
	}

	/**
	 * <p>
	 * Does the command on the {@link ManagedProcess}.
	 * <p>
	 * This asynchronously invokes the command and as such this method is
	 * non-blocking. Use the {@link CommandCallback} to be informed about
	 * completion of the command.
	 * 
	 * @param command
	 *            Command.
	 * @param callback
	 *            {@link CommandCallback}. May be <code>null</code> if not
	 *            interested in results of command.
	 * @throws ProcessException
	 *             If fails to invoke the command.
	 */
	public synchronized void doCommand(Object command, CommandCallback callback)
			throws ProcessException {

		// Create the request for the command
		long requestId = this.nextRequestId++;
		ProcessRequest request = new ProcessRequest(requestId, command);

		// Create and register the command
		this.activeCommands.put(new Long(requestId), callback);

		// Send the request
		this.sendRequest(request);
	}

	/**
	 * Convenience method to invoke a command on the {@link ManagedProcess}
	 * blocking until the command completes.
	 * 
	 * @param command
	 *            Command.
	 * @param timeout
	 *            Timeout of the command.
	 * @return Response.
	 * @throws ProcessException
	 *             If command fails or times out.
	 */
	public Object doCommand(Object command, long timeout)
			throws ProcessException {

		// Create the blocking command call back
		BlockingCommandCallback callback = new BlockingCommandCallback();

		// Register for time out
		TIMER.schedule(callback, timeout);

		// Do command
		this.doCommand(command, callback);

		// Block waiting for response / failure / time out
		return callback.getResponse();
	}

	/**
	 * Flags the {@link Process} is complete.
	 */
	private void flagComplete() {
		this.isComplete = true;
	}

	/**
	 * Obtains the {@link CommandCallback} for the completed command.
	 * 
	 * @param requestId
	 *            {@link ProcessRequest} id of the command.
	 * @return {@link CommandCallback} or <code>null</code> if none available.
	 */
	private synchronized CommandCallback getCallbackForCompletedCommand(
			Long requestId) {

		// Obtain the command call back
		CommandCallback callback = this.activeCommands.get(requestId);
		if (callback != null) {
			// Command no longer active
			this.activeCommands.remove(requestId);
		}

		// Return the command call back
		return callback;
	}

	/**
	 * Sends the {@link ProcessRequest} to the {@link Process}.
	 * 
	 * @param request
	 *            {@link ProcessRequest}.
	 * @throws ProcessException
	 *             {@link ProcessException}.
	 */
	private void sendRequest(ProcessRequest request) throws ProcessException {
		try {
			// Send the request to the process
			this.toProcessPipe.writeObject(request);
			this.toProcessPipe.flush();

		} catch (IOException ex) {
			// Propagate failure
			throw new ProcessException(ex);
		}
	}

	/**
	 * Blocking {@link CommandCallback}.
	 */
	private static class BlockingCommandCallback extends TimerTask implements
			CommandCallback {

		/**
		 * Flag if complete.
		 */
		private boolean isComplete = false;

		/**
		 * Response.
		 */
		private Object response = null;

		/**
		 * Failure.
		 */
		private ProcessException failure = null;

		/**
		 * Obtains the response from the command.
		 * 
		 * @return Response.
		 * @throws ProcessException
		 *             If fails.
		 */
		public synchronized Object getResponse() throws ProcessException {
			// Loop until response / failure / time out
			for (;;) {

				// Handle if complete
				if (this.isComplete) {
					// Propagate possible failure
					if (this.failure != null) {
						throw this.failure;
					}

					// Return the command response
					return response;
				}

				// Wait some time for completion
				try {
					this.wait(100);
				} catch (InterruptedException ex) {
					// Ignore
				}
			}
		}

		/*
		 * ================== TimerTask ===========================
		 */

		@Override
		public synchronized void run() {
			// Flag timed out
			this.failure = new ProcessException("Command timed out");
			this.isComplete = true;

			// Notify complete
			this.notify();
		}

		/*
		 * ================== CommandCallback ===========================
		 */

		@Override
		public synchronized void complete(Object response) {
			// Indicate complete
			this.response = response;
			this.isComplete = true;

			// Notify complete
			this.notify();
		}

		@Override
		public synchronized void failed(Throwable failure) {
			// Indicate failed
			this.failure = new ProcessException(failure);
			this.isComplete = true;

			// Notify complete
			this.notify();
		}
	}

	/**
	 * Handler for the {@link ProcessResponse}.
	 */
	private static class ProcessResponseHandler extends Thread {

		/**
		 * {@link ProcessManager}.
		 */
		private final ProcessManager processManager;

		/**
		 * {@link ServerSocket} to receive {@link ProcessResponse} instances.
		 */
		private final ServerSocket fromProcessServerSocket;

		/**
		 * Initiate.
		 * 
		 * @param processManager
		 *            {@link ProcessManager}.
		 * @param fromProcessServerSocket
		 *            {@link ServerSocket} to receive {@link ProcessResponse}
		 *            instances.
		 */
		public ProcessResponseHandler(ProcessManager processManager,
				ServerSocket fromProcessServerSocket) {
			this.processManager = processManager;
			this.fromProcessServerSocket = fromProcessServerSocket;

			// Flag as deamon (should not stop process finishing)
			this.setDaemon(true);
		}

		/*
		 * ================= Thread ======================
		 */

		@Override
		public void run() {
			try {
				// Accept only the first connection (from process)
				Socket socket = this.fromProcessServerSocket.accept();

				// Obtain pipe from process
				ObjectInputStream fromProcessPipe = new ObjectInputStream(
						socket.getInputStream());

				// Loop while still processing
				while (!this.processManager.isProcessComplete()) {

					// Read in the response
					Object object = fromProcessPipe.readObject();
					if (!(object instanceof ProcessResponse)) {
						throw new IllegalArgumentException("Unknown response: "
								+ object
								+ " ["
								+ (object == null ? "null" : object.getClass()
										.getName()));
					}
					ProcessResponse response = (ProcessResponse) object;

					// Obtain the correlating command call back for the response
					Long requestId = new Long(response
							.getCorrelatingRequestId());
					CommandCallback callback = this.processManager
							.getCallbackForCompletedCommand(requestId);

					// Ensure a correlating command for response
					if (callback != null) {
						Throwable failure = response.getFailure();
						if (failure != null) {
							// Command failed
							callback.failed(failure);
						} else {
							// Command completed
							callback.complete(response.getResponse());
						}
					}

				}
			} catch (EOFException ex) {
				// Process completed
				this.processManager.flagComplete();

			} catch (Throwable ex) {
				// Indicate failure
				ex.printStackTrace();

			} finally {
				// Close the socket as process complete
				try {
					this.fromProcessServerSocket.close();
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Gobbles the Stream.
	 */
	private static class StreamGobbler extends Thread {

		/**
		 * {@link InputStream} to gobble.
		 */
		private final InputStream stream;

		/**
		 * {@link ProcessManager} to flag if complete. May be <code>null</code>.
		 */
		private final ProcessManager processManager;

		/**
		 * Initiate.
		 * 
		 * @param stream
		 *            {@link InputStream} to gobble.
		 * @param processManager
		 *            {@link ProcessManager} to flag if complete. May be
		 *            <code>null</code>.
		 */
		public StreamGobbler(InputStream stream, ProcessManager processManager) {
			this.stream = stream;
			this.processManager = processManager;

			// Flag as deamon (should not stop process finishing)
			this.setDaemon(true);
		}

		/*
		 * ================= Thread ======================
		 */

		@Override
		public void run() {
			try {
				// Consume from stream until EOF
				for (int value = this.stream.read(); value != -1; value = this.stream
						.read()) {
					System.out.write(value);
				}
			} catch (Throwable ex) {
				// Provide stack trace and exit
				ex.printStackTrace();
			} finally {
				if (this.processManager != null) {
					// Flag process is complete
					this.processManager.flagComplete();
				}
			}
		}
	}

}