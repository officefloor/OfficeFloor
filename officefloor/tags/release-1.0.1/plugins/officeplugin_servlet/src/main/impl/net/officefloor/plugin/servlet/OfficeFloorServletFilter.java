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
package net.officefloor.plugin.servlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.plugin.autowire.AutoWireObject;
import net.officefloor.plugin.autowire.AutoWireOfficeFloor;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.servlet.bridge.ServletBridgeManagedObjectSource;
import net.officefloor.plugin.servlet.bridge.spi.ServletServiceBridger;
import net.officefloor.plugin.servlet.socket.server.http.ServletServerHttpConnection;
import net.officefloor.plugin.servlet.socket.server.http.source.ServletServerHttpConnectionManagedObjectSource;
import net.officefloor.plugin.servlet.web.http.session.ServletHttpSessionManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.server.WebApplicationAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link Filter} to invoke processing within an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class OfficeFloorServletFilter extends
		WebApplicationAutoWireOfficeFloorSource implements Filter {

	/**
	 * {@link FilterConfig}.
	 */
	private FilterConfig filterConfig;

	/**
	 * {@link ServletServiceBridger}.
	 */
	@SuppressWarnings("rawtypes")
	private ServletServiceBridger bridger;

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	/**
	 * {@link ServletResourceLink} instances.
	 */
	private final List<ServletResourceLink> servletResourceLinks = new LinkedList<ServletResourceLink>();

	/**
	 * Handled URIs.
	 */
	private final Set<String> handledURIs = new HashSet<String>();

	/**
	 * Obtains the {@link FilterConfig}.
	 * 
	 * @return {@link FilterConfig}.
	 */
	protected FilterConfig getFilterConfig() {
		return this.filterConfig;
	}

	/**
	 * Links {@link OfficeSectionOutput} to a {@link Servlet} container
	 * resource.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput}.
	 * @param requestDispatcherPath
	 *            Path for the {@link RequestDispatcher}.
	 */
	public void linkToServletResource(AutoWireSection section,
			String outputName, String requestDispatcherPath) {
		this.servletResourceLinks.add(new ServletResourceLink(section,
				outputName, requestDispatcherPath));
	}

	/**
	 * Provides configuration of this as a
	 * {@link WebApplicationAutoWireOfficeFloorSource}.
	 */
	protected abstract void configure();

	/*
	 * ===================== Filter =========================
	 */

	@Override
	public void init(FilterConfig config) throws ServletException {

		// Maintain reference to the filter configuration
		this.filterConfig = config;

		// Create the bridger for the Servlet container
		this.bridger = ServletBridgeManagedObjectSource
				.createServletServiceBridger(this.getClass(), this,
						HANDLER_SECTION_NAME, HANDLER_INPUT_NAME);

		// Configure Server HTTP connection
		this.addManagedObject(
				ServletServerHttpConnectionManagedObjectSource.class, null,
				ServerHttpConnection.class);

		// Configure the HTTP session
		this.addManagedObject(ServletHttpSessionManagedObjectSource.class,
				null, HttpSession.class);

		// Configure the Servlet container resource section
		AutoWireSection servletContainerResource = this.addSection(
				"SERVLET_CONTAINER_RESOURCE",
				ServletContainerResourceSectionSource.class, "NOT_HANDLED");
		this.setNonHandledServicer(servletContainerResource, "NOT_HANDLED");

		// Provide dependencies of Servlet
		Class<?>[] dependencyTypes = this.bridger.getObjectTypes();
		for (Class<?> dependencyType : dependencyTypes) {
			// Add Servlet dependency for dependency injection
			AutoWireObject dependency = this.addManagedObject(
					ServletDependencyManagedObjectSource.class, null,
					dependencyType);
			dependency.addProperty(
					ServletDependencyManagedObjectSource.PROPERTY_TYPE_NAME,
					dependencyType.getName());
		}

		// Process Context Team to ensure appropriate Thread for dependencies
		// (EJB's typically rely on ThreadLocal functionality)
		if (dependencyTypes.length > 0) {
			this.assignTeam(ProcessContextTeamSource.class, dependencyTypes);
		}

		// Configure the web application
		this.configure();

		// Load the handled URIs
		for (String uri : this.getURIs()) {
			uri = (uri.startsWith("/") ? uri : "/" + uri);
			this.handledURIs.add(uri);
		}

		// Link the Servlet Resources
		for (ServletResourceLink link : this.servletResourceLinks) {
			servletContainerResource.addProperty(link.requestDispatcherPath,
					link.requestDispatcherPath);
			this.link(link.section, link.outputName, servletContainerResource,
					link.requestDispatcherPath);
		}

		// Open the OfficeFloor
		try {
			this.officeFloor = this.openOfficeFloor();
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		// Obtain the HTTP request and response
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Obtain the URI
		String uri = ServletServerHttpConnection.getRequestUri(httpRequest);

		// Determine handling request (quick cache look-up to save invoking)
		if (this.handledURIs.contains(uri) || (uri.endsWith(".task"))) {

			// Handle by OfficeFloor
			this.bridger.service(this, httpRequest, httpResponse);

			// Determine if handled
			if (ServletContainerResourceSectionSource.completeServletService(
					httpRequest, httpResponse)) {
				return; // serviced
			}
		}

		// Not handled so continue on for Servlet container to handle
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// Ensure close OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * {@link Servlet} resource link.
	 */
	private static class ServletResourceLink {

		/**
		 * {@link AutoWireSection}.
		 */
		public final AutoWireSection section;

		/**
		 * {@link OfficeSectionOutput} name.
		 */
		public final String outputName;

		/**
		 * {@link RequestDispatcher} path.
		 */
		public final String requestDispatcherPath;

		/**
		 * Initiate.
		 * 
		 * @param section
		 *            {@link AutoWireSection}.
		 * @param outputName
		 *            {@link OfficeSectionOutput} name.
		 * @param requestDispatcherPath
		 *            {@link RequestDispatcher} path.
		 */
		public ServletResourceLink(AutoWireSection section, String outputName,
				String requestDispatcherPath) {
			this.section = section;
			this.outputName = outputName;
			this.requestDispatcherPath = requestDispatcherPath;
		}
	}

}