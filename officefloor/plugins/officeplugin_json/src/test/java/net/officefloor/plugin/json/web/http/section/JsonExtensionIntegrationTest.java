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
package net.officefloor.plugin.json.web.http.section;

import java.io.IOException;
import java.io.Serializable;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.json.HttpJson;
import net.officefloor.plugin.json.JsonResponseWriter;
import net.officefloor.plugin.web.http.application.HttpTemplateSection;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.server.http.HttpTestUtil;

/**
 * Validates the extension of {@link HttpTemplateSection} via JSON.
 * 
 * @author Daniel Sagenschneider
 */
public class JsonExtensionIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Ensure JSON extension integrates.
	 */
	public void testJsonExtensionIntegration() throws Exception {

		// Configure the HTTP server
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.officeFloor((context) -> {
			HttpTestUtil.configureTestHttpServer(context, 7878, "ROUTE", "route");
		});
		compiler.web((context) -> {

			// Add the template
			String templatePath = this.getFileLocation(this.getClass(), "template.ofp");
			HttpTemplateSection template = context.getWebArchitect().addHttpTemplate("/test", templatePath,
					MockTemplateLogic.class);

			// Extend the template
			template.addTemplateExtension(JsonHttpTemplateSectionExtension.class);
		});

		// Start application
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();
		try (CloseableHttpClient client = HttpTestUtil.createHttpClient()) {

			// Ensure handles JSON object and JSON writer appropriately
			HttpPost request = new HttpPost("http://localhost:7878/test-ajax");
			request.setEntity(new StringEntity("{\"value\":\"REQUEST\"}"));
			HttpResponse response = client.execute(request);
			assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect response", "{\"value\":\"RESPONSE\"}", EntityUtils.toString(response.getEntity()));

		} finally {
			// Ensure stop server (client already closed)
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock JSON object.
	 */
	@HttpJson
	public static class MockJsonObject implements Serializable {

		private String value;

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	/**
	 * Mock template logic.
	 */
	public static class MockTemplateLogic {

		public void ajax(MockJsonObject object, JsonResponseWriter writer) throws IOException {

			// Ensure correct object
			assertEquals("Incorrect value", "REQUEST", object.getValue());

			// Provide response object
			MockJsonObject response = new MockJsonObject();
			response.setValue("RESPONSE");
			writer.writeResponse(response);
		}
	}

}