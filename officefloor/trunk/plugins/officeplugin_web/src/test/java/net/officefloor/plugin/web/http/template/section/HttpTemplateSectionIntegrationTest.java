/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireOfficeFloorSource;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the integration of the {@link HttpTemplateSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link Connection}.
	 */
	private final Connection connection = this
			.createSynchronizedMock(Connection.class);

	/**
	 * Port for running on.
	 */
	private int port;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {

		// Auto-wire for testing
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();

		// Add the HTTP server socket listener
		this.port = MockHttpServer.getAvailablePort();
		HttpServerSocketManagedObjectSource.autoWire(source, this.port,
				"SECTION", "renderTemplate");

		// Add connection
		source.addObject(Connection.class, this.connection);

		// Mock the HTTP Session
		HttpSession httpSession = this.createMock(HttpSession.class);
		source.addObject(HttpSession.class, httpSession);

		// TODO provide HTTP template router for testing

		// Load the template section
		final String templateLocation = this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/Template.ofp";
		AutoWireSection templateSection = source.addSection("SECTION",
				HttpTemplateSectionSource.class, templateLocation);
		templateSection.addSectionProperty(
				HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
				TemplateLogic.class.getName());

		// Load mock section for handling outputs
		AutoWireSection handleOutputSection = source.addSection("OUTPUT",
				ClassSectionSource.class, MockSection.class.getName());

		// Link flow outputs
		source.link(templateSection, "output", handleOutputSection, "finished");
		source.link(templateSection, "doExternalFlow", handleOutputSection,
				"finished");

		// Open the OfficeFloor
		this.officeFloor = source.openOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can render the template.
	 */
	public void testRenderTemplate() throws Exception {

		final String XML = "<html><body>Template Test:<table>"
				+ "<tr><td>Name</td><td>Description</td></tr>"
				+ "<tr><td>row</td><td>test row</td></tr></table>"
				+ "<form action=\"/SECTION.links/submit.task\">"
				+ "<input type=\"submit\"/></form></body></html>";

		// Send the request to obtain results of rending template
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet("http://localhost:" + this.port);
		HttpResponse response = client.execute(request);

		// Ensure successful
		assertEquals("Ensure successful", 200, response.getStatusLine()
				.getStatusCode());

		// Ensure correct rendering of template
		String rendering = MockHttpServer.getEntityBody(response);
		assertXmlEquals("Incorrect rendering", XML, rendering);
	}

	/**
	 * Ensure can handle submit to a link.
	 */
	public void testSubmit() throws Exception {

		// TODO remove
		if (true) {
			System.err.println("TODO uncomment to run "
					+ this.getClass().getSimpleName() + ".testSubmit");
			return;
		}

		final String RESPONSE = "submit - doInternalFlow[1] - finished(Parameter for External Flow)";

		// Send the request to obtain results of rending template
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet("http://localhost:" + this.port
				+ "/SECTION.links/submit.task");
		HttpResponse response = client.execute(request);

		// Ensure successful
		assertEquals("Ensure successful", 200, response.getStatusLine()
				.getStatusCode());

		// Ensure correct rendering of template
		String rendering = MockHttpServer.getEntityBody(response);
		assertEquals("Incorrect rendering", RESPONSE, rendering);
	}

	/**
	 * Mock section for output tasks of the template.
	 */
	public static class MockSection {
		public void finished(@Parameter String parameter,
				ServerHttpConnection connection) throws IOException {
			if ((parameter != null) && (parameter.length() > 0)) {
				Writer writer = new OutputStreamWriter(connection
						.getHttpResponse().getBody().getOutputStream());
				writer.write(" - finished(");
				writer.write(parameter);
				writer.write(")");
				writer.flush();
			}
		}
	}

}