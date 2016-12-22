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
package net.officefloor.plugin.web.http.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseSendTask.HttpResponseSendTaskDependencies;
import net.officefloor.plugin.socket.server.http.response.source.HttpResponseSenderWorkSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.route.HttpRouteTask.HttpRouteTaskDependencies;
import net.officefloor.plugin.web.http.route.HttpRouteTask.HttpRouteTaskFlows;
import net.officefloor.plugin.web.http.route.HttpRouteWorkSource;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;

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
	 * Prefix on the {@link ManagedFunction} name to service links.
	 */
	public static final String PROPERTY_LINK_SERVICE_TASK_NAME_PREFIX = "link.service.task.name.prefix";

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

		// Add the Route task
		SectionWork routeWork = designer.addSectionWork("ROUTE",
				HttpRouteWorkSource.class.getName());
		SectionTask routeTask = routeWork.addSectionTask("ROUTE",
				HttpRouteWorkSource.TASK_NAME);
		linkObject(routeTask,
				HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION.name(),
				ServerHttpConnection.class, designer, objects);
		linkObject(routeTask,
				HttpRouteTaskDependencies.HTTP_APPLICATION_LOCATION.name(),
				HttpApplicationLocation.class, designer, objects);
		linkObject(routeTask, HttpRouteTaskDependencies.REQUEST_STATE.name(),
				HttpRequestState.class, designer, objects);
		linkObject(routeTask, HttpRouteTaskDependencies.HTTP_SESSION.name(),
				HttpSession.class, designer, objects);
		linkEscalation(routeTask, InvalidHttpRequestUriException.class,
				designer, escalations);
		linkEscalation(routeTask, HttpRequestTokeniseException.class, designer,
				escalations);
		linkEscalation(routeTask, IOException.class, designer, escalations);
		linkEscalation(routeTask, UnknownWorkException.class, designer,
				escalations);
		linkEscalation(routeTask, UnknownTaskException.class, designer,
				escalations);
		linkEscalation(routeTask, InvalidParameterTypeException.class,
				designer, escalations);

		// Link handling input to route task
		SectionInput input = designer.addSectionInput(
				HttpServerAutoWireOfficeFloorSource.HANDLER_INPUT_NAME, null);
		designer.link(input, routeTask);

		// Send to non-handled requests to not handled output
		TaskFlow unhandledServiceFlow = routeTask
				.getTaskFlow(HttpRouteTaskFlows.NOT_HANDLED.name());
		SectionOutput unhandledRequestOutput = designer.addSectionOutput(
				UNHANDLED_REQUEST_OUTPUT_NAME, null, false);
		designer.link(unhandledServiceFlow, unhandledRequestOutput,
				FlowInstigationStrategyEnum.SEQUENTIAL);

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