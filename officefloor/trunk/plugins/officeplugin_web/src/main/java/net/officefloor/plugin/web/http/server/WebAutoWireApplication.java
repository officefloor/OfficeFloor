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
package net.officefloor.plugin.web.http.server;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.plugin.autowire.AutoWireApplication;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
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
	 * Adds a {@link HttpTemplate} available at the specified URI.
	 * 
	 * @param templatePath
	 *            Path to the template file.
	 * @param templateLogicClass
	 *            Class providing the logic for the template.
	 * @param templateUri
	 *            URI for the template. May be <code>null</code> indicate the
	 *            template not publicly available.
	 * @return {@link HttpTemplateAutoWireSection} to allow linking flows.
	 */
	HttpTemplateAutoWireSection addHttpTemplate(String templatePath,
			Class<?> templateLogicClass, String templateUri);

	/**
	 * <p>
	 * Adds a private {@link HttpTemplate}.
	 * <p>
	 * The {@link HttpTemplate} is not directly available via URI but is linked
	 * by flows. This allows pre-processing before the {@link HttpTemplate} is
	 * attempted to be rendered.
	 * 
	 * @param templatePath
	 *            Path to the template file.
	 * @param templateLogicClass
	 *            Class providing the logic for the template.
	 * @return {@link HttpTemplateAutoWireSection} to allow linking flows.
	 */
	HttpTemplateAutoWireSection addHttpTemplate(String templatePath,
			Class<?> templateLogicClass);

	/**
	 * Links a URI to an {@link OfficeSectionInput}.
	 * 
	 * @param uri
	 *            URI to be linked.
	 * @param section
	 *            {@link AutoWireSection} servicing the URI.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput} servicing the URI.
	 */
	void linkUri(String uri, AutoWireSection section, String inputName);

	/**
	 * Obtains the registered URIs.
	 * 
	 * @return Registered URIs.
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
	 * Specifies the {@link OfficeSectionInput} to handle if unable to route
	 * {@link HttpRequest}.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 */
	void setNonHandledServicer(AutoWireSection section, String inputName);

}