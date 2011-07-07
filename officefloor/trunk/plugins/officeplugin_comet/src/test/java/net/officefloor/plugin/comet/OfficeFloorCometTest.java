/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.comet;

import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireAdministration;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirer;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.plugin.comet.api.CometSubscriber;
import net.officefloor.plugin.comet.api.OfficeFloorComet;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometInterest;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.comet.spi.CometService;
import net.officefloor.plugin.comet.spi.CometServiceManagedObjectSource;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.work.clazz.FlowInterface;

import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * Tests the {@link OfficeFloorComet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCometTest extends OfficeFrameTestCase {

	@Override
	protected void tearDown() throws Exception {
		AutoWireAdministration.closeAllOfficeFloors();
	}

	/**
	 * Ensure able to &quot;long poll&quot; for an event.
	 */
	public void testLongPoll() {

		// Start the server
		int port = MockHttpServer.getAvailablePort();
		startServer(port);

		// Subscribe for event
		CometServiceInvoker subscription = CometServiceInvoker.subscribe(port,
				"/template/comet-subscribe",
				CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER, new CometInterest(
						MockCometSubscriber.class.getName(), null));
		assertNull("Should not have a response",
				subscription.checkForResponse());

		// Publish an event
		long sequenceNumber = CometServiceInvoker.publish(port,
				"/template/comet-publish", new CometEvent(
						MockCometSubscriber.class.getName(), "EVENT", null));

		// Obtain the subscribed event
		CometResponse response = subscription.waitOnResponse();
		assertEquals("Incorrect number of events", 1,
				response.getEvents().length);

		// Ensure appropriate sequence number
		CometEvent event = response.getEvents()[0];
		assertEquals("Incorrect event sequence number", sequenceNumber,
				event.getSequenceNumber());
	}

	/**
	 * Main method to manually test with a browser.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String... args) throws Exception {

		// Indicate running manually
		System.out.println("Manually running Comet test application");

		// Start the server
		startServer(HttpServerAutoWireOfficeFloorSource.DEFAULT_HTTP_PORT);
	}

	/**
	 * Starts the server.
	 * 
	 * @param port
	 *            Port server is to listen on.
	 */
	private static void startServer(int port) {
		try {
			// Obtain the path to the template
			String templatePath = OfficeFloorCometTest.class.getPackage()
					.getName().replace('.', '/')
					+ "/Template.html";

			// Start server with GWT extension
			HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource(
					port);
			HttpTemplateAutoWireSection template = source.addHttpTemplate(
					templatePath, TemplateLogic.class, "template");

			// TODO replace with HTTP Template Extension
			source.addManagedObject(CometServiceManagedObjectSource.class,
					new ManagedObjectSourceWirer() {
						@Override
						public void wire(ManagedObjectSourceWirerContext context) {
							context.setInput(true);
							context.mapTeam(
									CometServiceManagedObjectSource.EXPIRE_TEAM_NAME,
									OnePersonTeamSource.class);
						}
					}, CometService.class).setTimeout(600 * 1000);
			AutoWireSection section = source.addSection("SECTION",
					ClassSectionSource.class, TemplateLogic.class.getName());
			source.linkUri("/template/comet-subscribe", section, "service");
			source.linkUri("/template/comet-publish", section, "service");

			// Extend the template
			SourcePropertiesImpl properties = new SourcePropertiesImpl();
			GwtHttpTemplateSectionExtension.extendTemplate(template, source,
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

		@FlowInterface
		public static interface Flows {
			void subscribe();

			void publish();
		}

		public void service(ServerGwtRpcConnection<Long> connection, Flows flows) {
			RPCRequest request = connection.getRpcRequest();
			Object parameter = request.getParameters()[0];
			if (parameter instanceof CometRequest) {
				flows.subscribe();
			} else {
				flows.publish();
			}
		}

		@NextTask("finished")
		public void subscribe(CometService service) {

			// Service subscription
			service.service();

			// TODO remove
			System.out.println("  SUBSCRIBE - COMET - STARTED");
		}

		public void finished(CometService service) {
			// TODO remove
			System.out.println("  SUBSCRIBE - COMET - COMPLETE");
		}

		public void publish(ServerGwtRpcConnection<Long> connection,
				CometService service) {
			try {
				RPCRequest request = connection.getRpcRequest();
				CometEvent event = (CometEvent) request.getParameters()[0];
				Class<?> listenerType = Class.forName(event
						.getListenerTypeName());
				long sequenceNumber = service.publishEvent(
						event.getSequenceNumber(), listenerType,
						event.getData(), event.getFilterKey());
				connection.onSuccess(Long.valueOf(sequenceNumber));
			} catch (Exception ex) {
				connection.onFailure(ex);
			}

			// TODO remove
			System.out.println("  PUBLISHED");
		}
	}

	/**
	 * Mock {@link CometSubscriber} interface for testing.
	 */
	private static interface MockCometSubscriber extends CometSubscriber {
	}

}