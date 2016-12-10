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
package net.officefloor.plugin.servlet.container;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.servlet.context.MappedHttpServletRequest;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.filter.FilterChainFactory;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;
import net.officefloor.plugin.servlet.security.HttpServletSecurity;
import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;

/**
 * {@link HttpServletContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletContainerImpl implements HttpServletContainer,
		HttpServletServicer, FilterChain {

	/**
	 * Name of attribute containing the last access time for the
	 * {@link HttpSession}.
	 */
	private static final String ATTRIBUTE_LAST_ACCESS_TIME = "#HttpServlet.LastAccessTime#";

	/**
	 * {@link Servlet} name.
	 */
	private final String servletName;

	/**
	 * {@link HttpServlet}.
	 */
	private final HttpServlet servlet;

	/**
	 * {@link FilterChainFactory}.
	 */
	private final FilterChainFactory filterChainFactory;

	/**
	 * {@link ServletContext}.
	 */
	private final ServletContext servletContext;

	/**
	 * {@link Clock}.
	 */
	private final Clock clock;

	/**
	 * Default {@link Locale}.
	 */
	private final Locale defaultLocale;

	/**
	 * Initiate.
	 * 
	 * @param servletName
	 *            Name of the Servlet.
	 * @param servlet
	 *            {@link HttpServlet}.
	 * @param initParameters
	 *            Init parameters for the {@link ServletConfig}.
	 * @param officeServletContext
	 *            {@link OfficeServletContext}.
	 * @param office
	 *            {@link Office}.
	 * @param clock
	 *            {@link Clock}.
	 * @param defaultLocale
	 *            Default {@link Locale}.
	 * @throws ServletException
	 *             If fails to initialise the {@link HttpServlet}.
	 */
	public HttpServletContainerImpl(String servletName, HttpServlet servlet,
			Map<String, String> initParameters,
			OfficeServletContext officeServletContext, Office office,
			Clock clock, Locale defaultLocale) throws ServletException {

		// Initiate state
		this.servletName = servletName;
		this.servlet = servlet;
		this.clock = clock;
		this.defaultLocale = defaultLocale;

		// Initiate the servlet context
		this.servletContext = new ServletContextImpl(officeServletContext,
				office);

		// Create the filter chain factory
		this.filterChainFactory = officeServletContext
				.getFilterChainFactory(office);

		// Initialise the servlet
		this.servlet.init(new ServletConfigImpl(servletName,
				this.servletContext, initParameters));
	}

	/**
	 * Services the {@link HttpServletRequest} with appropriate filtering.
	 * 
	 * @param mapping
	 *            {@link ServicerMapping}.
	 * @param mappingType
	 *            {@link MappingType}.
	 * @param request
	 *            {@link HttpServletRequest}.
	 * @param response
	 *            {@link HttpServletResponse}.
	 * @throws ServletException
	 *             As per {@link Servlet} API.
	 * @throws IOException
	 *             As per {@link Servlet} API.
	 */
	private void service(ServicerMapping mapping, MappingType mappingType,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Target the HTTP Servlet to service request
		FilterChain target = this;

		// Create the filter chain
		FilterChain filterChain = this.filterChainFactory.createFilterChain(
				mapping, mappingType, target);

		// Execute the filter chain to service request
		filterChain.doFilter(request, response);
	}

	/*
	 * ===================== HttpServletContainer ========================
	 */

	@Override
	public void service(ServerHttpConnection connection,
			HttpRequestState attributes, HttpSession session,
			HttpServletSecurity security, TaskContext<?, ?, ?> taskContext,
			ServicerMapping mapping) throws ServletException, IOException {

		// Obtain the last access time
		Long lastAccessTime = (Long) attributes
				.getAttribute(ATTRIBUTE_LAST_ACCESS_TIME);
		if (lastAccessTime == null) {
			// Not available for request, so use last request time
			lastAccessTime = (Long) session
					.getAttribute(ATTRIBUTE_LAST_ACCESS_TIME);
			long currentTime = this.clock.currentTimeMillis();
			if (lastAccessTime == null) {
				// No last request, so use current time
				lastAccessTime = new Long(currentTime);
			}

			// Update request with time so only the one time per request.
			// Above get will return value for further request servicing.
			attributes.setAttribute(ATTRIBUTE_LAST_ACCESS_TIME, lastAccessTime);

			// Update session for next request (with time of this request)
			session.setAttribute(ATTRIBUTE_LAST_ACCESS_TIME, new Long(
					currentTime));
		}

		HttpServletRequest request;
		HttpServletResponseImpl response;
		MappingType mappingType;
		try {
			// Create the HTTP session
			javax.servlet.http.HttpSession httpSession = new HttpSessionImpl(
					session, lastAccessTime.longValue(), this.clock,
					this.servletContext);

			// Obtain the session Id token name
			String sessionIdTokenName = session.getTokenName();

			// Create the HTTP request
			request = new HttpServletRequestImpl(connection, attributes,
					security, sessionIdTokenName, httpSession,
					this.servletContext, this.defaultLocale, taskContext);

			// If have mapping, wrap to provide servicer mapping values
			if (mapping != null) {
				// Override request with mapping details
				request = new MappedHttpServletRequest(mapping, request);

				// Servicer mapping provided so must be forward
				mappingType = MappingType.FORWARD;

			} else {
				// Provide request mapping
				mapping = new RequestServicerMapping(request);

				// No servicer mapping so must be request
				mappingType = MappingType.REQUEST;
			}

			// Create the HTTP response
			response = new HttpServletResponseImpl(
					connection.getHttpResponse(), this.clock, request,
					this.defaultLocale);

		} catch (HttpRequestTokeniseException ex) {
			// Propagate invalid HTTP Request
			throw new IOException(ex);
		}

		// Service the request
		this.service(mapping, mappingType, request, response);

		// Serviced so flush buffered content
		response.flushBuffers();
	}

	@Override
	public void include(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Create the Include mapping
		ServicerMapping mapping = new RequestServicerMapping(request);
		MappingType mappingType = MappingType.INCLUDE;

		// Include
		this.service(mapping, mappingType, request, response);
	}

	/*
	 * ========================= HttpServletServicer ===========================
	 */

	@Override
	public String getServletName() {
		return this.servletName;
	}

	@Override
	public String[] getServletMappings() {
		// Container does not handle mappings (only servicing)
		return new String[0];
	}

	@Override
	public void include(OfficeServletContext context,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.include(request, response);
	}

	/*
	 * ============================= FilterChain ===============================
	 */

	@Override
	public void doFilter(ServletRequest request, ServletResponse response)
			throws IOException, ServletException {
		// End of filter chain so service request with HTTP Servlet
		this.servlet.service(request, response);
	}

	/**
	 * {@link ServicerMapping} wrapping a {@link HttpServletRequest}.
	 */
	private class RequestServicerMapping implements ServicerMapping {

		/**
		 * Delegate {@link HttpServletRequest} being wrapped.
		 */
		private final HttpServletRequest request;

		/**
		 * Initiate.
		 * 
		 * @param request
		 *            Delegate {@link HttpServletRequest} being wrapped.
		 */
		public RequestServicerMapping(HttpServletRequest request) {
			this.request = request;
		}

		/*
		 * ===================== ServicerMapping ========================
		 */

		@Override
		public HttpServletServicer getServicer() {
			return HttpServletContainerImpl.this;
		}

		@Override
		public String getServletPath() {
			return this.request.getServletPath();
		}

		@Override
		public String getPathInfo() {
			return this.request.getPathInfo();
		}

		@Override
		public String getQueryString() {
			return this.request.getQueryString();
		}

		@Override
		public String getParameter(String name) {
			return this.request.getParameter(name);
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return this.request.getParameterMap();
		}

		@Override
		public Enumeration<String> getParameterNames() {
			return this.request.getParameterNames();
		}

		@Override
		public String[] getParameterValues(String name) {
			return this.getParameterValues(name);
		}
	}

}