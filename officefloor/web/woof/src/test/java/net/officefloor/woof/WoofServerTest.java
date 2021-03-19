/*-
 * #%L
 * Web on OfficeFloor
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

package net.officefloor.woof;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.team.Team;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpInput;

/**
 * Tests the WoOF server.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServerTest extends AbstractTestCase {

	/**
	 * {@link System} property for user home.
	 */
	private static final String USER_HOME = "user.home";

	/**
	 * Obtains the user home path.
	 * 
	 * @return User home path.
	 */
	private String userHomePath() throws IOException {
		return this.findFile(".config/officefloor/application-test.properties").getParentFile().getParentFile()
				.getParentFile().getAbsolutePath();
	}

	/**
	 * Ensure can invoke {@link HttpRequest} on the WoOF server.
	 */
	public void testWoofServerDefaultPorts() throws IOException {
		this.doRequestTest("/template", "TEMPLATE");
	}

	/**
	 * Ensure can invoke {@link HttpRequest} on the WoOF server.
	 */
	public void testWoofServerNonDefaultPorts() throws IOException {

		// Open the OfficeFloor (on non-default ports)
		this.officeFloor = WoOF.open(8787, 9797);

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpResponse response = client.execute(new HttpGet("http://localhost:8787/template"));
			assertEquals("Incorrect template", "TEMPLATE", HttpClientTestUtil.entityToString(response));
		}
	}

	/**
	 * Ensure can override context.
	 */
	public void testContextualOverload() throws IOException {

		// Run within context (without WoOF loads)
		this.officeFloor = WoofLoaderSettings.contextualLoad((context) -> {

			// Don't load WoOF
			context.notLoadWoof();

			// Register handling
			context.extend((woofContext) -> {
				OfficeArchitect office = woofContext.getOfficeArchitect();

				// Add the section
				OfficeSection section = office.addOfficeSection("SECTION", ClassSectionSource.class.getName(),
						ContextualOverloadSection.class.getName());

				// Configure to service request
				HttpInput input = woofContext.getWebArchitect().getHttpInput(false, "GET", "/extended");
				office.link(input.getInput(), section.getOfficeSectionInput("service"));
			});

			// Start the OfficeFloor
			return WoOF.open();
		});

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure WoOF template not loaded
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/template"));
			assertEquals("Should not be found", 404, response.getStatusLine().getStatusCode());

			// Ensure can obtain configured in service
			response = client.execute(new HttpGet("http://localhost:7878/extended"));
			assertEquals("Should obtain value", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect entity", "OVERRIDE", HttpClientTestUtil.entityToString(response));
		}
	}

	public static class ContextualOverloadSection {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("OVERRIDE");
		}
	}

	/**
	 * Ensure default JSON configured.
	 */
	public void testDefaultJsonObject() throws IOException {

		// Open the OfficeFloor (on default ports)
		this.officeFloor = WoOF.open();

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpPost post = new HttpPost("http://localhost:7878/objects");
			post.addHeader("Content-Type", "application/json");
			post.setEntity(new StringEntity("{\"message\":\"TEST\"}"));
			HttpResponse response = client.execute(post);
			assertEquals("Incorrect template", "{\"message\":\"TEST-mock\"}",
					HttpClientTestUtil.entityToString(response));
		}
	}

	/**
	 * Ensure can configure {@link Team}.
	 * 
	 * @throws IOException
	 */
	public void testTeams() throws IOException {
		this.doRequestTest("/teams", "\"DIFFERENT THREAD\"");
	}

	/**
	 * Ensure can invoke a {@link Procedure}.
	 */
	public void testProcedure() throws IOException {
		this.doRequestTest("/procedure", "\"PROCEDURE\"");
	}

	/**
	 * Ensure can override {@link Property} value via default properties file.
	 */
	public void testApplicationProperties() throws IOException {
		this.doRequestTest("/property", "DEFAULT_OVERRIDE");
	}

	/**
	 * Ensure can override {@link Property} value via profile properties file.
	 */
	public void testApplicationProfileProperties() throws IOException {
		this.doRequestTest("/property", "TEST_OVERRIDE", WoOF.DEFAULT_OFFICE_PROFILES, "test");
	}

	/**
	 * Ensure can specify contextual profile.
	 */
	public void testContextOverrideProperty() throws IOException {
		WoofLoaderSettings.contextualLoad((context) -> {
			context.addOverrideProperty("Property.function.override", "CONTEXT_OVERRIDE");
			this.doRequestTest("/property", "CONTEXT_OVERRIDE");
			return null;
		});
	}

	/**
	 * Ensure can override {@link Property} via environment.
	 */
	public void testEnvironmentProperty() throws Exception {
		this.doEnvironmentTest("/property", "ENV_OVERRIDE", "OFFICEFLOOR.application.Property.function.override",
				"ENV_OVERRIDE");
	}

	/**
	 * Ensure can override {@link Property} via user properties.
	 */
	public void testUserProperties() throws IOException {
		this.doSystemPropertiesTest("/property", "USER_OVERRIDE", USER_HOME, this.userHomePath());
	}

	/**
	 * Ensure can override {@link Property} via user profile properties.
	 */
	public void testUserProfileProperties() throws IOException {
		this.doSystemPropertiesTest("/property", "USER_TEST_OVERRIDE", USER_HOME, this.userHomePath(),
				WoOF.DEFAULT_OFFICE_PROFILES, "test");
	}

	/**
	 * Ensure can override {@link Property} value via {@link System}.
	 */
	public void testSystemProperty() throws IOException {
		this.doSystemPropertiesTest("/property", "SYSTEM_OVERRIDE", "OFFICE.Property.function.override",
				"SYSTEM_OVERRIDE");
	}

	/**
	 * Ensure can override {@link Property} value via command line parameters.
	 */
	public void testCommandLineProperty() throws IOException {
		this.doRequestTest("/property", "COMMAND_LINE", "OFFICE.Property.function.override", "COMMAND_LINE");
	}

	/**
	 * Ensure no external properties configured.
	 */
	public void testNoExternalProperties() throws IOException {
		WoofLoaderSettings.contextualLoad((context) -> {
			context.notLoadExternal();
			this.doSystemPropertiesTest("/property", "DEFAULT_OVERRIDE", "OFFICE.Property.function.override",
					"SYSTEM_OVERRIDE", WoOF.DEFAULT_OFFICE_PROFILES, "test", USER_HOME, this.userHomePath());
			return null;
		});
	}

	/**
	 * Ensure can specify single profile.
	 */
	public void testSingleProfile() throws IOException {
		this.doSystemPropertiesTest("/profile", "test", WoOF.DEFAULT_OFFICE_PROFILES, "test");
	}

	/**
	 * Ensure handle multiple profiles.
	 */
	public void testMultipleProfiles() throws IOException {
		this.doSystemPropertiesTest("/profile", "test,unknown,override", "OFFICE.profiles",
				"test ,  unknown, override ");
	}

	/**
	 * Ensure can specify profile via environment.
	 */
	public void testEnvironmentProfile() throws Exception {
		this.doEnvironmentTest("/profile", "environment", "OFFICEFLOOR." + WoOF.DEFAULT_OFFICE_PROFILES, "environment");
	}

	/**
	 * Ensure can specify profile via {@link System}.
	 */
	public void testSystemProfile() throws IOException {
		this.doSystemPropertiesTest("/profile", "system", WoOF.DEFAULT_OFFICE_PROFILES, "system");
	}

	/**
	 * Ensure can load profile via command line.
	 */
	public void testCommandLineProfile() throws IOException {
		this.doRequestTest("/profile", "commandline", WoOF.DEFAULT_OFFICE_PROFILES, "commandline");
	}

	/**
	 * Ensure can specify contextual profile.
	 */
	public void testContextProfile() throws IOException {
		WoofLoaderSettings.contextualLoad((context) -> {
			context.notLoadExternal();
			context.addProfile("test");
			this.doSystemPropertiesTest("/property", "TEST_OVERRIDE");
			return null;
		});
	}

}
