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

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Web {@link AutoWireApplication}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebAutoWireApplication extends AutoWireApplication {

	/**
	 * Name of the {@link OfficeSection} that handles the {@link HttpRequest}
	 * instances.
	 */
	static String HANDLER_SECTION_NAME = "HANDLE_HTTP_SECTION";

	/**
	 * Name of the {@link OfficeSectionInput} that handles the
	 * {@link HttpRequest} instances.
	 */
	static String HANDLER_INPUT_NAME = "HANDLE_HTTP_INPUT";

	/**
	 * Prefix directory path for public web resources to locate them on the
	 * class path.
	 */
	static final String WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX = "PUBLIC";

	/**
	 * Adds a {@link HttpTemplate} available at the specified URI path.
	 * 
	 * @param templateUri
	 *            URI path for the template.
	 * @param templateFilePath
	 *            Path to the template file.
	 * @param templateLogicClass
	 *            Class providing the logic for the template. May be
	 *            <code>null</code> if template does not require logic (e.g.
	 *            static page with links).
	 * @return {@link HttpTemplateAutoWireSection} to allow linking flows.
	 */
	HttpTemplateAutoWireSection addHttpTemplate(String templateUri,
			String templateFilePath, Class<?> templateLogicClass);

	/**
	 * Specifies the default URI suffix for the {@link HttpTemplate} URI path
	 * and link URI paths.
	 * 
	 * @param uriSuffix
	 *            Default {@link HttpTemplate} URI suffix.
	 */
	void setDefaultHttpTemplateUriSuffix(String uriSuffix);

	/**
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpApplicationState}.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @param bindName
	 *            Name to bind the object within the
	 *            {@link HttpApplicationState}.
	 * @return {@link AutoWireObject}.
	 */
	AutoWireObject addHttpApplicationObject(Class<?> objectClass,
			String bindName);

	/**
	 * <p>
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpApplicationState}.
	 * <p>
	 * The bound name is arbitrarily chosen but will be unique for the object.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @return {@link AutoWireObject}.
	 */
	AutoWireObject addHttpApplicationObject(Class<?> objectClass);

	/**
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpSession}.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @param bindName
	 *            Name to bind the object within the {@link HttpSession}.
	 * @return {@link AutoWireObject}.
	 */
	AutoWireObject addHttpSessionObject(Class<?> objectClass, String bindName);

	/**
	 * <p>
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpSession}.
	 * <p>
	 * The bound name is arbitrarily chosen but will be unique for the object.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @return {@link AutoWireObject}.
	 */
	AutoWireObject addHttpSessionObject(Class<?> objectClass);

	/**
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpRequestState}.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @param isLoadParameters
	 *            Indicates whether to load the HTTP parameters to instantiated
	 *            objects.
	 * @param bindName
	 *            Name to bind the object within the {@link HttpRequestState}.
	 * @return {@link AutoWireObject}.
	 */
	AutoWireObject addHttpRequestObject(Class<?> objectClass,
			boolean isLoadParameters, String bindName);

	/**
	 * <p>
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpRequestState}.
	 * <p>
	 * The bound name is arbitrarily chosen but will be unique for the object.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @param isLoadParameters
	 *            Indicates whether to load the HTTP parameters to instantiated
	 *            objects.
	 * @return {@link AutoWireObject}.
	 */
	AutoWireObject addHttpRequestObject(Class<?> objectClass,
			boolean isLoadParameters);

	/**
	 * Links a URI to an {@link OfficeSectionInput}.
	 * 
	 * @param uri
	 *            URI to be linked.
	 * @param section
	 *            {@link AutoWireSection} servicing the URI.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput} servicing the URI.
	 * @return {@link HttpUriLink} to configure handling the URI.
	 */
	HttpUriLink linkUri(String uri, AutoWireSection section, String inputName);

	/**
	 * <p>
	 * Obtains the linked URIs.
	 * <p>
	 * {@link HttpTemplateAutoWireSection} URIs are not included in this list.
	 * To determine if the {@link HttpTemplateAutoWireSection} is serviced (e.g.
	 * for embedding with a JEE server for Servlet mapping) use the template URI
	 * suffix.
	 * 
	 * @return Linked URIs.
	 */
	String[] getURIs();

	/**
	 * Links the {@link OfficeSectionOutput} to render the {@link HttpTemplate}.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput}.
	 * @param template
	 *            {@link HttpTemplateAutoWireSection}.
	 */
	void linkToHttpTemplate(AutoWireSection section, String outputName,
			HttpTemplateAutoWireSection template);

	/**
	 * <p>
	 * Links to a resource.
	 * <p>
	 * The meaning of resource path is specific to implementation.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput}.
	 * @param resourcePath
	 *            Resource path.
	 */
	void linkToResource(AutoWireSection section, String outputName,
			String resourcePath);

	/**
	 * Links the {@link Escalation} to be handled by the
	 * {@link HttpTemplateAutoWireSection}.
	 * 
	 * @param escalation
	 *            {@link Escalation}.
	 * @param template
	 *            {@link HttpTemplateAutoWireSection}.
	 */
	void linkEscalation(Class<? extends Throwable> escalation,
			HttpTemplateAutoWireSection template);

	/**
	 * Links the {@link Escalation} to be handled by the resource.
	 * 
	 * @param escalation
	 *            {@link Escalation}.
	 * @param resourcePath
	 *            Resource path.
	 */
	void linkEscalation(Class<? extends Throwable> escalation,
			String resourcePath);

	/**
	 * Links {@link OfficeSectionOutput} to sending the {@link HttpResponse}.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput}.
	 */
	void linkToSendResponse(AutoWireSection section, String outputName);

	/**
	 * <p>
	 * Chains a {@link OfficeSectionInput} to the end of the servicing chain to
	 * handle a {@link HttpRequest}.
	 * <p>
	 * The {@link WebAutoWireApplication} functionality is always the first in
	 * the chain to attempt to service the {@link HttpRequest}.
	 * <p>
	 * Typically the last in the chain is servicing the {@link HttpRequest} by
	 * sending a static resource by matching URI to resource name - and if no
	 * resource found, a not found error.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 * @param notHandledOutputName
	 *            Name of the {@link OfficeSectionOutput} should this servicer
	 *            not handle the {@link HttpRequest}. May be <code>null</code>
	 *            if handles all {@link HttpRequest} instances (any services
	 *            chained after this will therefore not be used).
	 */
	void chainServicer(AutoWireSection section, String inputName,
			String notHandledOutputName);

}