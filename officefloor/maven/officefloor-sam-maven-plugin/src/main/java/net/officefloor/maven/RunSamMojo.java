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
