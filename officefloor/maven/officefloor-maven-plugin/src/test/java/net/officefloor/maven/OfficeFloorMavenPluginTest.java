/*-
 * #%L
 * Maven OfficeFloor Plugin
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.MojoRule;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.compile.mbean.OfficeFloorMBean;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpClientTestUtil;

/**
 * Tests the {@link OpenOfficeFloorMojo} and {@link CloseOfficeFloorMojo}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMavenPluginTest {

	/**
	 * {@link MojoRule}.
	 */
	@Rule
	public final MojoRule mojo = new MojoRule(new AbstractMojoTestCase() {

		@Override
		public Mojo lookupMojo(String goal, File pluginPom) throws Exception {

			// Read in the version
			File versionFile = new File("target/officefloor-version");
			assertTrue("Please run mvn generate-sources to create OfficeFloor version file", versionFile.exists());
			StringWriter version = new StringWriter();
			try (Reader reader = new FileReader(versionFile)) {
				for (int character = reader.read(); character != -1; character = reader.read()) {
					version.write(character);
				}
			}

			// Look up the mojo
			assertTrue(pluginPom.getAbsolutePath() + " not exists", pluginPom.exists());
			PlexusConfiguration pluginConfiguration = this.extractPluginConfiguration("officefloor-maven-plugin",
					pluginPom);
			return this.lookupMojo("net.officefloor.maven", "officefloor-maven-plugin", version.toString(), goal,
					pluginConfiguration);
		}
	});

	/**
	 * URL to {@link OfficeFloor}.
	 */
	private static final String url = "http://localhost:7878/available";

	/**
	 * {@link OpenOfficeFloorMojo}.
	 */
	private OpenOfficeFloorMojo open;

	@After
	public void tearDown() throws Exception {

		// Ensure kill process (so no clash between tests)
		if (this.open != null) {
			Process process = this.open.getProcess();
			if (process != null) {
				process.destroy();
			}
		}
	}

	/**
	 * Ensure disallow configuring JMX.
	 */
	@Test
	public void disallowJmxConfiguration() throws Exception {

		this.open = (OpenOfficeFloorMojo) this.mojo.lookupMojo("open",
				new File(PlexusTestCase.getBasedir(), "src/test/resources/test-jmx-pom.xml"));

		// Attempt to open OfficeFloor
		try {
			this.open.execute();
			fail("Should not be successful");

		} catch (MojoExecutionException ex) {
			assertEquals("Incorrect cause",
					"JMX configuration managed by officefloor-maven-plugin.  Can not configure property com.sun.management.jmxremote.port",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can open and close the {@link OfficeFloor}.
	 */
	@Test
	public void openCloseOfficeFloor() throws Exception {

		// Open the OfficeFloor
		this.openOfficeFloor("test-open-pom.xml");

		// Ensure can access OfficeFloor
		ensureOfficeFloorAvailable();

		// Close the OfficeFloor
		this.closeOfficeFloor("test-close-pom.xml");

		// Ensure no further access to OfficeFloor
		ensureOfficeFloorNotAvailable();
	}

	/**
	 * Ensure open and close on different JMX port.
	 */
	@Test
	public void differentJmxPort() throws Exception {

		// Open the OfficeFloor
		this.openOfficeFloor("test-open-9999-pom.xml");

		// Ensure OfficeFloor available
		ensureOfficeFloorAvailable();

		// Ensure running on different JMX port
		OfficeFloorMBean mbean = CloseOfficeFloorMojo.getOfficeFloorBean(9999);
		assertEquals("Should have Office", "OFFICE", mbean.getOfficeNames()[0]);

		// Close the OfficeFloor
		this.closeOfficeFloor("test-close-9999-pom.xml");

		// Ensure no further access to OfficeFloor
		ensureOfficeFloorNotAvailable();
	}

	/**
	 * Ensure handle failure to open {@link OfficeFloor}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void failOpenOfficeFloor() throws Exception {

		this.open = (OpenOfficeFloorMojo) this.mojo.lookupMojo("open",
				new File(PlexusTestCase.getBasedir(), "src/test/resources/test-fail-pom.xml"));

		// Attempt to open OfficeFloor
		try {
			this.open.execute();
			fail("Should not be successful");

		} catch (MojoExecutionException ex) {
			String cause = ex.getMessage();
			assertTrue("Should prefix failure to open: " + cause, cause.startsWith("OfficeFloor failed to open. "));
			assertTrue("Should contain failure details: " + cause,
					cause.contains(FailOfficeFloorOpen.FAIL_OPEN_MESSAGE));
		}
	}

	/**
	 * Opens {@link OfficeFloor}.
	 * 
	 * @param pomFileName Name of the pom file to use.
	 */
	private void openOfficeFloor(String pomFileName) throws Exception {

		// Open the OfficeFloor
		this.open = (OpenOfficeFloorMojo) this.mojo.lookupMojo("open",
				new File(PlexusTestCase.getBasedir(), "src/test/resources/" + pomFileName));
		open.execute();
	}

	/**
	 * Closes {@link OfficeFloor}.
	 * 
	 * @param pomFileName Name of the pom file to use.
	 */
	private void closeOfficeFloor(String pomFileName) throws Exception {

		// Close the OfficeFloor
		CloseOfficeFloorMojo close = (CloseOfficeFloorMojo) this.mojo.lookupMojo("close",
				new File(PlexusTestCase.getBasedir(), "src/test/resources/" + pomFileName));
		close.execute();
	}

	/**
	 * Ensures {@link OfficeFloor} is available.
	 */
	private static void ensureOfficeFloorAvailable() throws Exception {

		// Ensure can access OfficeFloor
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet(url));
			assertEquals("Should successful contact OfficeFloor", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect response", "AVAILABLE", EntityUtils.toString(response.getEntity()));
		}
	}

	/**
	 * Ensures {@link OfficeFloor} is NOT available.
	 */
	private static void ensureOfficeFloorNotAvailable() throws Exception {

		// Ensure can access OfficeFloor
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			client.execute(new HttpGet(url));
			fail("Should not successfully obtain connection");

		} catch (IOException ex) {
			assertTrue("Should be connection refused: " + ex.getMessage(),
					ex.getMessage().startsWith("Connect to") && ex.getMessage().contains("failed: Connection refused"));
		}
	}

}
