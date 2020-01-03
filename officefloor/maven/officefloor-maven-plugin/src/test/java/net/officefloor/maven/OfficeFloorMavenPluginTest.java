/*-
 * #%L
 * Maven OfficeFloor Plugin
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.officefloor.compile.mbean.OfficeFloorMBean;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpClientTestUtil;

/**
 * Tests the {@link OpenOfficeFloorMojo} and {@link CloseOfficeFloorMojo}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMavenPluginTest extends AbstractMojoTestCase {

	/**
	 * URL to {@link OfficeFloor}.
	 */
	private static final String url = "http://localhost:7878/available";

	/**
	 * {@link OpenOfficeFloorMojo}.
	 */
	private OpenOfficeFloorMojo open;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Clean up
			super.tearDown();

		} finally {
			// Ensure kill process (so no clash between tests)
			Process process = this.open.getProcess();
			if (process != null) {
				process.destroy();
			}
		}
	}

	/**
	 * Ensure disallow configuring JMX.
	 */
	public void testDisallowJmxConfiguration() throws Exception {

		this.open = (OpenOfficeFloorMojo) this.lookupMojo("open",
				new File(getBasedir(), "src/test/resources/test-jmx-pom.xml"));

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
	public void testOpenCloseOfficeFloor() throws Exception {

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
	public void testDifferentJmxPort() throws Exception {

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
	 * Opens {@link OfficeFloor}.
	 * 
	 * @param pomFileName Name of the pom file to use.
	 */
	private void openOfficeFloor(String pomFileName) throws Exception {

		// Open the OfficeFloor
		this.open = (OpenOfficeFloorMojo) this.lookupMojo("open",
				new File(getBasedir(), "src/test/resources/" + pomFileName));
		open.execute();
	}

	/**
	 * Closes {@link OfficeFloor}.
	 * 
	 * @param pomFileName Name of the pom file to use.
	 */
	private void closeOfficeFloor(String pomFileName) throws Exception {

		// Close the OfficeFloor
		CloseOfficeFloorMojo close = (CloseOfficeFloorMojo) this.lookupMojo("close",
				new File(getBasedir(), "src/test/resources/" + pomFileName));
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

	@Override
	protected Mojo lookupMojo(String goal, File pluginPom) throws Exception {

		// Read in the version
		File versionFile = new File(getBasedir(), "target/officefloor-version");
		assertTrue("Please run mvn generate-sources to create OfficeFloor version file", versionFile.exists());
		StringWriter version = new StringWriter();
		try (Reader reader = new FileReader(versionFile)) {
			for (int character = reader.read(); character != -1; character = reader.read()) {
				version.write(character);
			}
		}

		// Look up the mojo
		PlexusConfiguration pluginConfiguration = extractPluginConfiguration("officefloor-maven-plugin", pluginPom);
		return this.lookupMojo("net.officefloor.maven", "officefloor-maven-plugin", version.toString(), goal,
				pluginConfiguration);
	}

}
