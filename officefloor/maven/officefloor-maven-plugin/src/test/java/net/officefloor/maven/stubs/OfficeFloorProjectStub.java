package net.officefloor.maven.stubs;

import java.io.File;
import java.util.Arrays;

import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.project.MavenProject;

/**
 * Mock {@link MavenProject}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorProjectStub extends MavenProjectStub {

	/**
	 * Instantiate.
	 */
	public OfficeFloorProjectStub() {

		// Load the runtime class
		String javaClassPath = System.getProperty("java.class.path");
		this.setRuntimeClasspathElements(Arrays.asList(javaClassPath.split(File.pathSeparator)));
	}

}