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
