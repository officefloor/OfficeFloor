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

package net.officefloor.plugin.web.http.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseSendTask.HttpResponseSendTaskDependencies;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseSenderWorkSource;
import net.officefloor.plugin.web.http.resource.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.route.source.HttpRouteTask.HttpRouteTaskDependencies;
import net.officefloor.plugin.web.http.route.source.HttpRouteWorkSource;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteDependencies;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteTaskFlows;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteWorkSource;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

/**
 * Provides server functionality for the HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class WebApplicationSectionSource extends AbstractSectionSource {

	/**
	 * Name of the {@link SectionOutput} for servicing non-handled
	 * {@link HttpRequest} instances.
	 */
	public static final String UNHANDLED_REQUEST_OUTPUT_NAME = "unhandled-request";

	/**
	 * Name of the {@link SectionInput} to send the {@link HttpResponse}.
	 */
	public static final String SEND_RESPONSE_INPUT_NAME = "Send";

	/**
	 * Prefix on the {@link SectionOutput} name for the registered
	 * {@link HttpTemplateAutoWireSection} to allow linking flow.
	 */
	public static final String ROUTE_TO_HTTP_TEMPLATE_OUTPUT_URI_PREFIX = "Template_";

	/**
	 * Prefix on the {@link Task} name to service links.
	 */
	public static final String PROPERTY_LINK_SERVICE_TASK_NAME_PREFIX = "link.service.task.name.prefix";

	/**
	 * Name of property to indicating if routing is required.
	 */
	private static final String PROPERTY_IS_REQUIRE_ROUTING = "is.require.routing";

	/**
	 * Links URI to the {@link AutoWireSection} input.
	 * 
	 * @param uri
	 *            URI.
	 * @param section
	 *            {@link AutoWireSection} to handle the {@link HttpRequest}.
	 * @param inputName
	 *            Name of the {@link SectionInput}.
	 * @param httpSection
	 *            {@link AutoWireSection} for this
	 *            {@link WebApplicationSectionSource}.
	 * @param source
	 *            {@link WebApplicationAutoWireOfficeFloorSource}.
	 */
	public static void linkRouteToSection(String uri, AutoWireSection section,
			String inputName, AutoWireSection httpSection,
			WebApplicationAutoWireOfficeFloorSource source) {

		// Link route
		String sectionOutputName = linkRoute(httpSection, uri);

		// Link routing to HTTP template
		source.link(httpSection, sectionOutputName, section, inputName);
	}

	/**
	 * Links the {@link HttpTemplateAutoWireSection} for routing.
	 * 
	 * @param httpTemplate
	 *            {@link HttpTemplateAutoWireSection}
	 * @param httpSection
	 *            {@link AutoWireSection} for this
	 *            {@link WebApplicationSectionSource}.
	 * @param source
	 *            {@link WebApplicationAutoWireOfficeFloorSource}.
	 */
	public static void linkRouteToHttpTemplate(
			HttpTemplateAutoWireSection httpTemplate,
			AutoWireSection httpSection,
			WebApplicationAutoWireOfficeFloorSource source) {

		// Must have URI for routing
		String templateUri = httpTemplate.getTemplateUri();
		if (templateUri == null) {
			return; // no URI so not routed
		}

		// Link route
		String sectionOutputName = linkRoute(httpSection, templateUri);

		// Link routing to HTTP template
		source.link(httpSection, sectionOutputName, httpTemplate,
				HttpTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME);
	}

	/**
	 * Links the {@link TaskObject} to the {@link SectionObject}.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param objectName
	 *            Name of the object.
	 * @param objectType
	 *            Type of the object.
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param objects
	 *            Registry of the {@link SectionObject} by type of object.
	 */
	static void linkObject(SectionTask task, String objectName,
			Class<?> objectType, SectionDesigner designer,
			Map<Class<?>, SectionObject> objects) {

		// Obtain the section object
		SectionObject sectionObject = objects.get(objectType);
		if (sectionObject == null) {
			sectionObject = designer.addSectionObject(objectType.getName(),
					objectType.getName());
			objects.put(objectType, sectionObject);
		}

		// Link task object to section object
		TaskObject taskObject = task.getTaskObject(objectName);
		designer.link(taskObject, sectionObject);
	}

	/**
	 * Links an escalation to an {@link SectionOutput}.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param exception
	 *            Type of escalation.
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param escalations
	 *            Registry of the {@link SectionOutput} by type of escalation.
	 */
	static <E extends Throwable> void linkEscalation(SectionTask task,
			Class<E> exception, SectionDesigner designer,
			Map<Class<?>, SectionOutput> escalations) {

		// Obtain the section output
		SectionOutput output = escalations.get(exception);
		if (output == null) {
			output = designer.addSectionOutput(exception.getName(),
					exception.getName(), true);
			escalations.put(exception, output);
		}

		// Link task escalation to output
		TaskFlow escalation = task.getTaskEscalation(exception.getName());
		designer.link(escalation, output,
				FlowInstigationStrategyEnum.SEQUENTIAL);
	}

	/**
	 * Links the URI route to {@link SectionOutput}.
	 * 
	 * @param httpSection
	 *            {@link AutoWireSection} for this
	 *            {@link WebApplicationSectionSource}.
	 * @param uri
	 *            URI.
	 * @return {@link SectionOutput} name.
	 */
	private static String linkRoute(AutoWireSection httpSection, String uri) {

		// Flag requires routing
		flagToRoute(httpSection);

		// Determine the section output name
		String sectionOutputName = ROUTE_TO_HTTP_TEMPLATE_OUTPUT_URI_PREFIX
				+ uri;

		// Register HTTP template for routing
		httpSection.addProperty(
				HttpRouteWorkSource.PROPERTY_ROUTE_PREFIX + uri, uri);

		// Return the section output name
		return sectionOutputName;
	}

	/**
	 * Flags that requires routing.
	 * 
	 * @param httpSection
	 *            {@link AutoWireSection} for this
	 *            {@link WebApplicationSectionSource}.
	 */
	private static void flagToRoute(AutoWireSection httpSection) {

		final String TRUE = String.valueOf(true);

		// Ensure is require routing property is true
		for (Property property : httpSection.getProperties()) {
			if (PROPERTY_IS_REQUIRE_ROUTING.equals(property.getName())) {
				// Flag property to true as requires routing
				property.setValue(TRUE);
				return; // flagged true
			}
		}

		// As here property not added, so add
		httpSection.addProperty(PROPERTY_IS_REQUIRE_ROUTING, TRUE);
	}

	/*
	 * ======================= SectionSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		final Map<Class<?>, SectionObject> objects = new HashMap<Class<?>, SectionObject>();
		final Map<Class<?>, SectionOutput> escalations = new HashMap<Class<?>, SectionOutput>();

		// Add the Link route task
		SectionWork routeLinkWork = designer.addSectionWork("LINK_ROUTE",
				HttpTemplateRouteWorkSource.class.getName());
		routeLinkWork.addProperty(
				HttpTemplateRouteWorkSource.PROPERTY_TASK_NAME_PREFIX, context
						.getProperty(PROPERTY_LINK_SERVICE_TASK_NAME_PREFIX,
								null));
		SectionTask routeLinkTask = routeLinkWork.addSectionTask("LINK_ROUTE",
				"route");
		linkObject(routeLinkTask,
				HttpTemplateRouteDependencies.SERVER_HTTP_CONNECTION.name(),
				ServerHttpConnection.class, designer, objects);
		linkEscalation(routeLinkTask, InvalidHttpRequestUriException.class,
				designer, escalations);
		linkEscalation(routeLinkTask, UnknownWorkException.class, designer,
				escalations);
		linkEscalation(routeLinkTask, UnknownTaskException.class, designer,
				escalations);
		linkEscalation(routeLinkTask, InvalidParameterTypeException.class,
				designer, escalations);

		// Link handling input to route link task
		SectionInput input = designer.addSectionInput(
				HttpServerAutoWireOfficeFloorSource.HANDLER_INPUT_NAME, null);
		designer.link(input, routeLinkTask);

		// Create the flow for not matching of link route task
		TaskFlow notLinkRouteFlow = routeLinkTask
				.getTaskFlow(HttpTemplateRouteTaskFlows.NON_MATCHED_REQUEST
						.name());

		// Create the non-handled section output
		SectionOutput unhandledRequest = designer.addSectionOutput(
				UNHANDLED_REQUEST_OUTPUT_NAME, null, false);

		// Add router for URI's
		SectionTask routeUriTask = null;
		boolean isRequireRouting = Boolean.valueOf(context.getProperty(
				PROPERTY_IS_REQUIRE_ROUTING, String.valueOf(false)));
		if (!isRequireRouting) {
			// Not require URI routing, always not handled
			designer.link(notLinkRouteFlow, unhandledRequest,
					FlowInstigationStrategyEnum.SEQUENTIAL);

		} else {
			// Add the URI route task
			SectionWork routeUriWork = designer.addSectionWork("URI_ROUTE",
					HttpRouteWorkSource.class.getName());
			routeUriTask = routeUriWork.addSectionTask("URI_ROUTE", "route");
			linkObject(routeUriTask,
					HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION.name(),
					ServerHttpConnection.class, designer, objects);
			linkEscalation(routeUriTask, InvalidHttpRequestUriException.class,
					designer, escalations);
			for (String propertyName : context.getPropertyNames()) {
				if (propertyName
						.startsWith(HttpRouteWorkSource.PROPERTY_ROUTE_PREFIX)) {

					// Configure the route
					String templateUri = context.getProperty(propertyName);
					String templateRoutePattern = (templateUri.startsWith("/") ? templateUri
							: "/" + templateUri);
					routeUriWork
							.addProperty(propertyName, templateRoutePattern);

					// Obtain the section output name
					String sectionOutputName = ROUTE_TO_HTTP_TEMPLATE_OUTPUT_URI_PREFIX
							+ templateUri;

					// Link routing to registered HTTP template
					TaskFlow routeFlow = routeUriTask.getTaskFlow(templateUri);
					SectionOutput routeOutput = designer.addSectionOutput(
							sectionOutputName, null, false);
					designer.link(routeFlow, routeOutput,
							FlowInstigationStrategyEnum.SEQUENTIAL);
				}
			}

			// Link URI routing if no match on route link task
			designer.link(notLinkRouteFlow, routeUriTask,
					FlowInstigationStrategyEnum.SEQUENTIAL);

			// Link non-routed URI request to look for file
			TaskFlow defaultRouteFlow = routeUriTask.getTaskFlow("default");
			designer.link(defaultRouteFlow, unhandledRequest,
					FlowInstigationStrategyEnum.SEQUENTIAL);
		}

		// Provide input to send HTTP response
		SectionWork sendResponseWork = designer.addSectionWork("SEND",
				HttpResponseSenderWorkSource.class.getName());
		SectionTask sendResponseTask = sendResponseWork.addSectionTask("SEND",
				"SEND");
		linkObject(sendResponseTask,
				HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION.name(),
				ServerHttpConnection.class, designer, objects);
		linkEscalation(sendResponseTask, IOException.class, designer,
				escalations);
		SectionInput sendResponseInput = designer.addSectionInput(
				SEND_RESPONSE_INPUT_NAME, null);
		designer.link(sendResponseInput, sendResponseTask);
	}

}