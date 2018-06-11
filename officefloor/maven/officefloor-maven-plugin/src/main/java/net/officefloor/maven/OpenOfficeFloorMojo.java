/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

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

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

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
				isFirst = true;
				classPath.append(classPathEntry);
			}

			// Build and start the process
			ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classPath.toString(),
					OfficeFloorMain.class.getName());
			Process process = builder.start();

			// Gobble streams (to log and avoid buffer filling)
			new StreamGobbler(process.getInputStream(), false).start();
			new StreamGobbler(process.getErrorStream(), true).start();

		} catch (Exception ex) {
			throw new MojoExecutionException("Failed to open " + OfficeFloor.class.getSimpleName(), ex);
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
		 * Initiate.
		 * 
		 * @param input
		 *            {@link InputStream} to gobble.
		 * @param isError
		 *            Indicates if error.
		 */
		private StreamGobbler(InputStream input, boolean isError) {
			this.input = new BufferedReader(new InputStreamReader(input));
			this.isError = isError;

			// Flag as deamon (should not stop process finishing)
			this.setDaemon(true);
		}

		/*
		 * ================= Thread ======================
		 */

		@Override
		public void run() {
			try {

				// Obtain the logger
				Log log = OpenOfficeFloorMojo.this.getLog();

				// Consume from stream until EOF
				String line;
				while ((line = this.input.readLine()) != null) {
					if (this.isError) {
						log.error(line);
					} else {
						log.info(line);
					}
				}

			} catch (Throwable ex) {
				// Gracefully handle end of stream
			}
		}
	}

}