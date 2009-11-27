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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages a {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessManager {

	/**
	 * {@link ManagedProcess} to be run in its own process.
	 */
	private final ManagedProcess managedProcess;

	/**
	 * Invoked {@link Process} for the {@link ManagedProcess}.
	 */
	private Process process;

	/**
	 * Flag indicating if the {@link Process} is complete.
	 */
	private volatile boolean isComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param managedProcess
	 *            {@link ManagedProcess} to be run in its own process.
	 */
	public ProcessManager(ManagedProcess managedProcess) {
		this.managedProcess = managedProcess;
	}

	/**
	 * <p>
	 * Starts the process to run the {@link ManagedProcess}.
	 * <p>
	 * This only triggers starting the process.
	 * 
	 * @throws ProcessException
	 *             If fails to start the {@link ManagedProcess}.
	 */
	public void startProcess() throws ProcessException {

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
			javaExecutable = new File(javaBinDir, "java.exe");
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

		// Invoke the process
		ProcessBuilder builder = new ProcessBuilder(command);
		try {
			this.process = builder.start();
		} catch (IOException ex) {
			throw new ProcessException(ex);
		}

		// Gobble the process's stdout and stderr
		new StreamGobbler(this.process.getInputStream(), true).start();
		new StreamGobbler(this.process.getErrorStream(), false).start();

		try {
			// Write the managed process to the process to run
			ObjectOutputStream output = new ObjectOutputStream(this.process
					.getOutputStream());
			output.writeObject(this.managedProcess);
			output.flush();
		} catch (IOException ex) {
			// Propagate failure
			throw new ProcessException(ex);
		}
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
	 * Gobbles the Stream.
	 */
	private class StreamGobbler extends Thread {

		/**
		 * {@link InputStream} to gobble.
		 */
		private final InputStream stream;

		/**
		 * Indicates if should flag the {@link Process} is complete.
		 */
		private final boolean isFlagComplete;

		/**
		 * Initiate.
		 * 
		 * @param stream
		 *            {@link InputStream} to gobble.
		 * @param isFlagComplete
		 *            Indicates if should flag the {@link Process} is complete.
		 */
		public StreamGobbler(InputStream stream, boolean isFlagComplete) {
			this.stream = stream;
			this.isFlagComplete = isFlagComplete;
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
				if (this.isFlagComplete) {
					// Flag process is complete
					ProcessManager.this.isComplete = true;
				}
			}
		}
	}

}