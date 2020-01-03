/*-
 * #%L
 * Maven OfficeFloor Plugin
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import net.officefloor.OfficeFloorMain;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Open {@link OfficeFloor} {@link Mojo}.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "open", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class OpenOfficeFloorMojo extends AbstractMojo {

	/**
	 * Default JMX port.
	 */
	public static final int DEFAULT_JMX_PORT = 7777;

	/**
	 * Default time out in seconds to start {@link OfficeFloor}.
	 */
	private static final int DEFAULT_TIMEOUT = 60;

	/**
	 * {@link MavenProject}.
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * System properties provided to JVM.
	 */
	@Parameter(required = false)
	private Map<String, String> systemProperties;

	/**
	 * JMX port.
	 */
	@Parameter(required = false, defaultValue = "" + DEFAULT_JMX_PORT)
	private int jmxPort = DEFAULT_JMX_PORT;

	/**
	 * Time out in seconds for starting {@link OfficeFloor}.
	 */
	@Parameter(required = false, defaultValue = "" + DEFAULT_TIMEOUT)
	private int timeout = DEFAULT_TIMEOUT;

	/**
	 * {@link Process} running {@link OfficeFloor}.
	 */
	private Process process = null;

	/**
	 * Obtains the {@link Process} running {@link OfficeFloor}.
	 * 
	 * @return {@link Process} running {@link OfficeFloor}. May be
	 *         <code>null</code>.
	 */
	public Process getProcess() {
		return this.process;
	}

	/*
	 * =================== AbstractMojo =================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Should be able to start
		try {

			// Obtain details to run
			String javaHome = System.getProperty("java.home");
			String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

			// Generate the class path
			StringBuilder classPath = new StringBuilder();
			boolean isFirst = true;
			for (String classPathEntry : this.project.getRuntimeClasspathElements()) {
				if (!isFirst) {
					classPath.append(File.pathSeparator);
				}
				isFirst = false;
				classPath.append(classPathEntry);
			}

			// Create the command line
			List<String> commandLine = new LinkedList<>();
			commandLine.add(javaBin);
			commandLine.add("-cp");
			commandLine.add(classPath.toString());
			commandLine.add("-Dcom.sun.management.jmxremote.port=" + this.jmxPort);
			commandLine.add("-Dcom.sun.management.jmxremote.authenticate=false");
			commandLine.add("-Dcom.sun.management.jmxremote.ssl=false");
			if (this.systemProperties != null) {
				for (String name : this.systemProperties.keySet()) {

					// Ensure system property is valid
					if (name.startsWith("com.sun.management.jmxremote.")) {
						throw new MojoExecutionException(
								"JMX configuration managed by officefloor-maven-plugin.  Can not configure property "
										+ name);
					}
					String value = this.systemProperties.get(name);

					// Add the system property
					commandLine.add("-D" + name.trim() + "=" + value.trim());
				}
			}
			commandLine.add(OfficeFloorMain.class.getName());

			// Log the command line
			this.getLog().debug("Running OfficeFloor with: " + String.join(" ", commandLine));

			// Build and start the process
			ProcessBuilder builder = new ProcessBuilder(commandLine.toArray(new String[commandLine.size()]));
			this.process = builder.start();

			// Gobble streams (to log and avoid buffer filling)
			StdOutStreamGobbler stdout = new StdOutStreamGobbler(this.process.getInputStream(),
					OfficeFloorMain.STD_OUT_RUNNING_LINE);
			stdout.start();
			new StreamGobbler(this.process.getErrorStream(), true).start();

			// Wait on OfficeFloor to start
			stdout.waitForOutputLine();

		} catch (Exception ex) {
			if (ex instanceof MojoExecutionException) {
				throw (MojoExecutionException) ex;
			} else if (ex instanceof MojoFailureException) {
				throw (MojoFailureException) ex;
			}
			throw new MojoFailureException("Failed to open " + OfficeFloor.class.getSimpleName(), ex);
		}
	}

	/**
	 * Gobbles the Stream.
	 */
	private class StreamGobbler extends Thread {

		/**
		 * {@link InputStream} to gobble.
		 */
		private final BufferedReader input;

		/**
		 * Indicates if error.
		 */
		private final boolean isError;

		/**
		 * {@link Log} to log output.
		 */
		private final Log logger;

		/**
		 * Initiate.
		 * 
		 * @param input   {@link InputStream} to gobble.
		 * @param isError Indicates if error.
		 */
		private StreamGobbler(InputStream input, boolean isError) {
			this.input = new BufferedReader(new InputStreamReader(input));
			this.isError = isError;

			// Obtain the logger
			this.logger = OpenOfficeFloorMojo.this.getLog();

			// Flag as deamon (should not stop process finishing)
			this.setDaemon(true);
		}

		/**
		 * Handles the output line.
		 * 
		 * @param outputLine Output line.
		 */
		protected void handleOutputLine(String outputLine) {
			if (this.isError) {
				this.logger.error(outputLine);
			} else {
				this.logger.info(outputLine);
			}
		}

		/**
		 * Handles end of stream.
		 * 
		 * @param ex Possible failure of stream.
		 */
		protected void streamEnd(Throwable ex) {
		}

		/*
		 * ================= Thread ======================
		 */

		@Override
		public void run() {
			try {

				// Consume from stream until EOF
				String line;
				while ((line = this.input.readLine()) != null) {
					this.handleOutputLine(line);
				}

			} catch (Throwable ex) {
				// Gracefully handle end of stream
				this.streamEnd(ex);
			}
		}
	}

	/**
	 * Waits for {@link OfficeFloorMain} to be running.
	 */
	private class StdOutStreamGobbler extends StreamGobbler {

		/**
		 * Line to wait on to indicate ready.
		 */
		private final String waitLine;

		/**
		 * Indicates if the wait line has been output.
		 */
		private boolean isWaitLineOutput = false;

		/**
		 * Possible failure in running process.
		 */
		private Throwable failure = null;

		/**
		 * Waits for the output line.
		 * 
		 * @throws MojoExecutionException If error in waiting on output line. @throws
		 *                                MojoFailureException If times out waiting on
		 *                                output line..
		 */
		private synchronized void waitForOutputLine() throws MojoExecutionException, MojoFailureException {

			// Wait for line to be output (or time out)
			long startTime = System.currentTimeMillis();
			do {

				// Determine if error
				if (this.failure != null) {
					throw new MojoExecutionException("Failed to start " + OfficeFloor.class.getSimpleName(),
							this.failure);
				}

				// Determine if time out
				if ((startTime + (OpenOfficeFloorMojo.this.timeout * 1000)) < System.currentTimeMillis()) {
					throw new MojoFailureException(
							"Timed out waiting on " + OfficeFloor.class.getSimpleName() + " to start");
				}

				// Wait some time
				try {
					this.wait(100);
				} catch (InterruptedException ex) {
					throw new MojoExecutionException("Wait on " + OfficeFloor.class.getSimpleName() + " interrupted",
							ex);
				}

			} while (!this.isWaitLineOutput);
		}

		/**
		 * Instantiate.
		 * 
		 * @param input    {@link InputStream} to stdout to gobble.
		 * @param waitLine Line to wait on to indicate ready.
		 */
		private StdOutStreamGobbler(InputStream input, String waitLine) {
			super(input, false);
			this.waitLine = waitLine;
		}

		@Override
		protected void handleOutputLine(String outputLine) {

			// Determine if the wait line
			if ((this.waitLine != null) && (this.waitLine.equals(outputLine))) {
				synchronized (this) {
					this.isWaitLineOutput = true;
					this.notify();
				}
			}

			// Continue to log output
			super.handleOutputLine(outputLine);
		}

		@Override
		protected synchronized void streamEnd(Throwable ex) {
			this.failure = ex;
			this.notify();
		}
	}

}
