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
package net.officefloor.plugin.gwt.comet.web.http.section;

import java.io.IOException;
import java.lang.reflect.Proxy;

import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.gwt.comet.CometPublisher;
import net.officefloor.plugin.gwt.comet.CometPublisherInterface;
import net.officefloor.plugin.gwt.comet.CometServiceInvoker;
import net.officefloor.plugin.gwt.comet.api.CometSubscriber;
import net.officefloor.plugin.gwt.comet.api.OfficeFloorComet;
import net.officefloor.plugin.gwt.comet.internal.CometEvent;
import net.officefloor.plugin.gwt.comet.internal.CometInterest;
import net.officefloor.plugin.gwt.comet.internal.CometRequest;
import net.officefloor.plugin.gwt.comet.internal.CometResponse;
import net.officefloor.plugin.gwt.comet.spi.CometService;
import net.officefloor.plugin.gwt.comet.web.http.section.CometHttpTemplateSectionExtension;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Tests the {@link OfficeFloorComet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class CometHttpTemplateSectionExtensionTest extends OfficeFrameTestCase {

	/**
	 * Main method to manually test with a browser to test
	 * {@link OfficeFloorComet} interaction.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String... args) throws Exception {

		// Indicate running manually
		System.out.println("Manually running Comet test application");

		// Start the server
		startServer(
				HttpApplicationLocationManagedObjectSource.DEFAULT_HTTP_PORT,
				"/");
	}

	/**
	 * Flag indicating if manually published.
	 */
	private static boolean isManualPublish = false;

	@Override
	protected void setUp() throws Exception {
		// Reset for testing
		isManualPublish = false;
	}

	@Override
	protected void tearDown() throws Exception {
		AutoWireManagement.closeAllOfficeFloors();
	}

	/**
	 * Ensure automatic publishing of {@link CometEvent} to the others on the
	 * server.
	 */
	public void testAutomaticPublish() {
		this.doAutomaticPublish("template", "template");
	}

	/**
	 * Ensure automatic publishing of {@link CometEvent} to the others on the
	 * server for root template.
	 */
	public void testAutomaticPublishForRoot() {
		this.doAutomaticPublish("/", "root");
	}

	/**
	 * Ensure automatic publishing of {@link CometEvent} to the others on the
	 * server.
	 * 
	 * @param templateUri
	 *            Template URI.
	 * @param gwtServiceRelativePath
	 *            GWT service relative path.
	 */
	private void doAutomaticPublish(String templateUri,
			String gwtServiceRelativePath) {

		// Start the server
		int port = MockHttpServer.getAvailablePort();
		startServer(port, templateUri);

		// Subscribe for event
		CometServiceInvoker subscription = CometServiceInvoker.subscribe(port,
				"/" + gwtServiceRelativePath + "/comet-subscribe",
				CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER, new CometInterest(
						MockCometSubscriber.class.getName(), null));
		assertNull("Should not have a response",
				subscription.checkForResponse());

		// Publish an event
		long sequenceNumber = CometServiceInvoker.publish(port, "/"
				+ gwtServiceRelativePath + "/comet-publish", new CometEvent(
				MockCometSubscriber.class.getName(), "EVENT", null));

		// Obtain the subscribed event
		CometResponse response = subscription.waitOnResponse();
		assertEquals("Incorrect number of events", 1,
				response.getEvents().length);

		// Ensure appropriate sequence number
		CometEvent event = response.getEvents()[0];
		assertEquals("Incorrect event sequence number", sequenceNumber,
				event.getSequenceNumber());

		// Ensure not manually published
		assertFalse("Should be automatically published", isManualPublish);
	}

	/**
	 * Ensure able to manually publish the {@link CometEvent}.
	 */
	public void testManuallyPublish() {
		this.doManuallyPublishTest("template", "template");
	}

	/**
	 * Ensure able to manually publish the {@link CometEvent} for the root
	 * template.
	 */
	public void testManuallyPublishForRoot() {
		this.doManuallyPublishTest("/", "root");
	}

	/**
	 * Ensure able to manually publish the {@link CometEvent}.
	 * 
	 * @param templateUri
	 *            Template URI.
	 * @param gwtServiceRelativePath
	 *            GWT service relative path.
	 */
	public void doManuallyPublishTest(String templateUri,
			String gwtServiceRelativePath) {

		// Start the server (with manual handling of publishing)
		int port = MockHttpServer.getAvailablePort();
		startServer(
				port,
				templateUri,
				CometHttpTemplateSectionExtension.PROPERTY_MANUAL_PUBLISH_METHOD_NAME,
				"manualPublish");

		// Subscribe for event
		CometServiceInvoker subscription = CometServiceInvoker.subscribe(port,
				"/" + gwtServiceRelativePath + "/comet-subscribe",
				CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER, new CometInterest(
						MockCometSubscriber.class.getName(), null));
		assertNull("Should not have a response",
				subscription.checkForResponse());

		// Publish an event
		long sequenceNumber = CometServiceInvoker.publish(port, "/"
				+ gwtServiceRelativePath + "/comet-publish", new CometEvent(
				MockCometSubscriber.class.getName(), "EVENT", null));

		// Obtain the subscribed event
		CometResponse response = subscription.waitOnResponse();
		assertEquals("Incorrect number of events", 1,
				response.getEvents().length);

		// Ensure appropriate sequence number
		CometEvent event = response.getEvents()[0];
		assertEquals("Incorrect event sequence number", sequenceNumber,
				event.getSequenceNumber());

		// Ensure manually published
		assertTrue("Should be manually published", isManualPublish);
	}

	/**
	 * Ensure server able to publish a {@link CometEvent} via a
	 * {@link CometPublisher} {@link Proxy}.
	 */
	public void testServerPublish() throws Exception {
		this.doServerPublishTest("template", "template", "template");
	}

	/**
	 * Ensure server able to publish a {@link CometEvent} via a
	 * {@link CometPublisher} {@link Proxy} for root template.
	 */
	public void testServerPublishForRoot() throws Exception {
		this.doServerPublishTest("/", "root", "");
	}

	/**
	 * Ensure server able to publish a {@link CometEvent} via a
	 * {@link CometPublisher} {@link Proxy}.
	 * 
	 * @param templateUri
	 *            Template URI.
	 * @param gwtServiceRelativePath
	 *            GWT service relative path.
	 * @param linkUri
	 *            Link URI.
	 */
	public void doServerPublishTest(String templateUri,
			String gwtServiceRelativePath, String linkUri) throws Exception {

		// Start the server (with manual handling of publishing)
		int port = MockHttpServer.getAvailablePort();
		startServer(port, templateUri);

		// Subscribe for event
		CometServiceInvoker subscription = CometServiceInvoker.subscribe(port,
				"/" + gwtServiceRelativePath + "/comet-subscribe",
				CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER, new CometInterest(
						MockCometSubscriber.class.getName(), null));
		assertNull("Should not have a response",
				subscription.checkForResponse());

		// Trigger server to publish an event
		HttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = client.execute(new HttpGet(
				"http://localhost:" + port + "/" + linkUri
						+ "-triggerServerEvent"));
		assertEquals("Ensure successful", 200, httpResponse.getStatusLine()
				.getStatusCode());
		assertTrue("Should have a response entity", httpResponse.getEntity()
				.getContentLength() > 0);
		assertEquals("Ensure success flag", 1, httpResponse.getEntity()
				.getContent().read());
		client.getConnectionManager().shutdown();

		// Obtain the subscribed event
		CometResponse response = subscription.waitOnResponse();
		assertEquals("Incorrect number of events", 1,
				response.getEvents().length);

		// Ensure appropriate event
		CometEvent event = response.getEvents()[0];
		assertEquals("Incorrect server event", "SERVER", event.getData());

		// Ensure not manually published
		assertFalse("Should not be manually published", isManualPublish);
	}

	/**
	 * Starts the server.
	 * 
	 * @param port
	 *            Port server is to listen on.
	 * @param templateUri
	 *            Template URI.
	 * @param propertyNameValuePairs
	 *            Property name value pairs for the
	 *            {@link CometHttpTemplateSectionExtension}.
	 */
	private static void startServer(int port, String templateUri,
			String... propertyNameValuePairs) {
		try {

			// Obtain the path to the template
			String templatePath = CometHttpTemplateSectionExtensionTest.class
					.getPackage().getName().replace('.', '/')
					+ "/Template.html";

			// Start server with GWT extension
			HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource(
					port);
			HttpTemplateAutoWireSection template = source.addHttpTemplate(
					templateUri, templatePath, TemplateLogic.class);

			// Extend the template for GWT
			GwtHttpTemplateSectionExtension.extendTemplate(template, source,
					new SourcePropertiesImpl(), Thread.currentThread()
							.getContextClassLoader());

			// Extend the template for Comet (including specifying properties)
			SourcePropertiesImpl properties = new SourcePropertiesImpl();
			for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
				String name = propertyNameValuePairs[i];
				String value = propertyNameValuePairs[i = 1];
				properties.addProperty(name, value);
			}
			CometHttpTemplateSectionExtension.extendTemplate(template, source,
					properties, Thread.currentThread().getContextClassLoader());

			// Start server
			source.openOfficeFloor();

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Template logic class.
	 */
	public static class TemplateLogic {

		/**
		 * Handles manually publishing the {@link CometEvent}.
		 * 
		 * @param event
		 *            {@link CometEvent}.
		 * @param service
		 *            {@link CometService}.
		 * @param connection
		 *            {@link ServerGwtRpcConnection}.
		 */
		public void manualPublish(@Parameter CometEvent event,
				CometService service, ServerGwtRpcConnection<Long> connection)
				throws ClassNotFoundException {

			// Publish the event
			long sequenceNumber = service.publishEvent(
					event.getListenerTypeName(), event.getData(),
					event.getMatchKey());

			// Flag manually published
			CometHttpTemplateSectionExtensionTest.isManualPublish = true;

			// Provide response
			connection.onSuccess(Long.valueOf(sequenceNumber));
		}

		/**
		 * Triggers server to provide event.
		 * 
		 * @param publisher
		 *            {@link MockCometSubscriber}.
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 */
		public void triggerServerEvent(MockCometSubscriber publisher,
				ServerHttpConnection connection) throws IOException {

			// Trigger the comet event
			publisher.sendEvent("SERVER");

			// Provide response
			connection.getHttpResponse().getEntityWriter()
					.write(new byte[] { 1 });
		}
	}

	/**
	 * Mock {@link CometSubscriber} interface for testing.
	 */
	@CometPublisherInterface
	private static interface MockCometSubscriber extends CometSubscriber {
		void sendEvent(String event);
	}

}