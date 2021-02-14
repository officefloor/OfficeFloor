/*-
 * #%L
 * OfficeFloor SAM Maven Plugin
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
import java.io.InputStreamReader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Runs SAM for the manual testing.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE)
public class RunSamMojo extends AbstractStartSamMojo {

	/*
	 * ================== AbstractMojo ========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Start SAM
		Runnable stop = this.startSam();

		// Indicate need to press end to stop
		try {
			Thread.sleep(2000);
			this.getLog()
					.info("\n\nServer available at http://localhost:" + this.samPort + "\n\nPress [enter] to stop\n\n");
			new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed waiting on run", ex);
		}

		// Stop
		stop.run();
	}

}
