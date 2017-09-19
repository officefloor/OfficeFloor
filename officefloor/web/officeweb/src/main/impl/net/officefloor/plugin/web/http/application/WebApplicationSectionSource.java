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

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteFunctionDependencies;
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteFunctionFlows;
import net.officefloor.plugin.web.http.route.HttpRouteManagedFunctionSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.response.source.HttpResponseSenderManagedFunctionSource;
import net.officefloor.server.http.response.source.HttpResponseSendFunction.HttpResponseSendTaskDependencies;

/**
 * Provides server functionality for the HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class WebApplicationSectionSource extends AbstractSectionSource {

	/**
	 * Property name for the {@link OfficeSectionInput} to service
	 * {@link ServerHttpConnection} instances.
	 */
	public static final String PROPERTY_SERVICE_INPUT_NAME = "service.input";

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
	 * {@link HttpTemplateSection} to allow linking flow.
	 */
	public static final String ROUTE_TO_HTTP_TEMPLATE_OUTPUT_URI_PREFIX = "Template_";

	/**
	 * Prefix on the {@link ManagedFunction} name to service links.
	 */
	public static final String PROPERTY_LINK_SERVICE_FUNCTION_NAME_PREFIX = "link.service.task.name.prefix";

	/**
	 * Links the {@link FunctionObject} to the {@link SectionObject}.
	 * 
	 * @param function
	 *            {@link SectionFunction}.
	 * @param objectName
	 *            Name of the object.
	 * @param objectType
	 *            Type of the object.
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param objects
	 *            Registry of the {@link SectionObject} by type of object.
	 */
	static void linkObject(SectionFunction function, String objectName, Class<?> objectType, SectionDesigner designer,
			Map<Class<?>, SectionObject> objects) {

		// Obtain the section object
		SectionObject sectionObject = objects.get(objectType);
		if (sectionObject == null) {
			sectionObject = designer.addSectionObject(objectType.getName(), objectType.getName());
			objects.put(objectType, sectionObject);
		}

		// Link function object to section object
		FunctionObject functionObject = function.getFunctionObject(objectName);
		designer.link(functionObject, sectionObject);
	}

	/**
	 * Links an escalation to an {@link SectionOutput}.
	 * 
	 * @param function
	 *            {@link SectionFunction}.
	 * @param exception
	 *            Type of escalation.
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param escalations
	 *            Registry of the {@link SectionOutput} by type of escalation.
	 */
	static <E extends Throwable> void linkEscalation(SectionFunction function, Class<E> exception,
			SectionDesigner designer, Map<Class<?>, SectionOutput> escalations) {

		// Obtain the section output
		SectionOutput output = escalations.get(exception);
		if (output == null) {
			output = designer.addSectionOutput(exception.getName(), exception.getName(), true);
			escalations.put(exception, output);
		}

		// Link function escalation to output
		FunctionFlow escalation = function.getFunctionEscalation(exception.getName());
		designer.link(escalation, output, false);
	}

	/*
	 * ======================= SectionSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SERVICE_INPUT_NAME, "Service input");
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		final Map<Class<?>, SectionObject> objects = new HashMap<Class<?>, SectionObject>();
		final Map<Class<?>, SectionOutput> escalations = new HashMap<Class<?>, SectionOutput>();

		// Obtain the service input name
		String serviceInputName = context.getProperty(PROPERTY_SERVICE_INPUT_NAME);

		// Add the Route function
		SectionFunctionNamespace routeNamespace = designer.addSectionFunctionNamespace("ROUTE",
				HttpRouteManagedFunctionSource.class.getName());
		SectionFunction routeFunction = routeNamespace.addSectionFunction("ROUTE", HttpRouteManagedFunctionSource.FUNCTION_NAME);
		linkObject(routeFunction, HttpRouteFunctionDependencies.SERVER_HTTP_CONNECTION.name(), ServerHttpConnection.class,
				designer, objects);
		linkObject(routeFunction, HttpRouteFunctionDependencies.HTTP_APPLICATION_LOCATION.name(),
				HttpApplicationLocation.class, designer, objects);
		linkObject(routeFunction, HttpRouteFunctionDependencies.REQUEST_STATE.name(), HttpRequestState.class, designer,
				objects);
		linkObject(routeFunction, HttpRouteFunctionDependencies.HTTP_SESSION.name(), HttpSession.class, designer, objects);
		linkEscalation(routeFunction, InvalidHttpRequestUriException.class, designer, escalations);
		linkEscalation(routeFunction, HttpRequestTokeniseException.class, designer, escalations);
		linkEscalation(routeFunction, IOException.class, designer, escalations);
		linkEscalation(routeFunction, UnknownFunctionException.class, designer, escalations);
		linkEscalation(routeFunction, InvalidParameterTypeException.class, designer, escalations);

		// Link service input to route function
		SectionInput input = designer.addSectionInput(serviceInputName, null);
		designer.link(input, routeFunction);

		// Send to non-handled requests to not handled output
		FunctionFlow unhandledServiceFlow = routeFunction.getFunctionFlow(HttpRouteFunctionFlows.NOT_HANDLED.name());
		SectionOutput unhandledRequestOutput = designer.addSectionOutput(UNHANDLED_REQUEST_OUTPUT_NAME, null, false);
		designer.link(unhandledServiceFlow, unhandledRequestOutput, false);

		// Provide input to send HTTP response
		SectionFunctionNamespace sendResponseNamespace = designer.addSectionFunctionNamespace("SEND",
				HttpResponseSenderManagedFunctionSource.class.getName());
		SectionFunction sendResponseFunction = sendResponseNamespace.addSectionFunction("SEND", "SEND");
		linkObject(sendResponseFunction, HttpResponseSendTaskDependencies.SERVER_HTTP_CONNECTION.name(),
				ServerHttpConnection.class, designer, objects);
		linkEscalation(sendResponseFunction, IOException.class, designer, escalations);
		SectionInput sendResponseInput = designer.addSectionInput(SEND_RESPONSE_INPUT_NAME, null);
		designer.link(sendResponseInput, sendResponseFunction);
	}

}