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
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirer;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.plugin.comet.api.OfficeFloorComet;
import net.officefloor.plugin.comet.client.MockCometListener;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometPublicationServiceAsync;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.comet.spi.CometService;
import net.officefloor.plugin.comet.spi.CometServiceManagedObjectSource;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Tests the {@link OfficeFloorComet} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCometTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to &quot;long poll&quot; for an event.
	 */
	public void testLongPoll() {
		// TODO provide long poll request
		fail("TODO implement");
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

		// Obtain the path to the template
		String templatePath = OfficeFloorCometTest.class.getPackage().getName()
				.replace('.', '/')
				+ "/Template.html";

		// Start server with GWT extension
		HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource();
		HttpTemplateAutoWireSection template = source.addHttpTemplate(
				templatePath, TemplateLogic.class, "template");

		// TODO remove but for now include
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
		source.linkUri("/template/comet", section, "listen");

		// CometSubscriptionServiceAsync.class.getName() + ","

		// Extend the template
		SourcePropertiesImpl properties = new SourcePropertiesImpl();
		properties
				.addProperty(
						GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
						CometPublicationServiceAsync.class.getName());
		GwtHttpTemplateSectionExtension.extendTemplate(template, source,
				properties, Thread.currentThread().getContextClassLoader());

		source.openOfficeFloor();
	}

	/**
	 * Template logic class.
	 */
	public static class TemplateLogic {

		@NextTask("finished")
		public void listen_NOT(@Parameter CometRequest request,
				AsyncCallback<CometResponse> callback) {

			callback.onSuccess(new CometResponse(new CometEvent(1,
					MockCometListener.class.getName(), "EVENT", "FILTER_KEY")));

			// TODO remove
			System.out.println("  SUBSCRIBE - REQUEST");
		}

		@NextTask("finished")
		public void listen(CometService service) {

			service.service();

			// TODO remove
			System.out.println("  SUBSCRIBE - COMET - STARTED");
		}

		public void finished(CometService service) {
			// TODO remove
			System.out.println("  SUBSCRIBE - COMET - COMPLETE");
		}

		public void publish(@Parameter CometEvent event,
				AsyncCallback<Long> callback, CometService service) {
			// callback.onSuccess(Long.valueOf(1));

			try {
				Class<?> listenerType = Class.forName(event
						.getListenerTypeName());
				long sequenceNumber = service.publishEvent(listenerType,
						event.getData(), event.getFilterKey());
				callback.onSuccess(Long.valueOf(sequenceNumber));
			} catch (Exception ex) {
				callback.onFailure(ex);
			}

			// TODO remove
			System.out.println("  PUBLISHED");
		}
	}

}