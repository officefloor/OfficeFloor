/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.launch.woof;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.building.classpath.ClassPathFactoryImpl;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.DefaultPlexusContainer;

import com.google.gwt.dev.DevMode;

/**
 * Tests the {@link WoofDevelopmentLauncher}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofDevelopmentLauncherTest extends OfficeFrameTestCase implements
		GwtLauncher {

	/**
	 * {@link MockDevMode}.
	 */
	private MockDevMode devMode;

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	@Override
	protected void setUp() throws Exception {
		// Set test as launcher to avoid System.exit(int) calls
		WoofDevelopmentLauncher.setGwtLauncher(this);
	}

	@Override
	protected void tearDown() throws Exception {

		// Clear launcher
		WoofDevelopmentLauncher.setGwtLauncher(null);

		// Stop HTTP client
		this.client.getConnectionManager().shutdown();

		// Stop GWT
		if (this.devMode != null) {
			this.devMode.onDone();
		}
	}

	/**
	 * Ensure correct {@link DevMode} class name.
	 */
	public void testCorrectDevModeClassName() {
		assertEquals("Incorrect GWT main class", DevMode.class.getName(),
				WoofDevelopmentLauncher.DEFAULT_GWT_MAIN_CLASS_NAME);
	}

	/**
	 * Ensure given the project <code>pom.xml</code> that able to determine the
	 * appropriate class path for GWT {@link DevMode}.
	 */
	public void testGwtOnClassPath() throws Exception {

		// Obtain the POM file (with GWT user dependency)
		File pomFile = this.findFile("pom.xml");

		// Obtain the expected version
		MavenProject project = new ClassPathFactoryImpl(
				new DefaultPlexusContainer(), null).getMavenProject(pomFile);
		String gwtDevVersion = null;
		for (Dependency dependency : project.getDependencies()) {
			String groupId = dependency.getGroupId();
			String artifactId = dependency.getArtifactId();
			if ((WoofDevelopmentConfigurationLoader.GWT_GROUP_ID
					.equals(groupId))
					&& (WoofDevelopmentConfigurationLoader.GWT_DEV_ARTIFACT_ID
							.equals(artifactId))) {
				gwtDevVersion = dependency.getVersion();
			}
		}
		assertNotNull("Ensure have GWT version", gwtDevVersion);

		// Load the class path entries
		String[] classpathEntries = WoofDevelopmentConfigurationLoader
				.getDevModeClassPath(pomFile);

		// Ensure gwt-dev on class path
		String gwtDevClasspathSuffix = "/"
				+ WoofDevelopmentConfigurationLoader.GWT_DEV_ARTIFACT_ID + "-"
				+ gwtDevVersion + ".jar";
		boolean isGwtDevOnClasspath = false;
		for (String classpathEntry : classpathEntries) {
			if (classpathEntry.endsWith(gwtDevClasspathSuffix)) {
				isGwtDevOnClasspath = true;
			}
		}
		assertTrue("gwt-dev not found on the class path", isGwtDevOnClasspath);
	}

	/**
	 * Ensure issue raised if GWT not on class path.
	 */
	public void testGwtNotOnClasspath() throws Exception {

		// Find the POM file containing no dependencies
		File pomFile = this.findFile(this.getClass(), "NoDependencyPom.xml");

		// Ensure issue as GWT not referenced by POM file
		try {
			WoofDevelopmentConfigurationLoader.getDevModeClassPath(pomFile);
			fail("Should not be successful in obtaining the class path");
		} catch (Exception ex) {
			// Ensure correct cause
			assertEquals(
					"Incorrect cause",
					"Can not find GWT dependency com.google.gwt:gwt-user within project's POM file. Necessary to determine GWT version to use.",
					ex.getMessage());
		}
	}

	/**
	 * Ensure the application runs so remaining test focus on GWT
	 * {@link DevMode} integration.
	 */
	public void testEnsureApplicationRuns() throws Exception {
		try {
			// Run application
			WoofOfficeFloorSource.main();

			// Ensure template available
			this.doHttpRequest(7878, "template", "TEMPLATE");

			// Ensure section available
			this.doHttpRequest(7878, "section", "SECTION");

		} finally {
			// Ensure stop
			AutoWireManagement.closeAllOfficeFloors();
		}
	}

	/**
	 * Ensure can start the embedded WoOF server within {@link DevMode}.
	 */
	public void testEmbeddedWoofServer() throws Exception {

		// Re-use test class path for running

		// Create the WAR directory
		File warDirectory = new File(System.getProperty("java.io.tmpdir"), this
				.getClass().getSimpleName() + "-" + this.getName());
		if (warDirectory.exists()) {
			this.deleteDirectory(warDirectory);
		}
		assertTrue("Ensure WAR directory available", warDirectory.mkdir());

		// Create configuration for running (keeps command smaller)
		File woofFile = this.findFile("application.woof");
		WoofDevelopmentConfiguration configuration = WoofDevelopmentConfigurationLoader
				.loadConfiguration(new FileInputStream(woofFile));
		configuration.setWarDirectory(warDirectory);

		// Store the configuration
		File configurationFile = new File(warDirectory,
				WoofServletContainerLauncher.CONFIGURATION_FILE_NAME);
		configuration.storeConfiguration(configurationFile);

		// Run the dev mode (with WoOF embedded server)
		String configurationFilePath = configurationFile.getAbsolutePath();
		WoofDevelopmentLauncher.main(configurationFilePath);

		// Validate the arguments
		final String[] expectedArguments = new String[] { "-server",
				WoofServletContainerLauncher.class.getName(), "-war",
				warDirectory.getAbsolutePath(), "-startupUrl", "/template",
				"-startupUrl", "/section", "net.officefloor.launch.woof.Test" };
		assertEquals("Incorrect number of arguments", expectedArguments.length,
				this.devMode.arguments.length);
		for (int i = 0; i < expectedArguments.length; i++) {
			assertEquals("Incorrect argument " + i, expectedArguments[i],
					this.devMode.arguments[i]);
		}

		// Allow some time for GWT development to start up
		Thread.sleep(5000);

		// Ensure template available
		this.doHttpRequest(8888, "template", "TEMPLATE");

		// Ensure section available
		this.doHttpRequest(8888, "section", "SECTION");

		// Ensure able to obtain resource
		this.doHttpRequest(8888, "classpath.html", "CLASSPATH");
	}

	/**
	 * Undertakes the {@link HttpRequest} validating the {@link HttpResponse}.
	 * 
	 * @param port
	 *            Port.
	 * @param templateUri
	 *            Template URI.
	 * @param expectedResponseContent
	 *            Expected content in the {@link HttpResponse} entity.
	 */
	private void doHttpRequest(int port, String templateUri,
			String expectedResponseContent) throws IOException {

		// Ensure able to call application
		HttpResponse httpResponse = this.client.execute(new HttpGet(
				"http://localhost:" + port + "/" + templateUri));
		assertEquals("Ensure successful", 200, httpResponse.getStatusLine()
				.getStatusCode());

		// Validate appropriate response entity
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		httpResponse.getEntity().writeTo(buffer);
		String responseText = new String(buffer.toByteArray());
		assertTrue(
				"Incorrect response as not contain expected content (content="
						+ expectedResponseContent + ", response="
						+ responseText + ")",
				responseText.contains(expectedResponseContent));
	}

	/*
	 * =================== GwtLauncher ============================
	 */

	@Override
	public void launch(String... arguments) throws Exception {

		/*
		 * Runs the {@link DevMode} in similar way to its main method except
		 * without System.exit(int) calls to disrupt unit testing.
		 */
		this.devMode = new MockDevMode();
		this.devMode.mockMain(arguments);
	}

	/**
	 * Mock {@link DevMode} to provide access to necessary class members for
	 * unit testing.
	 */
	private class MockDevMode extends DevMode {

		/**
		 * Main arguments.
		 */
		public String[] arguments;

		/**
		 * Mock main for testing.
		 * 
		 * @param arguments
		 *            Arguments.
		 */
		public void mockMain(String[] arguments) {

			// Specify arguments
			this.arguments = arguments;

			// By default run head less (for continuous integration)
			boolean isHeadless = Boolean.parseBoolean(System.getProperty(
					"hide.gwt.devmode", String.valueOf(true)));
			this.setHeadless(isHeadless);

			// Process arguments and if successful run
			if (new ArgProcessor(this.options).processArgs(arguments)) {
				// Run in new thread as blocks until complete
				new Thread() {
					@Override
					public void run() {
						MockDevMode.this.run();
					}
				}.start();
			}
		}
	}

}