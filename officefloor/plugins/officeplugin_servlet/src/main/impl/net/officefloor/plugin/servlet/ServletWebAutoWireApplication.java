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
package net.officefloor.plugin.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.plugin.servlet.bridge.ServletBridgeManagedObjectSource;
import net.officefloor.plugin.servlet.bridge.spi.ServletServiceBridger;
import net.officefloor.plugin.servlet.socket.server.http.source.ServletServerHttpConnectionManagedObjectSource;
import net.officefloor.plugin.servlet.web.http.application.ServletHttpApplicationStateManagedObjectSource;
import net.officefloor.plugin.servlet.web.http.application.ServletHttpRequestStateManagedObjectSource;
import net.officefloor.plugin.servlet.web.http.session.ServletHttpSessionManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpApplicationState;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.WebArchitectEmployer;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link Servlet} {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletWebAutoWireApplication<S extends OfficeFloorServlet>
		extends WebArchitectEmployer implements
		WebArchitect {

	/**
	 * <p>
	 * Resets the registered {@link ServletWebAutoWireApplication} instances.
	 * <p>
	 * This should NOT be called in Production and is only to reset for testing.
	 */
	public static void reset() {
		synchronized (registeredApplications) {

			// Clear the registered applications
			registeredApplications.clear();

			// Reset the application index
			nextApplicationIndex = 1;
		}
	}

	/**
	 * Registered {@link ServletWebAutoWireApplication} instances by their
	 * lookup index.
	 */
	private static Map<Integer, ServletWebAutoWireApplication<?>> registeredApplications = new HashMap<Integer, ServletWebAutoWireApplication<?>>();

	/**
	 * Next {@link ServletWebAutoWireApplication} index.
	 */
	private static int nextApplicationIndex = 1;

	/**
	 * Init property for the {@link OfficeFloorServlet} implementation that
	 * contains the index to the registered
	 * {@link ServletWebAutoWireApplication}.
	 */
	private static final String INIT_PROPERTY_APPLICATION_INDEX = "officefloorservlet.application.index";

	/**
	 * {@link ServletServiceBridger}.
	 */
	private ServletServiceBridger<S> bridger;

	/**
	 * {@link ServletResourceLink} instances.
	 */
	private final List<ServletResourceLink> servletResourceLinks = new LinkedList<ServletResourceLink>();

	/**
	 * {@link ServletResourceEscalation} instances.
	 */
	private final List<ServletResourceEscalation> servletResourceEscalations = new LinkedList<ServletResourceEscalation>();

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	/**
	 * <p>
	 * Configures the {@link OfficeFloorServlet} implementation into the
	 * {@link ServletContext}.
	 * <p>
	 * This is expected to be called from a {@link ServletContextListener} as it
	 * will configure an instance of the {@link OfficeFloorServlet} with
	 * appropriate URI mapping.
	 * 
	 * @param servletInitiateInstance
	 *            Implementing instance of the {@link OfficeFloorServlet}. This
	 *            instance is only used used for registration and configuration.
	 *            Its {@link Class} however is registered so that a new instance
	 *            is instantiated and allows injection of necessary resources to
	 *            occur.
	 * @param servletContext
	 *            {@link ServletContext}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static synchronized void configure(
			OfficeFloorServlet servletInitiateInstance,
			final ServletContext servletContext) {

		// Wirer to specify process scoping
		final ManagedObjectSourceWirer processScopeWirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.setManagedObjectScope(ManagedObjectScope.PROCESS);
			}
		};

		// Obtain the Servlet name
		String servletName = servletInitiateInstance.getServletName();

		// Obtain the implementing class of the OfficeFloorServlet
		Class servletClass = servletInitiateInstance.getClass();

		// Determine if Servlet already registered for name
		if (servletContext.getServletRegistration(servletName) != null) {
			// Not register as already registered
			servletContext.log("Not registering "
					+ OfficeFloorServlet.class.getSimpleName() + " "
					+ servletName + " (" + servletClass.getName() + ") as "
					+ Servlet.class.getSimpleName()
					+ " already registered under name");
			return;
		}

		// Create the instance of the application
		ServletWebAutoWireApplication<?> source = new ServletWebAutoWireApplication();

		// Create the bridger for the Servlet container
		source.bridger = ServletBridgeManagedObjectSource
				.createServletServiceBridger(servletClass, source,
						HANDLER_SECTION_NAME, HANDLER_INPUT_NAME);

		// Provide default suffix for link mapping
		String templateUriSuffix = servletInitiateInstance
				.getTemplateUriSuffix();
		if (templateUriSuffix == null) {
			servletContext
					.log("Failed to configure "
							+ servletClass.getName()
							+ " as it does not provide a template URI suffix. It will not be available.");
			return;
		}
		if (!(templateUriSuffix.startsWith("."))) {
			templateUriSuffix = "." + templateUriSuffix;
		}
		source.setDefaultHttpTemplateUriSuffix(templateUriSuffix);

		// Allow loading template content from ServletContext.
		// (Allows integration into the WAR structure)
		OfficeFloorCompiler compiler = source.getOfficeFloorCompiler();
		compiler.addResources(new ResourceSource() {
			@Override
			public InputStream sourceResource(String location) {

				// Ensure location is always absolute
				location = (location.startsWith("/") ? location : "/"
						+ location);

				// Attempt to obtain resource
				InputStream resource = servletContext
						.getResourceAsStream(location);

				// Return resource (if obtained)
				return resource;
			}
		});

		// Configure Server HTTP connection
		source.addManagedObject(
				ServletServerHttpConnectionManagedObjectSource.class.getName(),
				processScopeWirer, new AutoWire(ServerHttpConnection.class));

		// Configure the HTTP session
		source.addManagedObject(
				ServletHttpSessionManagedObjectSource.class.getName(),
				processScopeWirer, new AutoWire(HttpSession.class));

		// Configure the HTTP Application and Request State
		source.addManagedObject(
				ServletHttpApplicationStateManagedObjectSource.class.getName(),
				processScopeWirer, new AutoWire(HttpApplicationState.class));
		source.addManagedObject(
				ServletHttpRequestStateManagedObjectSource.class.getName(),
				processScopeWirer, new AutoWire(HttpRequestState.class));

		// Provide dependencies of Servlet
		Class<?>[] dependencyTypes = source.bridger.getObjectTypes();
		for (Class<?> dependencyType : dependencyTypes) {
			// Add Servlet dependency for dependency injection
			AutoWireObject dependency = source.addManagedObject(
					ServletDependencyManagedObjectSource.class.getName(),
					processScopeWirer, new AutoWire(dependencyType));
			dependency.addProperty(
					ServletDependencyManagedObjectSource.PROPERTY_TYPE_NAME,
					dependencyType.getName());
		}

		// Process Context Team to ensure appropriate Thread for dependencies
		// (EJB's typically rely on ThreadLocal functionality)
		if (dependencyTypes.length > 0) {

			// Create the auto-wiring for dependency types
			AutoWire[] autoWiring = new AutoWire[dependencyTypes.length];
			for (int i = 0; i < autoWiring.length; i++) {
				autoWiring[i] = new AutoWire(dependencyTypes[i]);
			}

			// Assign the team
			source.assignTeam(ProcessContextTeamSource.class.getName(),
					autoWiring);
		}

		// Configure the context path
		String contextPath = servletContext.getContextPath();
		compiler.addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_CONTEXT_PATH,
				contextPath);

		// Configure the web application
		try {
			boolean isConfigure = servletInitiateInstance.configure(source,
					servletContext);
			if (!isConfigure) {
				// Flagged not to configure
				return;
			}
		} catch (Exception ex) {
			// Indicate failure and not configured
			servletContext.log("Failed to configure " + servletClass.getName()
					+ ". It will not be available.", ex);
			return; // not configure
		}

		// Configure the Servlet container resource section
		AutoWireSection servletContainerResource = source.addSection(
				"SERVLET_CONTAINER_RESOURCE",
				ServletContainerResourceSectionSource.class.getName(),
				"NOT_HANDLED");
		source.chainServicer(servletContainerResource, "NOT_HANDLED", null);

		// Link the Servlet Resources
		for (ServletResourceLink link : source.servletResourceLinks) {
			servletContainerResource.addProperty(link.requestDispatcherPath,
					link.requestDispatcherPath);
			source.link(link.section, link.outputName,
					servletContainerResource, link.requestDispatcherPath);
		}

		// Link the escalation handling by Servlet Resources
		for (ServletResourceEscalation handling : source.servletResourceEscalations) {
			servletContainerResource.addProperty(
					handling.requestDispatcherPath,
					handling.requestDispatcherPath);
			source.linkEscalation(handling.escalationType,
					servletContainerResource, handling.requestDispatcherPath);
		}

		// Register the source (for initiation into an application)
		int applicationIndex;
		synchronized (registeredApplications) {

			// Obtain the index for the application
			applicationIndex = nextApplicationIndex++;

			// Register the application
			registeredApplications.put(Integer.valueOf(applicationIndex),
					source);
		}

		// Load the handled URIs (being absolute to domain)
		// (+1 to handle link mappings)
		String[] rawUris = source.getURIs();
		String[] mappedUris = new String[rawUris.length + 1];
		for (int i = 0; i < rawUris.length; i++) {

			// Obtain the URI
			String uri = rawUris[i];
			uri = (uri.startsWith("/") ? uri : "/" + uri);

			// Provide mapping of URIs
			mappedUris[i] = uri;

			// Prefix with context for handling
			if ((contextPath != null) && (!("/".equals(contextPath)))) {
				uri = contextPath + uri;
			}
		}

		// Include link mapping
		mappedUris[mappedUris.length - 1] = "*" + templateUriSuffix;

		// Provide Servlet and its configuration
		Dynamic servlet = servletContext.addServlet(servletName, servletClass);
		servlet.setAsyncSupported(true);
		servlet.setInitParameter(INIT_PROPERTY_APPLICATION_INDEX,
				String.valueOf(applicationIndex));
		servlet.addMapping(mappedUris);
		servlet.setLoadOnStartup(1);

		// Provide Filter to override DefaultServlet of Server
		javax.servlet.FilterRegistration.Dynamic filter = servletContext
				.addFilter(servletName, servletClass);
		filter.setAsyncSupported(true);
		filter.setInitParameter(INIT_PROPERTY_APPLICATION_INDEX,
				String.valueOf(applicationIndex));
		filter.addMappingForUrlPatterns(null, false, mappedUris);

		// Log that configured so that know if occurred
		StringBuilder logMessage = new StringBuilder();
		logMessage.append(servletName + " Servlet/Filter ("
				+ servletClass.getName() + ") loaded to service ");
		boolean isFirst = true;
		for (String mappedUri : mappedUris) {
			if (!isFirst) {
				logMessage.append(", ");
			}
			isFirst = false;
			logMessage.append(mappedUri);
		}
		servletContext.log(logMessage.toString());
	}

	/**
	 * <p>
	 * Initiates and returns the {@link ServletWebAutoWireApplication} for the
	 * {@link OfficeFloorServlet} instance.
	 * <p>
	 * This is expected to be called from the
	 * {@link Servlet#init(ServletConfig)} so that the
	 * {@link ServletConfig#getInitParameter(String)} is available.
	 * 
	 * @param <I>
	 *            {@link OfficeFloorServlet} type.
	 * @param servletInstance
	 *            {@link OfficeFloorServlet} instance.
	 * @return {@link ServletWebAutoWireApplication} for the
	 *         {@link OfficeFloorServlet} instance after it has been initiated.
	 * @throws ServletException
	 *             If fails to initiate.
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <I extends OfficeFloorServlet> ServletWebAutoWireApplication<I> initiate(
			I servletInstance) throws ServletException {

		// Obtain the application for the Servlet instance
		String applicationIndex = servletInstance
				.getInitParameter(INIT_PROPERTY_APPLICATION_INDEX);
		if (applicationIndex == null) {
			throw new ServletException(
					servletInstance.getClass().getName()
							+ " is not configured correctly as it has no corresponding "
							+ ServletWebAutoWireApplication.class
									.getSimpleName());
		}

		// Obtain the application
		ServletWebAutoWireApplication<I> application;
		synchronized (registeredApplications) {
			application = (ServletWebAutoWireApplication<I>) registeredApplications
					.get(Integer.valueOf(applicationIndex));
		}
		if (application == null) {
			throw new ServletException(
					servletInstance.getClass().getName()
							+ " is not configured correctly as it has no corresponding "
							+ ServletWebAutoWireApplication.class
									.getSimpleName());
		}

		// Only open the OfficeFloor once for the application
		// (called twice by filter instance and servlet instance)
		if (application.officeFloor == null) {
			try {
				application.officeFloor = application.openOfficeFloor();
			} catch (Exception ex) {
				throw new ServletException(ex);
			}
		}

		// Return the application
		return application;
	}

	/**
	 * Services the {@link HttpServletRequest}.
	 * 
	 * @param servletInstance
	 *            {@link OfficeFloorServlet} instance.
	 * @param request
	 *            {@link HttpServletRequest}.
	 * @param response
	 *            {@link HttpServletResponse}.
	 * @return <code>true</code> if serviced or <code>false</code> indicating
	 *         not handled.
	 * @throws ServletException
	 *             If fails to service {@link HttpServletRequest}.
	 * @throws IOException
	 *             If I/O failure in servicing the {@link HttpServletRequest}.
	 */
	public boolean service(S servletInstance, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// Handle by OfficeFloor
		this.bridger.service(servletInstance, request, response,
				request.getServletContext());

		// Determine if handled
		boolean isHandled = ServletContainerResourceSectionSource
				.completeServletService(request, response);

		// Return whether handled
		return isHandled;
	}

	/**
	 * Destroys this {@link ServletWebAutoWireApplication}.
	 */
	public void destroy() {
		synchronized (ServletWebAutoWireApplication.class) {

			// Ensure close OfficeFloor
			if (this.officeFloor != null) {

				// Close OfficeFloor
				this.officeFloor.closeOfficeFloor();

				// Release reference
				this.officeFloor = null;
			}
		}
	}

	/**
	 * May only initiate via static methods. Ensures correct construction.
	 */
	private ServletWebAutoWireApplication() {
	}

	/*
	 * ======================= WebAutoWireApplication =====================
	 */

	@Override
	public void linkToResource(AutoWireSection section, String outputName,
			String requestDispatcherPath) {

		// Override to use Servlet Container resource
		this.servletResourceLinks.add(new ServletResourceLink(section,
				outputName, requestDispatcherPath));
	}

	@Override
	public void linkEscalation(Class<? extends Throwable> escalation,
			String resourcePath) {

		// Override to use Servlet Container resource
		this.servletResourceEscalations.add(new ServletResourceEscalation(
				escalation, resourcePath));
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

	/**
	 * {@link Servlet} resource {@link Escalation}.
	 */
	private static class ServletResourceEscalation {

		/**
		 * {@link Escalation} type.
		 */
		public final Class<? extends Throwable> escalationType;

		/**
		 * {@link RequestDispatcher} path.
		 */
		public final String requestDispatcherPath;

		/**
		 * Initiate.
		 * 
		 * @param escalationType
		 *            {@link Escalation} type.
		 * @param requestDispatcherPath
		 *            {@link RequestDispatcher} path.
		 */
		public ServletResourceEscalation(
				Class<? extends Throwable> escalationType,
				String requestDispatcherPath) {
			this.escalationType = escalationType;
			this.requestDispatcherPath = requestDispatcherPath;
		}
	}

}