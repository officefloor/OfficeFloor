/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import com.google.gwt.dev.DevMode;

import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.building.classpath.ClassPathFactoryImpl;
import net.officefloor.building.classpath.RemoteRepository;
import net.officefloor.compile.impl.issues.CompileException;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * Tests the {@link WoofDevelopmentLauncher}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofDevelopmentLauncherTest extends OfficeFrameTestCase implements GwtLauncher {

	/**
	 * {@link MockDevMode}.
	 */
	private MockDevMode devMode;

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

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
		this.client.close();

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
	 * Ensure GWT default version is aligned to OfficeFloor configuration.
	 */
	public void testDefaultGwtVersion() throws Exception {
		String officeFloorGwtVersion = this.getGwtVersion();
		assertEquals("Not using GWT version aligned to OfficeFloor", officeFloorGwtVersion,
				WoofDevelopmentConfigurationLoader.DEFAULT_GWT_DEV_VERSION);
	}

	/**
	 * Ensure given the project <code>pom.xml</code> that able to determine the
	 * appropriate class path for GWT {@link DevMode}.
	 */
	public void testGwtOnClassPath() throws Exception {

		// Obtain the POM file (with GWT user dependency)
		File pomFile = this.findFile("pom.xml");

		// Test
		this.doGwtOnClassPath(pomFile);
	}

	/**
	 * Ensure issue raised if GWT not on class path.
	 */
	public void testGwtNotOnClasspath() throws Exception {

		// Find the POM file containing no dependencies
		File pomFile = this.findFile(this.getClass(), "NoDependencyPom.xml");

		// Test
		this.doGwtOnClassPath(pomFile);
	}

	/**
	 * Undertakes obtaining the class path for GWT.
	 * 
	 * @param pomFile
	 *            POM file.
	 */
	private void doGwtOnClassPath(File pomFile) throws Exception {

		// Obtain the GWT version
		String gwtDevVersion = this.getGwtVersion();

		// Load the class path entries
		String[] classpathEntries = WoofDevelopmentConfigurationLoader.getDevModeClassPath(pomFile);

		// Ensure gwt-dev on class path
		String gwtDevClasspathSuffix = "/" + WoofDevelopmentConfigurationLoader.GWT_DEV_ARTIFACT_ID + "-"
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
	 * Obtains the GWT version.
	 * 
	 * @return GWT version.
	 */
	private String getGwtVersion() throws Exception {

		// Obtain the POM file (with GWT user dependency)
		File pomFile = this.findFile("pom.xml");

		// Obtain the GWT version
		MavenProject project = new ClassPathFactoryImpl(null, new RemoteRepository[0]).getMavenProject(pomFile);
		String gwtDevVersion = null;
		for (Dependency dependency : project.getDependencies()) {
			String groupId = dependency.getGroupId();
			String artifactId = dependency.getArtifactId();
			if ((WoofDevelopmentConfigurationLoader.GWT_GROUP_ID.equals(groupId))
					&& (WoofDevelopmentConfigurationLoader.GWT_DEV_ARTIFACT_ID.equals(artifactId))) {
				gwtDevVersion = dependency.getVersion();
			}
		}
		assertNotNull("Ensure have GWT version", gwtDevVersion);

		// Return the GWT version
		return gwtDevVersion;
	}

	/**
	 * Ensure the application runs so remaining test focus on GWT
	 * {@link DevMode} integration.
	 */
	public void testEnsureApplicationRuns() throws Exception {
		try {
			// Run application
			WoofOfficeFloorSource.start();

			// Ensure template available
			this.doHttpRequest(7878, "Test.woof", "TEMPLATE");

			// Ensure section available
			this.doHttpRequest(7878, "section", "SECTION");

		} finally {
			// Ensure stop
			WoofOfficeFloorSource.stop();
		}
	}

	/**
	 * Ensure can start the embedded WoOF server within {@link DevMode}.
	 */
	public void testEmbeddedWoofServer() throws Exception {

		// Launch the application with default configuration
		this.launchApplication(null, "net.officefloor.launch.woof.Test", "/Test.woof", "/section");

		// Ensure template available
		this.doHttpRequest(8888, "Test.woof", "TEMPLATE");

		// Ensure section available
		this.doHttpRequest(8888, "section", "SECTION");

		// Ensure able to obtain resource
		this.doHttpRequest(8888, "classpath.html", "CLASSPATH");
	}

	/**
	 * Ensure the application with no GWT runs.
	 */
	public void testEnsureNoGwtRuns() throws Exception {
		try {
			// Run application
			WoofOfficeFloorSource.start(WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION, "noGwt.woof");

			// Ensure template available
			this.doHttpRequest(7878, "noGwt.woof", "TEMPLATE");

		} finally {
			// Ensure clean up and stop
			WoofOfficeFloorSource.stop();
		}
	}

	/**
	 * Ensure can start the embedded WoOF server within {@link DevMode} that has
	 * no GWT modules.
	 */
	public void testEmbeddedNoGwtWoofServer() throws Exception {

		// Launch the application with alternate configuration
		this.launchApplication("noGwt.woof", "net.officefloor.launch.NoGwt", "/noGwt.woof");

		// Ensure start alternate configuration
		this.doHttpRequest(8888, "noGwt.woof", "TEMPLATE");
	}

	/**
	 * Ensure the application with invalid WoOF configuration.
	 */
	public void testEnsureInvalidWoof() throws Exception {
		try {
			// Run application
			WoofOfficeFloorSource.main(WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION, "invalid.woof");

			// Ensure did not successfully start (as invalid)
			fail("Should not be valid");

		} catch (CompileException ex) {
			// Cause should be class not found
			assertTrue("Invalid cause", ex.getCompilerIssue().getCause() instanceof ClassNotFoundException);

		} finally {
			// Ensure clean up and stop
			AutoWireManagement.closeAllOfficeFloors();
		}
	}

	/**
	 * Ensure can handle invalid embedded WoOF server within {@link DevMode}.
	 */
	public void testEmbeddedInvalidWoofServer() throws Exception {

		// Launch the application with alternate configuration
		this.launchApplication("invalid.woof", "net.officefloor.launch.NoGwt", "/invalid.woof");

	}

	/**
	 * Launches the application.
	 * 
	 * @param woofFileName
	 *            WoOF file name. May be <code>null</code> for default
	 *            configuration.
	 * @param gwtModuleName
	 *            GWT Module name.
	 * @param expectedStartupUrls
	 *            Expected startup URLs.
	 */
	private void launchApplication(String woofFileName, String gwtModuleName, String... expectedStartupUrls)
			throws Exception {

		// Re-use test class path for running

		// Default the WoOF file name (if required)
		boolean isDefaultWoofConfiguration = false;
		if (woofFileName == null) {
			// Default WoOF configuration
			isDefaultWoofConfiguration = true;
			woofFileName = "application.woof";
		}

		// Create the WAR directory
		File warDirectory = new File(System.getProperty("java.io.tmpdir"),
				this.getClass().getSimpleName() + "-" + this.getName());
		if (warDirectory.exists()) {
			this.deleteDirectory(warDirectory);
		}
		assertTrue("Ensure WAR directory available", warDirectory.mkdir());

		// Create configuration for running (keeps command smaller)
		File woofFile = this.findFile(woofFileName);
		WoofDevelopmentConfiguration configuration = WoofDevelopmentConfigurationLoader
				.loadConfiguration(new FileInputStream(woofFile));
		configuration.setWarDirectory(warDirectory);

		// Provide alternate WoOF configuration
		if (!isDefaultWoofConfiguration) {
			configuration.addProperty(WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION, woofFileName);
		}

		// Store the configuration
		File configurationFile = new File(warDirectory, WoofServletContainerLauncher.CONFIGURATION_FILE_NAME);
		configuration.storeConfiguration(configurationFile);

		// Run the dev mode (with WoOF embedded server)
		WoofDevelopmentLauncher.main(configurationFile.getAbsolutePath());

		// Validate the arguments for dev mode
		List<String> expectedArgumentList = new LinkedList<String>();
		expectedArgumentList.addAll(Arrays.asList("-server", WoofServletContainerLauncher.class.getName(), "-war",
				warDirectory.getAbsolutePath()));
		for (String startupUrl : expectedStartupUrls) {
			expectedArgumentList.addAll(Arrays.asList("-startupUrl", startupUrl));
		}
		expectedArgumentList.add(gwtModuleName);
		String[] expectedArguments = expectedArgumentList.toArray(new String[expectedArgumentList.size()]);
		assertEquals("Incorrect number of arguments", expectedArguments.length, this.devMode.arguments.length);
		for (int i = 0; i < expectedArguments.length; i++) {
			assertEquals("Incorrect argument " + i, expectedArguments[i], this.devMode.arguments[i]);
		}

		// Allow some time for GWT development to start up
		Thread.sleep(5000);
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
	private void doHttpRequest(int port, String templateUri, String expectedResponseContent) throws IOException {

		// Ensure able to call application
		HttpResponse httpResponse = this.client.execute(new HttpGet("http://localhost:" + port + "/" + templateUri));
		assertEquals("Ensure successful", 200, httpResponse.getStatusLine().getStatusCode());

		// Validate appropriate response entity
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		httpResponse.getEntity().writeTo(buffer);
		String responseText = new String(buffer.toByteArray());
		assertTrue("Incorrect response as not contain expected content (content=" + expectedResponseContent
				+ ", response=" + responseText + ")", responseText.contains(expectedResponseContent));
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

			// Run GWT on unique port (to stop conflicts)
			List<String> argumentsList = new ArrayList<String>(this.arguments.length + 2);
			argumentsList.addAll(Arrays.asList("-codeServerPort", String.valueOf(HttpTestUtil.getAvailablePort())));
			argumentsList.addAll(Arrays.asList(this.arguments));

			// By default run head less (for continuous integration)
			boolean isHeadless = Boolean.parseBoolean(System.getProperty("hide.gwt.devmode", String.valueOf(true)));
			this.setHeadless(isHeadless);

			// Process arguments and if successful run
			if (new ArgProcessor(this.options).processArgs(argumentsList.toArray(new String[argumentsList.size()]))) {
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