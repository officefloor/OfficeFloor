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

	/*
	 * ================== AbstractMojo ========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// TODO implement
		this.getLog().info("TODO implement stopping SAM");
	}

}
