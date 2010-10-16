/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.webxml;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.servlet.container.source.HttpServletTask;
import net.officefloor.plugin.servlet.container.source.HttpServletWorkSource;
import net.officefloor.plugin.servlet.container.source.HttpServletTask.DependencyKeys;
import net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.route.ServletRouteTask.FlowKeys;
import net.officefloor.plugin.servlet.route.source.ServletRouteWorkSource;
import net.officefloor.plugin.servlet.webxml.model.InitParamModel;
import net.officefloor.plugin.servlet.webxml.model.ServletMappingModel;
import net.officefloor.plugin.servlet.webxml.model.ServletModel;
import net.officefloor.plugin.servlet.webxml.model.WebAppModel;
import net.officefloor.plugin.servlet.webxml.model.WebXmlLoader;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.session.HttpSession;

/**
 * {@link SectionSource} to load {@link HttpServlet} functionality from a
 * <code>web.xml</code> file (as per {@link Servlet} specification).
 * 
 * @author Daniel Sagenschneider
 */
public class WebXmlSectionSource extends AbstractSectionSource {

	/*
	 * ===================== SectionSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required, using location for web.xml
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Load the web.xml configuration
		String webXmlLocation = context.getSectionLocation();
		WebAppModel webApp = new WebXmlLoader().loadConfiguration(
				webXmlLocation, context);

		// Configure the input flow
		SectionInput serviceInput = designer.addSectionInput("service", null);

		// Configure the output flows
		SectionOutput unhandledOutput = designer.addSectionOutput("unhandled",
				null, false);

		// Configure the exception flows
		SectionOutput servletExceptionOutput = designer.addSectionOutput(
				ServletException.class.getSimpleName(), ServletException.class
						.getName(), true);
		SectionOutput ioExceptionOutput = designer.addSectionOutput(
				IOException.class.getSimpleName(), IOException.class.getName(),
				true);

		// Configure the dependencies for the web application
		SectionObject servletServerMo = designer.addSectionObject(
				"SERVLET_SERVER", ServletServer.class.getName());
		SectionObject httpConnectionMo = designer.addSectionObject(
				"HTTP_CONNECTION", ServerHttpConnection.class.getName());
		SectionObject requestAttributesMo = designer.addSectionObject(
				"REQUEST_ATTRIBUTES", Map.class.getName());
		SectionObject httpSessionMo = designer.addSectionObject("HTTP_SESSION",
				HttpSession.class.getName());
		SectionObject httpSecurityMo = designer.addSectionObject(
				"HTTP_SECURITY", HttpSecurity.class.getName());

		// Obtain the servlet context name
		String servletContextName = (webApp.getDisplayName() == null ? "OfficeFloor"
				: webApp.getDisplayName());

		// Configure the Office Servlet Context
		SectionManagedObjectSource officeServletContextMos = designer
				.addSectionManagedObjectSource("OfficeServletContext",
						OfficeServletContextManagedObjectSource.class.getName());
		officeServletContextMos
				.addProperty(
						OfficeServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME,
						servletContextName);
		SectionManagedObject officeServletContextMo = officeServletContextMos
				.addSectionManagedObject("OfficeServletContext",
						ManagedObjectScope.PROCESS);
		ManagedObjectDependency contextToServerDependency = officeServletContextMo
				.getManagedObjectDependency("SERVLET_SERVER");
		designer.link(contextToServerDependency, servletServerMo);

		// Provide servicer to route requests to Servlets
		SectionWork routeWork = designer.addSectionWork("Route",
				ServletRouteWorkSource.class.getName());
		SectionTask routeTask = routeWork.addSectionTask("route",
				ServletRouteWorkSource.TASK_ROUTE);
		designer.link(serviceInput, routeTask);
		designer.link(routeTask.getTaskFlow(FlowKeys.UNHANDLED.name()),
				unhandledOutput, FlowInstigationStrategyEnum.SEQUENTIAL);
		designer
				.link(
						routeTask
								.getTaskObject(net.officefloor.plugin.servlet.route.ServletRouteTask.DependencyKeys.HTTP_CONNECTION
										.name()), httpConnectionMo);
		designer
				.link(
						routeTask
								.getTaskObject(net.officefloor.plugin.servlet.route.ServletRouteTask.DependencyKeys.OFFICE_SERVLET_CONTEXT
										.name()), officeServletContextMo);

		// Create the listing of servlet mappings
		Map<String, String> servletMappings = new HashMap<String, String>();
		List<ServletMappingModel> servletMappingModels = webApp
				.getServletMappings();
		if (servletMappingModels.size() == 0) {
			// Must have at least one servlet-mapping
			designer
					.addIssue(
							"At least one <servlet-mapping/> element must be configured",
							AssetType.WORK, "servlet-mapping");
		}
		for (ServletMappingModel mappingModel : servletMappingModels) {
			String servletName = mappingModel.getServletName();
			String urlPatterns = servletMappings.get(servletName);
			for (String pattern : mappingModel.getUrlPatterns()) {
				urlPatterns = (urlPatterns == null ? "" : urlPatterns + ",")
						+ pattern;
			}
			servletMappings.put(servletName, urlPatterns);
		}

		// Configure the servlets
		List<ServletModel> servletModels = webApp.getServlets();
		if (servletModels.size() == 0) {
			// Must have at least one servlet
			designer.addIssue(
					"At least one <servlet/> element must be configured",
					AssetType.WORK, "servlet");
		}
		for (ServletModel servletModel : servletModels) {

			// Obtain servlet details
			String servletName = servletModel.getServletName();
			String servletClass = servletModel.getServletClass().trim();
			String servletUrlPatterns = servletMappings.get(servletName);

			// Configure the servlet work
			SectionWork servlet = designer.addSectionWork(servletName,
					HttpServletWorkSource.class.getName());
			servlet.addProperty(HttpServletWorkSource.PROPERTY_SERVLET_NAME,
					servletName);
			servlet.addProperty(
					HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
					servletClass);
			servlet.addProperty(
					HttpServletWorkSource.PROPERTY_SERVLET_MAPPINGS,
					servletUrlPatterns);

			// Configure init-param values for servlet
			for (InitParamModel initParam : servletModel.getInitParams()) {
				servlet.addProperty(
						HttpServletWorkSource.PROPERTY_PREFIX_INIT_PARAMETER
								+ initParam.getName(), initParam.getValue());
			}

			// Configure the servlet service task
			SectionTask servletTask = servlet.addSectionTask("service-by-"
					+ servletName, HttpServletTask.TASK_NAME);
			servletTask.getTaskObject(DependencyKeys.SERVICER_MAPPING.name())
					.flagAsParameter();

			// Configure servlet dependencies
			designer
					.link(
							servletTask
									.getTaskObject(DependencyKeys.OFFICE_SERVLET_CONTEXT
											.name()), officeServletContextMo);
			designer.link(servletTask
					.getTaskObject(DependencyKeys.HTTP_CONNECTION.name()),
					httpConnectionMo);
			designer.link(servletTask
					.getTaskObject(DependencyKeys.REQUEST_ATTRIBUTES.name()),
					requestAttributesMo);
			designer.link(servletTask.getTaskObject(DependencyKeys.HTTP_SESSION
					.name()), httpSessionMo);
			designer.link(servletTask
					.getTaskObject(DependencyKeys.HTTP_SECURITY.name()),
					httpSecurityMo);

			// Configure the escalations
			// TODO consider web.xml error handling for this functionality
			designer.link(servletTask.getTaskEscalation(ServletException.class
					.getName()), servletExceptionOutput,
					FlowInstigationStrategyEnum.SEQUENTIAL);
			designer.link(servletTask.getTaskEscalation(IOException.class
					.getName()), ioExceptionOutput,
					FlowInstigationStrategyEnum.SEQUENTIAL);
		}
	}

}