/*-
 * #%L
 * OfficeFloor SAM Maven Plugin
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
