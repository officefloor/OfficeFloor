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
package net.officefloor.plugin.gwt.web.http.section;

import java.io.ByteArrayOutputStream;

import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.gwt.service.MockGwtServiceInterface;
import net.officefloor.plugin.gwt.service.MockGwtServiceInterfaceAsync;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Test integration of GWT.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtIntegrationTest extends OfficeFrameTestCase {

	/**
	 * HTTP Server.
	 */
	private HttpServerAutoWireOfficeFloorSource source;

	/**
	 * Port for testing.
	 */
	private int port;

	/**
	 * HTTP Client.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * {@link Escalation} from the {@link OfficeFloor}.
	 */
	private static volatile Throwable escalation = null;

	@Override
	protected void setUp() throws Exception {
		// Configure the port
		this.port = MockHttpServer.getAvailablePort();
		this.source = new HttpServerAutoWireOfficeFloorSource(this.port);
	}

	/**
	 * Ensure transforms HTML to include GWT.
	 */
	public void testTransformation() throws Exception {
		this.doTransformationTest("template", "template");
	}

	/**
	 * Ensures handles root template (/) transformation for HTML to include GWT.
	 */
	public void testRootTransformation() throws Exception {
		this.doTransformationTest("/", "root");
	}

	/**
	 * Undertakes the transformation for HTML to include GWT.
	 * 
	 * @param templateUri
	 *            Template URI.
	 * @param gwtServiceUri
	 *            GWT Service URI.
	 */
	private void doTransformationTest(String templateUri, String gwtServiceUri)
			throws Exception {

		// Configure the template with GWT
		String templatePath = this.getFileLocation(this.getClass(),
				"Template.html");
		HttpTemplateAutoWireSection section = this.source
				.addHttpTemplate(templateUri, templatePath,
						GwtTransformationTemplateLogic.class);

		// Add the GWT Extension
		GwtHttpTemplateSectionExtension.extendTemplate(section, this.source,
				new SourcePropertiesImpl(), Thread.currentThread()
						.getContextClassLoader());

		// Start Server
		this.source.openOfficeFloor();

		// Request the template
		HttpResponse response = this.client.execute(new HttpGet(
				"http://localhost:" + this.port + "/"
						+ ("/".equals(templateUri) ? "" : templateUri)));
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Obtain the response
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String responseBody = buffer.toString();

		// Ensure expected response
		final String GWT_SCRIPT = "<script type=\"text/javascript\" language=\"javascript\" src=\""
				+ gwtServiceUri
				+ "/"
				+ gwtServiceUri
				+ ".nocache.js\"></script>";
		final String GWT_HISTORY_IFRAME = "<iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>";
		final String EXPECTED_RESPONSE = "<html><head>" + GWT_SCRIPT
				+ "<title>GWT</title></head><body>" + GWT_HISTORY_IFRAME
				+ "<p>Test</p></body></html>";
		assertEquals("Incorrect response", EXPECTED_RESPONSE, responseBody);
	}

	/**
	 * Ensure able to invoke GWT Service.
	 */
	public void testGwtService() throws Exception {
		this.doGwtServiceTest("template", "template");
	}

	/**
	 * Ensure able to invoke GWT Service for root template.
	 */
	public void testRootGwtService() throws Exception {
		this.doGwtServiceTest("/", "root");
	}

	/**
	 * Undertakes invoking the GWT Service.
	 * 
	 * @param templateUri
	 *            Template URI.
	 * @param gwtServiceUri
	 *            GWT service URI.
	 */
	private void doGwtServiceTest(String templateUri, String gwtServiceUri)
			throws Exception {

		// Configure the template with GWT Service
		String templatePath = this.getFileLocation(this.getClass(),
				"Template.html");
		HttpTemplateAutoWireSection section = this.source.addHttpTemplate(
				templateUri, templatePath, GwtServiceTemplateLogic.class);

		// Add the GWT Extension
		SourcePropertiesImpl properties = new SourcePropertiesImpl();
		properties
				.addProperty(
						GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
						MockGwtServiceInterfaceAsync.class.getName());
		GwtHttpTemplateSectionExtension.extendTemplate(section, this.source,
				properties, Thread.currentThread().getContextClassLoader());

		// Capture any exceptions
		this.source
				.linkEscalation(Throwable.class, section, "handleEscalation");

		// Start Server
		this.source.openOfficeFloor();

		// Invoke GWT Service and validate returns successfully
		String moduleBaseURL = "http://localhost:" + this.port + "/"
				+ gwtServiceUri + "/";
		MockGwtServiceInterface service = (MockGwtServiceInterface) SyncProxy
				.newProxyInstance(MockGwtServiceInterface.class, moduleBaseURL,
						"GwtServicePath");
		String result = service.service(new Integer(10));
		assertEquals("Incorrect result", "10", result);
	}

	@Override
	protected void tearDown() throws Exception {
		// Shutdown
		try {
			AutoWireManagement.closeAllOfficeFloors();
		} finally {
			this.client.getConnectionManager().shutdown();
		}

		// Ensure no escalation failure
		if (escalation != null) {
			fail(escalation);
		}
	}

	/**
	 * GWT Template logic.
	 */
	public static class GwtServiceTemplateLogic {
		public void service(@Parameter Integer parameter,
				AsyncCallback<String> callback) {
			callback.onSuccess(String.valueOf(parameter.intValue()));
		}

		public void handleEscalation(@Parameter Throwable escalation) {
			GwtIntegrationTest.escalation = escalation;
		}
	}

	/**
	 * Transformation template logic (as does not GWT Service).
	 */
	public static class GwtTransformationTemplateLogic {
		public void service() {
		}
	}

}