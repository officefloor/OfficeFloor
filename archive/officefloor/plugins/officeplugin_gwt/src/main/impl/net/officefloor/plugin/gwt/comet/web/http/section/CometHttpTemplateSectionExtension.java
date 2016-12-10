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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.plugin.gwt.comet.CometProxyPublisherManagedObjectSource;
import net.officefloor.plugin.gwt.comet.CometPublisher;
import net.officefloor.plugin.gwt.comet.CometPublisherInterface;
import net.officefloor.plugin.gwt.comet.CometPublisherManagedObjectSource;
import net.officefloor.plugin.gwt.comet.api.OfficeFloorComet;
import net.officefloor.plugin.gwt.comet.internal.CometEvent;
import net.officefloor.plugin.gwt.comet.internal.CometPublicationService;
import net.officefloor.plugin.gwt.comet.internal.CometSubscriptionService;
import net.officefloor.plugin.gwt.comet.section.CometSectionSource;
import net.officefloor.plugin.gwt.comet.spi.CometRequestServicer;
import net.officefloor.plugin.gwt.comet.spi.CometRequestServicerManagedObjectSource;
import net.officefloor.plugin.gwt.comet.spi.CometService;
import net.officefloor.plugin.gwt.comet.spi.CometServiceManagedObjectSource;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;
import net.officefloor.plugin.work.clazz.FlowInterface;

import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * <p>
 * {@link HttpTemplateSectionExtension} to include Comet.
 * <p>
 * Parameters on the {@link HttpTemplate} logic class that have their parameter
 * type be an interface an annotated with {@link CometPublisherInterface} will
 * have a dependency via a {@link Proxy} from the {@link CometPublisher}
 * injected. This will enable easier code for the server to publish
 * {@link CometEvent} instances to their respective {@link OfficeFloorComet}
 * clients.
 * 
 * @author Daniel Sagenschneider
 */
public class CometHttpTemplateSectionExtension implements
		HttpTemplateSectionExtension {

	/**
	 * <p>
	 * Name of the {@link Method} on the {@link HttpTemplate} logic class to
	 * handle the published {@link CometEvent}.
	 * <p>
	 * Specifying this method allows manually handling a published
	 * {@link CometEvent} which is necessary in a clustered environment (i.e.
	 * putting the {@link CometEvent} on a queue for all instances within the
	 * cluster to publish the {@link CometEvent});
	 * <P>
	 * The result of the specified {@link Method} should send a {@link Long}
	 * value to {@link ServerGwtRpcConnection#onSuccess(Object)} to indicate
	 * published (otherwise a no data response is sent which typically will
	 * cause the client to consider the publish as failed).
	 */
	public static final String PROPERTY_MANUAL_PUBLISH_METHOD_NAME = "manual.publish.method.name";

	/**
	 * Name of the {@link AutoWireSection} to service the
	 * {@link CometSubscriptionService} and {@link CometPublicationService}.
	 */
	public static final String COMET_SECTION_NAME = "COMET";

	/**
	 * Extends the {@link GwtModuleModel} for using {@link OfficeFloorComet}.
	 * 
	 * @param module
	 *            {@link GwtModuleModel}.
	 */
	public static void extendGwtModule(GwtModuleModel module) {
		// Inherit the GWT Comet functionality for the client
		module.addInherit(OfficeFloorComet.INHERIT_MODULE_NAME);
	}

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

		// Process scope the managed objects
		final ManagedObjectSourceWirer processScopeWirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.setManagedObjectScope(ManagedObjectScope.PROCESS);
			}
		};

		// Configure in as extension to HTTP template
		template.addTemplateExtension(CometHttpTemplateSectionExtension.class);

		// Determine if already configured Comet Service
		if (!application.isObjectAvailable(new AutoWire(CometService.class))) {
			// Configure the Comet Service
			application.addManagedObject(
					CometServiceManagedObjectSource.class.getName(),
					new ManagedObjectSourceWirer() {
						@Override
						public void wire(ManagedObjectSourceWirerContext context) {
							context.setManagedObjectScope(ManagedObjectScope.PROCESS);
							context.mapTeam(
									CometServiceManagedObjectSource.EXPIRE_TEAM_NAME,
									OnePersonTeamSource.class.getName());
						}
					}, new AutoWire(CometService.class)).setTimeout(600 * 1000);
		}

		// Determine if already configured CometRequestServicer
		if (!application.isObjectAvailable(new AutoWire(
				CometRequestServicer.class))) {
			// Configure the Comet Request Servicer
			application
					.addManagedObject(
							CometRequestServicerManagedObjectSource.class
									.getName(), processScopeWirer,
							new AutoWire(CometRequestServicer.class));
		}

		// Determine if already configured CometPublisher
		if (!application.isObjectAvailable(new AutoWire(CometPublisher.class))) {
			// Configure the Comet Publisher
			application.addManagedObject(
					CometPublisherManagedObjectSource.class.getName(),
					processScopeWirer, new AutoWire(CometPublisher.class));
		}

		// Obtain the section to service Comet requests
		AutoWireSection section = application.getSection("COMET");
		if (section == null) {
			// Add the section
			section = application.addSection("COMET",
					CometSectionSource.class.getName(), "COMET");
		}

		// Obtain the template URI
		String templateUri = template.getTemplateUri();
		if ((templateUri == null) || (templateUri.trim().length() == 0)) {
			throw new IllegalArgumentException("Template '"
					+ template.getSectionName()
					+ " must have a URI as being extended by Comet services");
		}

		// Obtain the GWT module name
		String gwtModuleName = GwtHttpTemplateSectionExtension
				.getGwtModuleName(templateUri);

		// Determine if manually publishing
		String manualPublishMethodName = properties.getProperty(
				PROPERTY_MANUAL_PUBLISH_METHOD_NAME, null);
		if (manualPublishMethodName != null) {

			// Manually handle publishing, strip template URI to flow name
			String flowName = gwtModuleName.replace("/", "");

			// Configure work for manually handling publish
			String propertyName = CometSectionSource.PROPERTY_MANUAL_PUBLISH_URI_PREFIX
					+ flowName;
			section.addProperty(propertyName, gwtModuleName);

			// Link manual publish handling to template logic method
			String outputFlowName = CometSectionSource.PUBLISH_OUTPUT_PREFIX
					+ flowName;
			application.link(section, outputFlowName, template,
					manualPublishMethodName);
		}

		// Configure the Comet handling for the template
		application.linkUri(gwtModuleName + "/comet-subscribe", section,
				CometSectionSource.SUBSCRIBE_INPUT_NAME);
		application.linkUri(gwtModuleName + "/comet-publish", section,
				CometSectionSource.PUBLISH_INPUT_NAME);

		// Provide any CometPublisherInterface proxies as necessary
		for (Method method : template.getTemplateLogicClass().getMethods()) {
			for (Class<?> parameterType : method.getParameterTypes()) {
				if (parameterType
						.isAnnotationPresent(CometPublisherInterface.class)) {
					// CometPublisherInterface, so register once
					if (!application.isObjectAvailable(new AutoWire(
							parameterType))) {
						application
								.addManagedObject(
										CometProxyPublisherManagedObjectSource.class
												.getName(), processScopeWirer,
										new AutoWire(parameterType))
								.addProperty(
										CometProxyPublisherManagedObjectSource.PROPERTY_PROXY_INTERFACE,
										parameterType.getName());
					}
				}
			}
		}
	}

	/**
	 * Flow interface to publish the {@link CometEvent}.
	 */
	@FlowInterface
	public static interface PublishFlow {

		/**
		 * Triggers publishing the {@link CometEvent}.
		 * 
		 * @param event
		 *            {@link CometEvent}.
		 */
		void publish(CometEvent event);
	}

	/**
	 * {@link ClassSectionSource} logic for {@link CometService}.
	 */
	public static class CometServiceLogic {

		/**
		 * Initially handles the {@link CometSubscriptionService}.
		 * 
		 * @param servicer
		 *            {@link CometRequestServicer}.
		 */
		@NextTask("finished")
		public void subscribe(CometRequestServicer servicer) {
			// Service subscription
			servicer.service();
		}

		/**
		 * Invoked once a {@link CometEvent} is available for the
		 * {@link CometSubscriptionService} or timed out waiting on a
		 * {@link CometEvent}.
		 * 
		 * @param servicer
		 *            {@link CometRequestServicer}.
		 */
		public void finished(CometRequestServicer servicer) {
			// Waits for CometEvent to be available for subscription
		}

		/**
		 * Handles the {@link CometPublicationService}.
		 * 
		 * @param connection
		 *            {@link ServerGwtRpcConnection} to notify client that
		 *            published.
		 * @param flow
		 *            {@link PublishFlow}.
		 */
		public void publish(ServerGwtRpcConnection<Long> connection,
				PublishFlow flow) {

			// Obtain the published event
			RPCRequest request = connection.getRpcRequest();
			CometEvent event = (CometEvent) request.getParameters()[0];

			// Publish the event
			flow.publish(event);
		}

		/**
		 * Automatically publishes the {@link CometEvent}.
		 * 
		 * @param event
		 *            {@link CometEvent}.
		 * @param service
		 *            {@link CometService}.
		 * @param connection
		 *            {@link ServerGwtRpcConnection}.
		 */
		public void automaticallyPublish(@Parameter CometEvent event,
				CometService service, ServerGwtRpcConnection<Long> connection) {
			long sequenceNumber = -1;
			try {

				// Publish the event
				sequenceNumber = service.publishEvent(
						event.getSequenceNumber(), event.getListenerTypeName(),
						event.getData(), event.getMatchKey());

			} catch (Exception ex) {
				// Failed to publish event
				connection.onFailure(ex);
				return; // Not successful
			}

			// Notify successfully published
			connection.onSuccess(Long.valueOf(sequenceNumber));
		}
	}

	/*
	 * ================== HttpTemplateSectionExtension =======================
	 */

	@Override
	public void extendTemplate(HttpTemplateSectionExtensionContext context)
			throws Exception {
		// No need to change template
	}

}