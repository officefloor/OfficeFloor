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
package net.officefloor.plugin.comet.web.http.section;

import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirer;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometPublicationService;
import net.officefloor.plugin.comet.internal.CometSubscriptionService;
import net.officefloor.plugin.comet.spi.CometService;
import net.officefloor.plugin.comet.spi.CometServiceManagedObjectSource;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;

import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * {@link HttpTemplateSectionExtension} to include Comet.
 * 
 * @author Daniel Sagenschneider
 */
public class CometHttpTemplateSectionExtension {

	/**
	 * Name of the {@link AutoWireSection} to service the
	 * {@link CometSubscriptionService} and {@link CometPublicationService}.
	 */
	public static final String COMET_SECTION_NAME = "COMET";

	/**
	 * Initiates the extending of the template with Comet.
	 * 
	 * @param template
	 *            {@link HttpTemplateAutoWireSection}.
	 * @param application
	 *            {@link WebAutoWireApplication}.
	 * @param properties
	 *            {@link SourceProperties}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @throws Exception
	 *             If fails to extend the template configuration.
	 */
	public static void extendTemplate(
			final HttpTemplateAutoWireSection template,
			final WebAutoWireApplication application,
			SourceProperties properties, ClassLoader classLoader)
			throws Exception {

		// Determine if already configured Comet Logic
		if (!application.isObjectAvailable(CometService.class)) {
			// Configure the Comet Logic
			application.addManagedObject(CometServiceManagedObjectSource.class,
					new ManagedObjectSourceWirer() {
						@Override
						public void wire(ManagedObjectSourceWirerContext context) {
							context.setInput(true);
							context.mapTeam(
									CometServiceManagedObjectSource.EXPIRE_TEAM_NAME,
									OnePersonTeamSource.class);
						}
					}, CometService.class).setTimeout(600 * 1000);
		}

		// Obtain the section to service Comet requests
		AutoWireSection section = application.getSection("COMET");
		if (section == null) {
			// Add the section
			section = application.addSection("COMET", ClassSectionSource.class,
					CometServiceLogic.class.getName());
		}

		// Obtain the template URI
		String templateUri = template.getTemplateUri();
		if ((templateUri == null) || (templateUri.trim().length() == 0)) {
			throw new IllegalArgumentException("Template '"
					+ template.getSectionName()
					+ " must have a URI as being extended by Comet services");
		}

		// Configure the Comet handling for the template
		application.linkUri(templateUri + "/comet-subscribe", section,
				"subscribe");
		application.linkUri(templateUri + "/comet-publish", section, "publish");
	}

	/**
	 * {@link ClassSectionSource} logic for {@link CometService}.
	 */
	public static class CometServiceLogic {

		/**
		 * Initially handles the {@link CometSubscriptionService}.
		 * 
		 * @param service
		 *            {@link CometService}.
		 */
		@NextTask("finished")
		public void subscribe(CometService service) {
			// Service subscription
			service.service();
		}

		/**
		 * Invoked once a {@link CometEvent} is available for the
		 * {@link CometSubscriptionService} or timed out waiting on a
		 * {@link CometEvent}.
		 * 
		 * @param service
		 *            {@link CometService}.
		 */
		public void finished(CometService service) {
			// Waits for CometEvent to be available for subscription
		}

		/**
		 * Handles the {@link CometPublicationService}.
		 * 
		 * @param connection
		 *            {@link ServerGwtRpcConnection} to notify client that
		 *            published.
		 * @param service
		 *            {@link CometService}.
		 */
		public void publish(ServerGwtRpcConnection<Long> connection,
				CometService service) {

			long sequenceNumber = -1;
			try {

				// Obtain the published event
				RPCRequest request = connection.getRpcRequest();
				CometEvent event = (CometEvent) request.getParameters()[0];
				Class<?> listenerType = Class.forName(event
						.getListenerTypeName());

				// Publish the event
				sequenceNumber = service.publishEvent(
						event.getSequenceNumber(), listenerType,
						event.getData(), event.getFilterKey());

			} catch (Exception ex) {
				// Failed to publish event
				connection.onFailure(ex);
				return; // Not successful
			}

			// Notify successfully published
			connection.onSuccess(Long.valueOf(sequenceNumber));
		}
	}

}