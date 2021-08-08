/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.servlet.tomcat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.tomcat.util.scan.StandardJarScanner;

import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.servlet.FilterServicer;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.inject.InjectContext;
import net.officefloor.servlet.inject.InjectContextFactory;
import net.officefloor.servlet.inject.InjectionRegistry;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * {@link Tomcat} {@link ServletServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TomcatServletManager implements ServletManager, ServletServicer {

	/**
	 * Operation to run.
	 */
	@FunctionalInterface
	public static interface Operation<R, T extends Throwable> {

		/**
		 * Logic of operation.
		 * 
		 * @return Result.
		 * @throws T Possible failure.
		 */
		R run() throws T;
	}

	/**
	 * Indicates if within Maven <code>war</code> project.
	 */
	private static final ThreadLocal<Boolean> isWithinMavenWarProject = new ThreadLocal<>();

	/**
	 * Runs the {@link Operation} assuming within Maven <code>war</code> project.
	 * 
	 * @param <R>       Return type.
	 * @param <T>       Possible exception type.
	 * @param operation {@link Operation}.
	 * @return Result.
	 * @throws T Possible failure.
	 */
	public static <R, T extends Throwable> R runInMavenWarProject(Operation<R, T> operation) throws T {
		Boolean original = isWithinMavenWarProject.get();
		try {
			// Flag within war project
			isWithinMavenWarProject.set(Boolean.TRUE);

			// Undertake operation
			return operation.run();

		} finally {
			// Determine if clear (as specified)
			if (original == null) {
				isWithinMavenWarProject.remove();
			}
		}
	}

	/**
	 * {@link ThreadLocal} for this {@link TomcatServletManager}.
	 */
	private static final ThreadLocal<TomcatServletManager> tomcatServletManager = new ThreadLocal<>();

	/**
	 * {@link Tomcat} for embedded {@link Servlet} container.
	 */
	private final Tomcat tomcat;

	/**
	 * {@link Connector}.
	 */
	private final Connector connector;

	/**
	 * {@link Context}.
	 */
	private final Context context;

	/**
	 * {@link InjectionRegistry}.
	 */
	private final InjectionRegistry injectionRegistry;

	/**
	 * {@link OfficeExtensionContext}.
	 */
	private final OfficeExtensionContext sourceContext;

	/**
	 * {@link OfficeFloorProtocol}.
	 */
	private final OfficeFloorProtocol protocol;

	/**
	 * Registered {@link Servlet} instances.
	 */
	private final Map<String, ServletServicer> registeredServlets = new HashMap<>();

	/**
	 * Registered {@link Filter} instances.
	 */
	private final Map<String, FilterServicer> registeredFilters = new HashMap<>();

	/**
	 * {@link Servlet} instances that require to have dependencies injected.
	 */
	private final List<Servlet> servletInstancesForDependencyInjection = new LinkedList<>();

	/**
	 * {@link SupplierSourceContext}.
	 */
	private SupplierSourceContext supplierSourceContext;

	/**
	 * Indicates if to chain in this {@link ServletManager}.
	 */
	private boolean isChainInServletManager = false;

	/**
	 * Indicates if chain decision made, so no longer able to flag.
	 */
	private boolean isChainDecisionMade = false;

	/**
	 * {@link InjectContextFactory}.
	 */
	private InjectContextFactory injectContextFactory;

	/**
	 * {@link AvailableType} instances.
	 */
	private AvailableType[] availableTypes;

	/**
	 * Instantiate.
	 * 
	 * @param contextPath       Context path.
	 * @param injectionRegistry {@link InjectionRegistry}.
	 * @param sourceContext     {@link OfficeExtensionContext}.
	 * @param webAppPath        Path to web application (WAR). May be
	 *                          <code>null</code>.
	 * @throws IOException If fails to setup container.
	 */
	public TomcatServletManager(String contextPath, InjectionRegistry injectionRegistry,
			OfficeExtensionContext sourceContext, String webAppPath) throws IOException {
		this.injectionRegistry = injectionRegistry;
		this.sourceContext = sourceContext;

		// Create OfficeFloor connector
		this.connector = new Connector(OfficeFloorProtocol.class.getName());
		this.connector.setPort(1);
		this.connector.setThrowOnFailure(true);

		// Obtain the username
		String username = System.getProperty("user.name");

		// Create the base directory (and directory for expanding)
		Path baseDir = Files.createTempDirectory(username + "_tomcat_base");
		Path webAppsDir = baseDir.resolve("webapps");
		Files.createDirectories(webAppsDir);

		// Setup tomcat
		this.tomcat = new Tomcat();
		this.tomcat.setBaseDir(baseDir.toAbsolutePath().toString());
		this.tomcat.setConnector(this.connector);
		this.tomcat.getHost().setAutoDeploy(false);

		// Configure webapp directory
		if (webAppPath == null) {
			Path tempWebApp = Files.createTempDirectory(username + "_webapp");
			webAppPath = tempWebApp.toAbsolutePath().toString();
		}

		// Create the context
		String contextName = ((contextPath == null) || (contextPath.equals("/"))) ? "" : contextPath;
		this.context = this.tomcat.addWebapp(contextName, webAppPath);

		// Configure context
		StandardJarScanner jarScanner = (StandardJarScanner) this.context.getJarScanner();
		jarScanner.setScanManifest(false);

		// Obtain OfficeFloor protocol to input request
		this.protocol = (OfficeFloorProtocol) this.connector.getProtocolHandler();

		// Listen for setup
		tomcatServletManager.set(this);
		this.context.addApplicationListener(SetupApplicationListener.class.getName());

		// Determine if load for running in Maven war project
		if (isWithinMavenWarProject.get() != null) {
			WebResourceRoot resources = new StandardRoot(this.context);
			resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
					new File("target/test-classes").getAbsolutePath(), "/"));
			resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
					new File("target/classes").getAbsolutePath(), "/"));
			this.context.setResources(resources);
		}
	}

	public static class SetupApplicationListener implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext servletContext = sce.getServletContext();

			// Register servlets
			servletContext.getServletRegistrations().forEach((name, registration) -> {
				try {
					ServletSupplierSource.registerForInjection(registration.getClassName());
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});

			// Register filters
			servletContext.getFilterRegistrations().forEach((name, registration) -> {
				try {
					ServletSupplierSource.registerForInjection(registration.getClassName());
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});

			// Load the instance manager
			TomcatServletManager servletManager = tomcatServletManager.get();
			InjectContextFactory factory = servletManager.injectionRegistry.createInjectContextFactory();
			servletManager.context.setInstanceManager(
					new OfficeFloorInstanceManager(factory, servletManager.sourceContext.getClassLoader()));
			tomcatServletManager.remove();
		}
	}

	/**
	 * Specifies the {@link SupplierSourceContext}.
	 * 
	 * @param supplierSourceContext {@link SupplierSourceContext}.
	 */
	public void setSupplierSourceContext(SupplierSourceContext supplierSourceContext) {
		this.supplierSourceContext = supplierSourceContext;
	}

	/**
	 * Indicates if chain in the {@link ServletManager}.
	 * 
	 * @return <code>true</code> to chain in the {@link ServletManager}.
	 */
	public boolean isChainServletManager() {

		// Flag that decision made
		this.isChainDecisionMade = true;

		// Indicates if chain in servlet manager
		return this.isChainInServletManager;
	}

	/**
	 * Starts the {@link Servlet} container.
	 * 
	 * @param availableTypes {@link AvailableType} instances.
	 * @throws Exception If fails to start.
	 */
	public void start(AvailableType[] availableTypes) throws Exception {

		// Make available types available to servlet container
		this.availableTypes = availableTypes;

		// Start tomcat
		this.tomcat.start();

		// Instantiate context factory
		this.injectContextFactory = this.injectionRegistry.createInjectContextFactory();

		// Load dependencies to servlet instances
		for (Servlet servlet : this.servletInstancesForDependencyInjection) {
			this.injectContextFactory.injectDependencies(servlet);
		}
	}

	/**
	 * Stops the {@link Servlet} container.
	 * 
	 * @throws Exception If fails to stop.
	 */
	public void stop() throws Exception {
		this.tomcat.stop();
		this.tomcat.destroy();
	}

	/*
	 * ===================== ServletServicer =======================
	 */

	@Override
	public void service(ServerHttpConnection connection, Executor executor, AsynchronousFlow asynchronousFlow,
			AsynchronousFlowCompletion asynchronousFlowCompletion, Map<String, ? extends Object> attributes)
			throws Exception {
		this.service(connection, executor, asynchronousFlow, asynchronousFlowCompletion, attributes, null,
				this.protocol.getAdapter()::service);
	}

	/*
	 * ===================== ServletManager ========================
	 */

	@Override
	public Context getContext() {
		return this.context;
	}

	@Override
	public ServletServicer addServlet(String name, Class<? extends Servlet> servletClass, Consumer<Wrapper> decorator) {
		return this.addServlet(name, decorator, () -> Tomcat.addServlet(this.context, name, servletClass.getName()));
	}

	@Override
	public ServletServicer addServlet(String name, Servlet servlet, boolean isInjectDependencies,
			Consumer<Wrapper> decorator) {

		// Determine if register servlet for dependency injection
		if (isInjectDependencies) {
			this.servletInstancesForDependencyInjection.add(servlet);
		}

		// Add the servlet
		return this.addServlet(name, decorator, () -> Tomcat.addServlet(this.context, name, servlet));
	}

	/**
	 * Adds a {@link Servlet}.
	 * 
	 * @param name       Name of {@link Servlet}.
	 * @param decorator  Decorates the {@link Wrapper}.
	 * @param addServlet Adds the {@link Servlet} and provides resulting
	 *                   {@link Wrapper}.
	 * @return {@link ServletServicer} for the {@link Servlet}.
	 */
	private ServletServicer addServlet(String name, Consumer<Wrapper> decorator, Supplier<Wrapper> addServlet) {

		// Determine if already registered
		ServletServicer servletServicer = this.registeredServlets.get(name);
		if (servletServicer != null) {
			return servletServicer;
		}

		// Add the servlet
		Wrapper wrapper = addServlet.get();
		Servlet servletInstance = wrapper.getServlet();
		String servletClassName = wrapper.getServletClass();

		// Decorate the servlet
		if (decorator != null) {
			decorator.accept(wrapper);
		}

		// Ensure not override name and servlet
		wrapper.setName(name);
		wrapper.setServlet(servletInstance);
		wrapper.setServletClass(servletClassName);

		// Setup the wrapper
		this.setupWrapperForDirectInvocation(wrapper);

		// Provide servicer
		ContainerAdapter adapter = new ContainerAdapter(wrapper, this.connector);
		servletServicer = (connection, executor, asynchronousFlow, asynchronousFlowCompletion, attributes) -> this
				.service(connection, executor, asynchronousFlow, asynchronousFlowCompletion, attributes, null,
						adapter::service);

		// Register and return servicer
		this.registeredServlets.put(name, servletServicer);
		return servletServicer;
	}

	@Override
	public FilterServicer addFilter(String name, Class<? extends Filter> filterClass, Consumer<FilterDef> decorator) {

		// Determine if already registered
		FilterServicer filterServicer = this.registeredFilters.get(name);
		if (filterServicer != null) {
			return filterServicer;
		}

		// Add the filter
		FilterDef filterDef = new FilterDef();
		if (decorator != null) {
			decorator.accept(filterDef);
		}
		filterDef.setFilterName(name);
		filterDef.setFilterClass(filterClass.getName());
		filterDef.setAsyncSupported("true");
		this.context.addFilterDef(filterDef);

		// Add the filter chain servlet
		Wrapper wrapper = Tomcat.addServlet(this.context, name, FilterChainHttpServlet.class.getName());

		// Configure filter on servlet
		FilterMap filterMap = new FilterMap();
		filterMap.setFilterName(name);
		filterMap.addServletName(name);
		this.context.addFilterMap(filterMap);

		// Setup the wrapper
		this.setupWrapperForDirectInvocation(wrapper);

		// Provide servicer
		ContainerAdapter adapter = new ContainerAdapter(wrapper, this.connector);
		filterServicer = (connection, executor, asynchronousFlow, asynchronousFlowCompletion, chain) -> this.service(
				connection, executor, asynchronousFlow, asynchronousFlowCompletion, null, chain, adapter::service);

		// Register and return servicer
		this.registeredFilters.put(name, filterServicer);
		return filterServicer;
	}

	@Override
	public <T> T getDependency(String qualifier, Class<? extends T> type) {
		return this.injectionRegistry.getDependency(qualifier, type, this.supplierSourceContext);
	}

	@Override
	public AvailableType[] getAvailableTypes() {

		// Ensure have available types
		if (this.availableTypes == null) {
			throw new IllegalStateException(AvailableType.class.getSimpleName() + " listing only available on "
					+ ServletSupplierSource.class.getSimpleName() + " completion");
		}

		// Return the available types
		return this.availableTypes;
	}

	@Override
	public void chainInServletManager() {

		// Determine if decision made
		if (this.isChainDecisionMade) {
			throw new IllegalStateException(
					ServletManager.class.getSimpleName() + " chain configuration already completed");
		}

		// Flag chain in the servlet manager
		this.isChainInServletManager = true;
	}

	@Override
	public OfficeExtensionContext getSourceContext() {
		return this.sourceContext;
	}

	/**
	 * Sets up the {@link Wrapper} for direct servicing.
	 * 
	 * @param wrapper {@link Wrapper}.
	 */
	private void setupWrapperForDirectInvocation(Wrapper wrapper) {

		// Always support async
		wrapper.setAsyncSupported(true);

		// Always load on startup (this ensure dependencies are loaded)
		if (wrapper.getLoadOnStartup() < 0) {
			wrapper.setLoadOnStartup(1);
		}
	}

	/**
	 * {@link HttpServlet} to handle {@link FilterChain}.
	 */
	public static class FilterChainHttpServlet extends HttpServlet {

		/**
		 * Serialise version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Attribute name for the {@link FilterChain}.
		 */
		public static final String ATTRIBUTE_NAME_FILTER_CHAIN = "#filter-chain#";

		/*
		 * ======================= HttpServlet ===========================
		 */

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			// As here, execute the filter chain
			FilterChain filterChain = (FilterChain) req.getAttribute(ATTRIBUTE_NAME_FILTER_CHAIN);
			filterChain.doFilter(req, resp);
		}
	}

	/**
	 * Servicer.
	 */
	@FunctionalInterface
	private static interface Servicer {

		/**
		 * Undertakes servicing.
		 * 
		 * @param request  {@link Request}.
		 * @param response {@link Response}.
		 * @throws Exception If fails servicing.
		 */
		void service(Request request, Response response) throws Exception;
	}

	/**
	 * Services the {@link ServerHttpConnection} via {@link Servicer}.
	 * 
	 * @param connection                 {@link ServerHttpConnection}.
	 * @param executor                   {@link Executor}.
	 * @param asynchronousFlow           {@link AsynchronousFlow}.
	 * @param asynchronousFlowCompletion {@link AsynchronousFlowCompletion}.
	 * @param attributes                 Attributes for the
	 *                                   {@link HttpServletRequest}. May be
	 *                                   <code>null</code>.
	 * @param filterChain                {@link FilterChain}. Will be ignored for
	 *                                   {@link Servlet}.
	 * @param servicer                   {@link Servicer}.
	 * @throws Exception If fails servicing.
	 */
	private void service(ServerHttpConnection connection, Executor executor, AsynchronousFlow asynchronousFlow,
			AsynchronousFlowCompletion asynchronousFlowCompletion, Map<String, ? extends Object> attributes,
			FilterChain filterChain, Servicer servicer) throws Exception {

		// Parse out the URL
		HttpRequest httpRequest = connection.getRequest();
		String requestUri = httpRequest.getUri();
		String[] parts = requestUri.split("\\?");
		requestUri = parts[0];
		String queryString;
		if (parts.length > 1) {
			String[] queryParts = new String[parts.length - 1];
			System.arraycopy(parts, 1, queryParts, 0, queryParts.length);
			queryString = String.join("?", queryParts);
		} else {
			queryString = "";
		}

		// Create the request
		Request request = new Request();
		request.scheme().setString(connection.isSecure() ? "https" : "http");
		request.method().setString(httpRequest.getMethod().getName());
		request.requestURI().setString(requestUri);
		request.decodedURI().setString(requestUri);
		request.queryString().setString(queryString);
		request.protocol().setString(httpRequest.getVersion().getName());
		MimeHeaders headers = request.getMimeHeaders();
		for (HttpHeader header : httpRequest.getHeaders()) {
			headers.addValue(header.getName()).setString(header.getValue());
		}
		if (attributes != null) {
			attributes.forEach((name, value) -> request.setAttribute(name, value));
		}
		request.setInputBuffer(new OfficeFloorInputBuffer(httpRequest));

		// Provide injection of context
		InjectContext injectContext = this.injectContextFactory.createInjectContext();
		injectContext.activate();
		request.setAttribute(InjectContext.REQUEST_ATTRIBUTE_NAME, injectContext);

		// Hook in potential filter chain
		if (filterChain != null) {
			request.setAttribute(FilterChainHttpServlet.ATTRIBUTE_NAME_FILTER_CHAIN, filterChain);
		}

		// Create the response
		Response response = new Response();
		HttpResponse httpResponse = connection.getResponse();
		response.setOutputBuffer(new OfficeFloorOutputBuffer(httpResponse));

		// Create processor for request
		new OfficeFloorProcessor(this.protocol, request, response, connection, executor, asynchronousFlow,
				asynchronousFlowCompletion);

		// Undertake servicing
		servicer.service(request, response);
	}

	/**
	 * {@link InputBuffer} for {@link ServerHttpConnection}.
	 */
	private static class OfficeFloorInputBuffer implements InputBuffer {

		/**
		 * {@link InputStream} to {@link HttpRequest} entity.
		 */
		private final InputStream entity;

		/**
		 * Instantiate.
		 * 
		 * @param httpRequest {@link HttpRequest}.
		 */
		private OfficeFloorInputBuffer(HttpRequest httpRequest) {
			this.entity = httpRequest.getEntity();
		}

		/*
		 * ================ InputBuffer =====================
		 */

		@Override
		public int available() {
			try {
				return this.entity.available();
			} catch (IOException ex) {
				return -1;
			}
		}

		@Override
		public int doRead(ApplicationBufferHandler handler) throws IOException {

			// Initiate the buffer
			ByteBuffer buffer = handler.getByteBuffer();
			BufferJvmFix.limit(buffer, buffer.capacity());

			// Write content to buffer
			int bytesRead = 0;
			int value;
			while ((value = this.entity.read()) != -1) {

				// Load the byte
				buffer.put(bytesRead, (byte) value);
				bytesRead++;

				// Determine if buffer full
				if (bytesRead == buffer.capacity()) {
					BufferJvmFix.limit(buffer, bytesRead);
					return bytesRead; // buffer full
				}
			}

			// Finished writing
			if (bytesRead == 0) {
				BufferJvmFix.limit(buffer, 0);
				return -1; // end of entity
			} else {
				// Provide last entity
				BufferJvmFix.limit(buffer, bytesRead);
				return bytesRead;
			}
		}
	}

	/**
	 * {@link OutputBuffer} for {@link ServerHttpConnection}.
	 */
	private static class OfficeFloorOutputBuffer implements OutputBuffer {

		/**
		 * {@link HttpResponse}.
		 */
		private final HttpResponse httpResponse;

		/**
		 * Bytes written.
		 */
		private long bytesWritten = 0;

		/**
		 * Instantiate.
		 * 
		 * @param httpResponse {@link HttpResponse}.
		 */
		private OfficeFloorOutputBuffer(HttpResponse httpResponse) {
			this.httpResponse = httpResponse;
		}

		/*
		 * ================= OutputBuffer ======================
		 */

		@Override
		public int doWrite(ByteBuffer chunk) throws IOException {
			int size = chunk.remaining();
			this.httpResponse.getEntity().write(chunk);
			this.bytesWritten += size;
			return size;
		}

		@Override
		public long getBytesWritten() {
			return this.bytesWritten;
		}
	}

}
