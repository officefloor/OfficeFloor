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

package net.officefloor.plugin.woof.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import net.officefloor.autowire.AutoWire;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometInterest;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.comet.internal.CometSubscriptionService;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.woof.servlet.MockLogic.CometTrigger;
import net.officefloor.plugin.woof.servlet.client.MockGwtService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.gdevelop.gwt.syncrpc.SyncProxy;

/**
 * Tests the {@link WoofServletFilter}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletFilterTest extends OfficeFrameTestCase {

	/**
	 * Port {@link Server} is listening on.
	 */
	private int port;

	/**
	 * {@link Server}.
	 */
	private Server server;

	/**
	 * {@link HttpClient}.
	 */
	private final HttpClient client = new DefaultHttpClient();

	/**
	 * Ensure {@link WoofServletFilter} configures itself to service a request.
	 */
	public void testServiceRequest() throws Exception {
		this.doServiceRequestTest("");
	}

	/**
	 * Ensure {@link WoofServletFilter} respects the {@link ServletContext}.
	 */
	public void testServiceRequestWithinContext() throws Exception {
		this.doServiceRequestTest("/path");
	}

	/**
	 * Ensure can service a HTTP request.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	private void doServiceRequestTest(String contextPath) throws Exception {

		// Start Server (with context path)
		this.startServer(contextPath);

		// Validate appropriate response from HTTP template
		String responseText = this.doGetEntity(contextPath + "/test");
		assertEquals(
				"Incorrect template content",
				"TEMPLATE TEST OnePersonTeam_"
						+ new AutoWire(MockDependency.class).getQualifiedType(),
				responseText);
	}

	/**
	 * Ensure can invoke GWT AJAX service.
	 */
	public void testServiceGwtAjax() throws Exception {
		this.doServiceGwtAjaxTest("");
	}

	/**
	 * Ensure can invoke GWT AJAX service respecting the {@link ServletContext}.
	 */
	public void testServiceGwtAjaxWithinContext() throws Exception {
		this.doServiceGwtAjaxTest("/path");
	}

	/**
	 * Undertakes testing the servicing of GWT AJAX servicing.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	private void doServiceGwtAjaxTest(String contextPath) throws Exception {

		// Start Server
		this.startServer(contextPath);

		// Create the proxy for GWT
		MockGwtService service = (MockGwtService) SyncProxy.newProxyInstance(
				MockGwtService.class, "http://localhost:" + this.port
						+ contextPath + "/gwt/", "service");

		// Invoke the GWT service
		String result = service.gwtService("TEST");
		assertEquals("Incorrect response", "AJAX-TEST", result);
	}

	/**
	 * Ensure can invoke Comet service.
	 */
	public void testServiceComet() throws Exception {
		this.doServiceCometTest("");
	}

	/**
	 * Ensure can invoke Comet service respecting the {@link ServletContext}.
	 */
	public void testServiceCometWithinContext() throws Exception {
		this.doServiceCometTest("/path");
	}

	/**
	 * Undertakes testing the servicing of a comet request.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	private void doServiceCometTest(String contextPath) throws Exception {

		final String eventData = "TEST";

		// Start Server
		this.startServer(contextPath);

		// Create the proxy for GWT and trigger comet event
		MockGwtService service = (MockGwtService) SyncProxy.newProxyInstance(
				MockGwtService.class, "http://localhost:" + this.port
						+ contextPath + "/gwt/", "service");
		service.cometTrigger(eventData); // event will be waiting

		// Create the proxy for Comet and subscribe to event
		CometSubscriptionService caller = (CometSubscriptionService) SyncProxy
				.newProxyInstance(
						CometSubscriptionService.class,
						"http://localhost:" + this.port + contextPath + "/gwt/",
						"comet-subscribe");
		CometResponse response = caller.subscribe(new CometRequest(
				CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER, new CometInterest(
						CometTrigger.class.getName(), null)));
		CometEvent[] events = response.getEvents();
		assertEquals("Incorrect number of events", 1, events.length);
		CometEvent event = events[0];

		// Ensure correct event
		String data = (String) event.getData();
		assertEquals("Incorrect event", eventData, data);
	}

	/*
	 * =================== Setup/Teardown/Helper ==========================
	 */

	/**
	 * Executes a {@link HttpGet} against the URI.
	 * 
	 * @param uri
	 *            URI for the {@link HttpGet} request.
	 * @return Entity of response as text.
	 */
	private String doGetEntity(String uri) throws Exception {

		// Ensure serviced by HTTP template from WoOF configuration
		HttpGet request = new HttpGet("http://localhost:" + this.port + uri);
		HttpResponse response = this.client.execute(request);
		assertEquals("Must be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Obtain the response entity as text
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String responseText = new String(buffer.toByteArray());

		// Return the response entity
		return responseText;
	}

	@Override
	protected void setUp() throws Exception {
		// Obtain the port for the application
		this.port = MockHttpServer.getAvailablePort();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			this.client.getConnectionManager().shutdown();

		} finally {
			// Stop the server
			if (this.server != null) {
				this.server.stop();
			}
		}
	}

	/**
	 * Starts the {@link Server}.
	 * 
	 * @param contextPath
	 *            Context path running within.
	 */
	private void startServer(String contextPath) throws Exception {

		// Find the base directory for resources
		File baseDirectory = new File(".", "src/test/webapp");
		assertTrue("Base directory should exist", baseDirectory.isDirectory());

		// Start servlet container with filter
		this.server = new Server(this.port);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("".equals(contextPath) ? "/" : contextPath);
		context.setResourceBase(baseDirectory.getAbsolutePath());
		context.setSessionHandler(new SessionHandler());
		this.server.setHandler(context);

		// Add the WoOF Servlet Filter
		FilterHolder filter = new FilterHolder(new WoofServletFilter());
		context.addFilter(filter, "/*", EnumSet.of(DispatcherType.REQUEST));

		// Add Servlet for being filtered
		context.addServlet(MockHttpServlet.class, "/");

		// Start the server
		this.server.start();
	}

	/**
	 * Mock {@link HttpServlet}.
	 */
	public static class MockHttpServlet extends HttpServlet {
	}

}