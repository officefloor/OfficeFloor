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
package net.officefloor.plugin.woof;

import java.io.IOException;
import java.io.Serializable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import net.officefloor.frame.test.LoggerAssertion;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.json.HttpJson;
import net.officefloor.plugin.json.JsonResponseWriter;

/**
 * Ensure JSON implicitly works for WoOF.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofJsonTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * {@link LoggerAssertion} for the {@link WoofLoader}.
	 */
	private LoggerAssertion loaderLoggerAssertion;

	/**
	 * {@link LoggerAssertion} for the {@link WoofOfficeFloorSource}.
	 */
	private LoggerAssertion sourceLoggerAssertion;

	@Override
	protected void setUp() throws Exception {

		// Create the logger assertions
		this.loaderLoggerAssertion = LoggerAssertion
				.setupLoggerAssertion(WoofLoaderImpl.class.getName());
		this.sourceLoggerAssertion = LoggerAssertion
				.setupLoggerAssertion(WoofOfficeFloorSource.class.getName());
	}

	@Override
	protected void tearDown() throws Exception {

		try {

			// Ensure stop
			try {
				this.client.getConnectionManager().shutdown();
			} finally {
				WoofOfficeFloorSource.stop();
			}

		} finally {
			// Disconnect from loggers
			this.sourceLoggerAssertion.disconnectFromLogger();
			this.loaderLoggerAssertion.disconnectFromLogger();
		}
	}

	/**
	 * Ensure can service JSON.
	 */
	public void testJson() throws Exception {

		// Obtain JSON WoOF configuration
		String jsonTemplateConfigurationLocation = this.getFileLocation(
				this.getClass(), "/JsonTemplate.woof");

		// Run the application with no logic template
		WoofOfficeFloorSource.start(
				WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				jsonTemplateConfigurationLocation);

		// Test
		HttpPost request = new HttpPost(
				"http://localhost:7878/template-link.woof");
		request.setEntity(new StringEntity("{\"value\":\"REQUEST\"}"));
		HttpResponse response = this.client.execute(request);
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());
		assertEquals("Incorrect response", "{\"value\":\"RESPONSE\"}",
				EntityUtils.toString(response.getEntity()));
	}

	/**
	 * JSON object.
	 */
	@HttpJson
	public static class JsonObject implements Serializable {

		private String value;

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	/**
	 * Template logic.
	 */
	public static class TemplateLogic {

		public void link(JsonObject request, JsonResponseWriter writer)
				throws IOException {

			// Ensure correct request
			assertEquals("Incorrect JSON request", "REQUEST",
					request.getValue());

			// Provide the response
			JsonObject response = new JsonObject();
			response.setValue("RESPONSE");
			writer.writeResponse(response);
		}
	}

}