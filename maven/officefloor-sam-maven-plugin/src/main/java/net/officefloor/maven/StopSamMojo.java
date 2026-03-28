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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stops SAM for the integration testing.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "stop")
public class StopSamMojo extends AbstractMojo {

	/**
	 * {@link Runnable} to stop SAM.
	 */
	private static Runnable stop = null;

	/**
	 * Sets the stop SAM.
	 * 
	 * @param stopRunnable {@link Runnable} to stop SAM.
	 */
	static void setStop(Runnable stopRunnable) {
		stop = stopRunnable;
	}

	/*
	 * ================== AbstractMojo ========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (stop != null) {
				stop.run();
			}
		} finally {
			// Ensure clear to avoid repeated stop
			stop = null;
		}
	}

}
