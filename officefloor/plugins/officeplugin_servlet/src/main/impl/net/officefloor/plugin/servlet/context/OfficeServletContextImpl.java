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
package net.officefloor.plugin.servlet.context;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.servlet.container.IteratorEnumeration;
import net.officefloor.plugin.servlet.container.ServletRequestForwarder;
import net.officefloor.plugin.servlet.filter.FilterChainFactory;
import net.officefloor.plugin.servlet.filter.FilterChainFactoryImpl;
import net.officefloor.plugin.servlet.filter.FilterServicer;
import net.officefloor.plugin.servlet.filter.configuration.FilterServicersFactory;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.mapping.ServicerMapper;
import net.officefloor.plugin.servlet.mapping.ServicerMapperImpl;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;

/**
 * {@link OfficeServletContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeServletContextImpl implements OfficeServletContext {

	/**
	 * {@link ServletContext} name.
	 */
	private final String servletContextName;

	/**
	 * Init parameters.
	 */
	private final Map<String, String> initParameters;

	/**
	 * {@link FilterServicer} instances.
	 */
	private final FilterServicer[] filterServicers;

	/**
	 * Mapping of file extension to MIME type.
	 */
	private final Map<String, String> fileExtensionToMimeType;

	/**
	 * {@link OfficeContext} instances by its {@link Office}.
	 */
	private final Map<Office, OfficeContext> contexts = new HashMap<Office, OfficeContext>();

	/**
	 * {@link FilterChainFactory} instances by its {@link Office}.
	 */
	private final Map<Office, FilterChainFactory> filterChainFactories = new HashMap<Office, FilterChainFactory>();

	/**
	 * {@link ServletServer}.
	 */
	private volatile ServletServer servletServer;

	/**
	 * Initiate.
	 * 
	 * @param servletContextName
	 *            {@link ServletContext} name.
	 * @param initParameters
	 *            Init parameters.
	 * @param fileExtensionToMimeType
	 *            Mapping of file extension to MIME type.
	 * @param filterConfiguration
	 *            {@link Filter} configuration.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @throws ServletException
	 *             If fails to initialise.
	 */
	public OfficeServletContextImpl(String servletContextName,
			Map<String, String> initParameters,
			Map<String, String> fileExtensionToMimeType,
			Properties filterConfiguration, ClassLoader classLoader)
			throws ServletException {
		this.servletContextName = servletContextName;
		this.initParameters = initParameters;
		this.fileExtensionToMimeType = fileExtensionToMimeType;

		// Create the filter servicers from configuration
		this.filterServicers = new FilterServicersFactory()
				.createFilterServices(filterConfiguration, classLoader, this);
	}

	/**
	 * <p>
	 * Loads the {@link ServletServer}.
	 * <p>
	 * This method only uses the {@link ServletServer} on first invocation.
	 * After this point further invocations ignore the input
	 * {@link ServletServer}.
	 * 
	 * @param servletServer
	 *            {@link ServletServer}.
	 */
	public void init(ServletServer servletServer) {
		if (this.servletServer == null) {
			this.servletServer = servletServer;
		}
	}

	/**
	 * Obtains the {@link OfficeContext} for the {@link Office}.
	 * 
	 * @param office
	 *            {@link Office}.
	 * @return {@link OfficeContext} for the {@link Office}.
	 */
	private OfficeContext getOfficeContext(Office office) {
		synchronized (this.contexts) {

			// Lazy create the office context
			OfficeContext context = this.contexts.get(office);
			if (context == null) {

				// Obtain the listing of servicers for the office
				List<HttpServletServicer> servicers = new LinkedList<HttpServletServicer>();
				try {
					// Iterate over work of the Office
					for (String workName : office.getWorkNames()) {
						WorkManager work = office.getWorkManager(workName);

						// Iterate over tasks of the Office
						for (String taskName : work.getTaskNames()) {
							FunctionManager task = work.getTaskManager(taskName);

							// Load task if Servicer
							Object differentiator = task.getDifferentiator();
							if (differentiator instanceof HttpServletServicer) {
								HttpServletServicer httpServlet = (HttpServletServicer) differentiator;

								// Wrap servicer for Office details
								HttpServletServicer servicer = new OfficeServicer(
										workName, taskName, httpServlet);

								// Register the servicer
								servicers.add(servicer);
							}
						}
					}
				} catch (Exception ex) {
					// Failed loading but carry on
				}

				// Create the context for the office
				context = new OfficeContext(
						servicers.toArray(new HttpServletServicer[0]));

				// Register the context against the office
				this.contexts.put(office, context);
			}

			// Return the context
			return context;
		}
	}

	/*
	 * ====================== OfficeServletContext ======================
	 */

	@Override
	public FilterChainFactory getFilterChainFactory(Office office)
			throws ServletException {
		synchronized (this.filterChainFactories) {

			// Lazy create the filter chain factory
			FilterChainFactory factory = this.filterChainFactories.get(office);
			if (factory == null) {

				// Construct the filter chain factory
				factory = new FilterChainFactoryImpl(office,
						this.filterServicers);

				// Register the filter chain factory
				this.filterChainFactories.put(office, factory);
			}

			// Return the filter chain factory
			return factory;
		}
	}

	@Override
	public ServletFunctionReference mapPath(Office office, String path) {
		// Obtain the context
		OfficeContext context = this.getOfficeContext(office);

		// Obtain the servicer mapping
		ServicerMapping mapping = context.mapper.mapPath(path);
		if (mapping == null) {
			// No servicer for path
			return null;
		}

		// Downcast and return the Servlet task reference
		OfficeServicer officeServicer = (OfficeServicer) mapping.getServicer();
		return officeServicer;
	}

	@Override
	public String getContextPath(Office office) {
		return this.servletServer.getContextPath();
	}

	@Override
	public ServletContext getContext(Office office, String uripath) {
		// Always restrict access to other contexts
		return null;
	}

	@Override
	public String getMimeType(Office office, String file) {

		// Determine if file extension
		int extensionIndex = file.lastIndexOf('.');
		if (extensionIndex < 0) {
			// No file extension, so no MIME type
			return null;
		}

		// Obtain the file extension (+1 to ignore separator)
		String fileExtension = file.substring(extensionIndex + 1);

		// Obtain the MIME type for file extension
		String mimeType = this.fileExtensionToMimeType.get(fileExtension
				.toLowerCase());

		// Return the MIME type
		return mimeType;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Set getResourcePaths(Office office, String path) {
		return this.servletServer.getResourceLocator()
				.getResourceChildren(path);
	}

	@Override
	public URL getResource(Office office, String path)
			throws MalformedURLException {
		return this.servletServer.getResourceLocator().getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(Office office, String path) {
		return this.servletServer.getResourceLocator()
				.getResourceAsStream(path);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(Office office, String path) {
		// Obtain the context
		OfficeContext context = this.getOfficeContext(office);

		// Obtain the servicer mapping
		ServicerMapping mapping = context.mapper.mapPath(path);
		if (mapping == null) {
			// No servicer for path
			return null;
		}

		// Downcast and return the request dispatcher
		OfficeServicer officeServicer = (OfficeServicer) mapping.getServicer();
		return officeServicer.createRequestDispatcher(mapping);
	}

	@Override
	public RequestDispatcher getNamedDispatcher(Office office, String name) {
		// Obtain the context
		OfficeContext context = this.getOfficeContext(office);

		// Obtain the servicer for the name
		HttpServletServicer servicer = context.mapper.mapName(name);
		if (servicer == null) {
			// No servicer by name
			return null;
		}

		// Downcast and return the request dispatcher (no mapping as by name)
		OfficeServicer officeServicer = (OfficeServicer) servicer;
		return officeServicer.createRequestDispatcher(null);
	}

	@Override
	public void log(Office office, String msg) {
		this.servletServer.getLogger().log(msg);
	}

	@Override
	public void log(Office office, String message, Throwable throwable) {
		this.servletServer.getLogger().log(message, throwable);
	}

	@Override
	public String getRealPath(Office office, String path) {

		// Obtain the path to context
		String serverName = this.servletServer.getServerName();
		int serverPort = this.servletServer.getServerPort();
		String contextPath = this.servletServer.getContextPath();
		String realPath = "http://" + serverName
				+ (serverPort == 80 ? "" : ":" + serverPort) + contextPath;

		// Add path
		realPath = realPath + (path.startsWith("/") ? path : "/" + path);

		// Return the real path
		return realPath;
	}

	@Override
	public String getServerInfo(Office office) {
		return "OfficeFloor servlet plug-in/1.0";
	}

	@Override
	public String getInitParameter(Office office, String name) {
		return this.initParameters.get(name);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames(Office office) {
		return new IteratorEnumeration<String>(this.initParameters.keySet()
				.iterator());
	}

	@Override
	public Object getAttribute(Office office, String name) {
		// Obtain the context
		OfficeContext context = this.getOfficeContext(office);

		// Return the attribute
		synchronized (context) {
			return context.attributes.get(name);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames(Office office) {
		// Obtain the context
		OfficeContext context = this.getOfficeContext(office);

		// Return the attribute
		synchronized (context) {
			// Create copy of names to stop concurrency issues
			List<String> names = new ArrayList<String>(
					context.attributes.keySet());

			// Return the iterator over the names
			return new IteratorEnumeration<String>(names.iterator());
		}
	}

	@Override
	public void removeAttribute(Office office, String name) {
		// Obtain the context
		OfficeContext context = this.getOfficeContext(office);

		// Remove the attribute
		synchronized (context) {
			context.attributes.remove(name);
		}
	}

	@Override
	public void setAttribute(Office office, String name, Object object) {
		// Obtain the context
		OfficeContext context = this.getOfficeContext(office);

		// Specify the attribute
		synchronized (context) {
			context.attributes.put(name, object);
		}
	}

	@Override
	public String getServletContextName(Office office) {
		return this.servletContextName;
	}

	/**
	 * Context for {@link Office}.
	 */
	private static class OfficeContext {

		/**
		 * Attributes.
		 */
		public final Map<String, Object> attributes = new HashMap<String, Object>();

		/**
		 * {@link ServicerMapper}.
		 */
		public final ServicerMapper mapper;

		/**
		 * Initiate.
		 * 
		 * @param servletServicers
		 *            {@link HttpServletServicer} instances.
		 */
		public OfficeContext(HttpServletServicer[] servletServicers) {
			this.mapper = new ServicerMapperImpl(servletServicers);
		}
	}

	/**
	 * {@link Office} {@link HttpServletServicer}.
	 */
	private class OfficeServicer implements HttpServletServicer,
			ServletFunctionReference {

		/**
		 * Name of {@link Work} for forwarding.
		 */
		private final String workName;

		/**
		 * Name of {@link ManagedFunction} for forwarding.
		 */
		private final String taskName;

		/**
		 * {@link HttpServletServicer} to include {@link HttpServlet}.
		 */
		private final HttpServletServicer httpServlet;

		/**
		 * Initiate.
		 * 
		 * @param workName
		 *            Name of {@link Work} for forwarding.
		 * @param taskName
		 *            Name of {@link ManagedFunction} for forwarding.
		 * @param httpServlet
		 *            {@link HttpServletServicer} to include {@link HttpServlet}
		 *            .
		 */
		public OfficeServicer(String workName, String taskName,
				HttpServletServicer httpServlet) {
			this.workName = workName;
			this.taskName = taskName;
			this.httpServlet = httpServlet;
		}

		/**
		 * Creates the {@link RequestDispatcher}.
		 * 
		 * @param mapping
		 *            {@link ServicerMapping}. May be <code>null</code>.
		 * @return {@link RequestDispatcher}.
		 */
		public RequestDispatcher createRequestDispatcher(ServicerMapping mapping) {
			// Create the Request Dispatcher
			return new OfficeRequestDispatcher(this.workName, this.taskName,
					this.httpServlet, mapping);
		}

		/*
		 * ======================== Servicer ==========================
		 */

		@Override
		public String getServletName() {
			return this.httpServlet.getServletName();
		}

		@Override
		public String[] getServletMappings() {
			return this.httpServlet.getServletMappings();
		}

		@Override
		public void include(OfficeServletContext context,
				HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			this.httpServlet.include(context, request, response);
		}

		/*
		 * ================== ServletTaskReference ==========================
		 */

		@Override
		public String getWorkName() {
			return this.workName;
		}

		@Override
		public String getFunctionName() {
			return this.taskName;
		}
	}

	/**
	 * {@link RequestDispatcher} implementation to route to {@link HttpServlet}
	 * instances within the {@link Office}.
	 */
	private class OfficeRequestDispatcher implements RequestDispatcher {

		/**
		 * Name of {@link Work} for forwarding.
		 */
		private final String workName;

		/**
		 * Name of {@link ManagedFunction} for forwarding.
		 */
		private final String taskName;

		/**
		 * {@link HttpServletServicer} to include {@link HttpServlet}.
		 */
		private final HttpServletServicer httpServlet;

		/**
		 * {@link ServicerMapping}.
		 */
		private final ServicerMapping mapping;

		/**
		 * Initiate.
		 * 
		 * @param workName
		 *            Name of {@link Work} for forwarding.
		 * @param taskName
		 *            Name of {@link ManagedFunction} for forwarding.
		 * @param httpServlet
		 *            {@link HttpServletServicer} to include {@link HttpServlet}
		 *            .
		 * @param mapping
		 *            {@link ServicerMapping}.
		 */
		public OfficeRequestDispatcher(String workName, String taskName,
				HttpServletServicer httpServlet, ServicerMapping mapping) {
			this.workName = workName;
			this.taskName = taskName;
			this.httpServlet = httpServlet;
			this.mapping = mapping;
		}

		/*
		 * ==================== RequestDispatcher ============================
		 */

		@Override
		public void forward(ServletRequest request, ServletResponse response)
				throws ServletException, IOException {

			// Obtain the forwarder
			ServletRequestForwarder forwarder = null;
			Object object = request
					.getAttribute(ServletRequestForwarder.ATTRIBUTE_FORWARDER);
			if (object instanceof ServletRequestForwarder) {
				forwarder = (ServletRequestForwarder) object;
			}

			// Ensure have forwarder
			if (forwarder == null) {
				throw new IllegalStateException(
						ServletRequestForwarder.class.getSimpleName()
								+ " must be available from the "
								+ ServletRequest.class.getSimpleName());
			}

			// Forward the Servlet Request
			forwarder.forward(this.workName, this.taskName, this.mapping);
		}

		@Override
		public void include(ServletRequest request, ServletResponse response)
				throws ServletException, IOException {

			// Downcast to HTTP request/response
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			// Provide mapping wrapper (not available if via named dispatch)
			if (this.mapping != null) {
				httpRequest = new MappedHttpServletRequest(this.mapping,
						httpRequest);
			}

			// Include the HTTP Servlet
			this.httpServlet.include(OfficeServletContextImpl.this,
					httpRequest, httpResponse);
		}
	}

}