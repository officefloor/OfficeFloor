/*-
 * #%L
 * Web on OfficeFloor
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

package net.officefloor.woof;

import java.io.IOException;

import net.officefloor.frame.test.FileTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the WoOF server.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class WoofServerTest extends AbstractModelTestCase {

	/**
	 * {@link System} property for user home.
	 */
	private static final String USER_HOME = "user.home";

	private final FileTestSupport files = new FileTestSupport();

	/**
	 * Obtains the user home path.
	 * 
	 * @return User home path.
	 */
	private String userHomePath() throws IOException {
		return this.files.findFile(".config/officefloor/application-test.properties").getParentFile().getParentFile()
				.getParentFile().getAbsolutePath();
	}

	/**
	 * Ensure can invoke {@link HttpRequest} on the WoOF server.
	 */
	@Test
	public void woofServerDefaultPorts() throws IOException {
		this.doRequestTest("/template", "TEMPLATE");
	}

	/**
	 * Ensure can invoke {@link HttpRequest} on the WoOF server.
	 */
	@Test
	public void woofServerNonDefaultPorts() throws IOException {

		// Open the OfficeFloor (on non-default ports)
		this.officeFloor = WoOF.open(8787, 9797, NO_COMPOSITION_PROPERTY_NAME, NO_COMPOSITION_PROPERTY_VALUE);

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpResponse response = client.execute(new HttpGet("http://localhost:8787/template"));
			assertEquals("TEMPLATE", HttpClientTestUtil.entityToString(response), "Incorrect template");
		}
	}

	/**
	 * Ensure can override context.
	 */
	@Test
	public void contextualOverload() throws IOException {

		// Run within context (without WoOF loads)
		this.officeFloor = WoofLoaderSettings.contextualLoad((context) -> {

			// Don't load WoOF
			context.notLoadWoof();
			context.notLoadObjects();

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
			assertEquals(404, response.getStatusLine().getStatusCode(), "Should not be found");

			// Ensure can obtain configured in service
			response = client.execute(new HttpGet("http://localhost:7878/extended"));
			assertEquals(200, response.getStatusLine().getStatusCode(), "Should obtain value");
			assertEquals("OVERRIDE", HttpClientTestUtil.entityToString(response), "Incorrect entity");
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
	@Test
	public void defaultJsonObject() throws IOException {

		// Open the OfficeFloor (on default ports)
		this.officeFloor = WoOF.open(NO_COMPOSITION_PROPERTY_NAME, NO_COMPOSITION_PROPERTY_VALUE);

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpPost post = new HttpPost("http://localhost:7878/objects");
			post.addHeader("Content-Type", "application/json");
			post.setEntity(new StringEntity("{\"message\":\"TEST\"}"));
			HttpResponse response = client.execute(post);
			assertEquals("{\"message\":\"TEST-mock\"}",
					HttpClientTestUtil.entityToString(response), "Incorrect template");
		}
	}

	/**
	 * Ensure can configure {@link Team}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void teams() throws IOException {
		this.doRequestTest("/teams", "\"DIFFERENT THREAD\"");
	}

	/**
	 * Ensure can invoke a {@link Procedure}.
	 */
	@Test
	public void procedure() throws IOException {
		this.doRequestTest("/procedure", "\"PROCEDURE\"");
	}

	/**
	 * Ensure can override {@link Property} value via default properties file.
	 */
	@Test
	public void applicationProperties() throws IOException {
		this.doRequestTest("/property", "DEFAULT_OVERRIDE");
	}

	/**
	 * Ensure can override {@link Property} value via profile properties file.
	 */
	@Test
	public void applicationProfileProperties() throws IOException {
		this.doRequestTest("/property", "TEST_OVERRIDE", WoOF.DEFAULT_OFFICE_PROFILES, "test");
	}

	/**
	 * Ensure can specify contextual profile.
	 */
	@Test
	public void contextOverrideProperty() throws IOException {
		WoofLoaderSettings.contextualLoad((context) -> {
			context.addOverrideProperty("Property.function.override", "CONTEXT_OVERRIDE");
			this.doRequestTest("/property", "CONTEXT_OVERRIDE");
			return null;
		});
	}

	/**
	 * Ensure can override {@link Property} via environment.
	 */
	@Test
	public void environmentProperty() throws Exception {
		this.doEnvironmentTest("/property", "ENV_OVERRIDE", "OFFICEFLOOR.application.Property.function.override",
				"ENV_OVERRIDE");
	}

	/**
	 * Ensure can override {@link Property} via user properties.
	 */
	@Test
	public void userProperties() throws IOException {
		this.doSystemPropertiesTest("/property", "USER_OVERRIDE", USER_HOME, this.userHomePath());
	}

	/**
	 * Ensure can override {@link Property} via user profile properties.
	 */
	@Test
	public void userProfileProperties() throws IOException {
		this.doSystemPropertiesTest("/property", "USER_TEST_OVERRIDE", USER_HOME, this.userHomePath(),
				WoOF.DEFAULT_OFFICE_PROFILES, "test");
	}

	/**
	 * Ensure can override {@link Property} value via {@link System}.
	 */
	@Test
	public void systemProperty() throws IOException {
		this.doSystemPropertiesTest("/property", "SYSTEM_OVERRIDE", "OFFICE.Property.function.override",
				"SYSTEM_OVERRIDE");
	}

	/**
	 * Ensure can override {@link Property} value via command line parameters.
	 */
	@Test
	public void commandLineProperty() throws IOException {
		this.doRequestTest("/property", "COMMAND_LINE", "OFFICE.Property.function.override", "COMMAND_LINE");
	}

	/**
	 * Ensure no external properties configured.
	 */
	@Test
	public void noExternalProperties() throws IOException {
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
	@Test
	public void singleProfile() throws IOException {
		this.doSystemPropertiesTest("/profile", "test", WoOF.DEFAULT_OFFICE_PROFILES, "test");
	}

	/**
	 * Ensure handle multiple profiles.
	 */
	@Test
	public void multipleProfiles() throws IOException {
		this.doSystemPropertiesTest("/profile", "test,unknown,override", "OFFICE.profiles",
				"test ,  unknown, override ");
	}

	/**
	 * Ensure can specify profile via environment.
	 */
	@Test
	public void environmentProfile() throws Exception {
		this.doEnvironmentTest("/profile", "environment", "OFFICEFLOOR." + WoOF.DEFAULT_OFFICE_PROFILES, "environment");
	}

	/**
	 * Ensure can specify profile via {@link System}.
	 */
	@Test
	public void systemProfile() throws IOException {
		this.doSystemPropertiesTest("/profile", "system", WoOF.DEFAULT_OFFICE_PROFILES, "system");
	}

	/**
	 * Ensure can load profile via command line.
	 */
	@Test
	public void commandLineProfile() throws IOException {
		this.doRequestTest("/profile", "commandline", WoOF.DEFAULT_OFFICE_PROFILES, "commandline");
	}

	/**
	 * Ensure can specify contextual profile.
	 */
	@Test
	public void contextProfile() throws IOException {
		WoofLoaderSettings.contextualLoad((context) -> {
			context.notLoadExternal();
			context.addProfile("test");
			this.doSystemPropertiesTest("/property", "TEST_OVERRIDE");
			return null;
		});
	}

}
