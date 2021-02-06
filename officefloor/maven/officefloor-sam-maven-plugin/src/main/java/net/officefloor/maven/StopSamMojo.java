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
