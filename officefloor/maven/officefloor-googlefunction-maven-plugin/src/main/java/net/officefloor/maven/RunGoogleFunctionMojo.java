package net.officefloor.maven;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * {@link Mojo} to run Google Function.
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE)
public class RunGoogleFunctionMojo extends StartGoogleFunctionMojo {

	/*
	 * ==================== Mojo ====================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Start Google Function
		super.execute();

		// Indicate need to press end to stop
		try {
			Thread.sleep(2000);
			this.getLog().info(
					"\n\nServer available at http://localhost:" + this.httpPort + "\n\nPress [enter] to stop\n\n");
			new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed waiting on run", ex);
		} finally {
			// Stop
			StopGoogleFunctionMojo.closeOfficeFloor(this.jmxPort);
		}
	}

}
