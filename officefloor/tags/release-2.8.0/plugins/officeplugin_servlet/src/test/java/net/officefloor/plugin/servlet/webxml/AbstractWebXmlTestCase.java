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
package net.officefloor.plugin.servlet.webxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.impl.repository.xml.XmlConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.plugin.servlet.container.source.HttpServletWorkSource;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource;
import net.officefloor.plugin.servlet.filter.configuration.FilterInstance;
import net.officefloor.plugin.servlet.filter.configuration.FilterMappings;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.route.source.ServletRouteWorkSource;
import net.officefloor.plugin.servlet.webxml.model.WebAppModel;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Abstract functionality for <code>web.xml</code> testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWebXmlTestCase extends OfficeFrameTestCase {

	/**
	 * Created {@link HttpClient} instances.
	 */
	private final List<HttpClient> clients = new LinkedList<HttpClient>();

	/**
	 * {@link OfficeFloor} for the Servlet application.
	 */
	private OfficeFloor officeFloor = null;

	/**
	 * Port that {@link Servlet} application will be listening on for requests.
	 */
	private int port;

	@Override
	protected void tearDown() throws Exception {

		// Stop the HTTP clients
		for (HttpClient client : this.clients) {
			client.getConnectionManager().shutdown();
		}

		// Stop servlet application (if started)
		if (this.officeFloor != null) {
			this.stopServletApplication();
		}
	}

	/**
	 * Starts the {@link Servlet} application for the input <code>web.xml</code>
	 * file.
	 * 
	 * @param webXmlFileName
	 *            Name of the <code>web.xml</code> file to configure the
	 *            {@link Servlet} application.
	 */
	protected void startServletApplication(String webXmlFileName) {
		try {
			// Obtain the port for the application
			this.port = MockHttpServer.getAvailablePort();

			// Obtain the location of password file and resource root
			File passwordFile = this.findFile(this.getClass(), "password.txt");

			// Create the configuration context
			XmlConfigurationContext xmlContext = new XmlConfigurationContext(
					this, "OfficeFloor.xml");
			xmlContext.addTag("port", String.valueOf(this.port));
			xmlContext.addTag("web.xml.file.name", webXmlFileName);
			xmlContext.addTag("password.file.location",
					passwordFile.getAbsolutePath());
			ResourceSource resourceSource = new MockResourceSource(xmlContext,
					new ClassLoaderConfigurationContext(this.getClass()
							.getClassLoader()));

			// Create and configure the compiler
			OfficeFloorCompiler compiler = OfficeFloorCompiler
					.newOfficeFloorCompiler(null);
			compiler.addResources(resourceSource);
			compiler.setCompilerIssues(new FailTestCompilerIssues());

			// Compiler the Office Floor
			this.officeFloor = compiler.compile("office-floor");

			// Open (start) the Office Floor for the Servlet application
			this.officeFloor.openOfficeFloor();

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Creates a {@link HttpClient}.
	 * 
	 * @return {@link HttpClient}.
	 */
	protected HttpClient createHttpClient() {
		HttpClient client = new DefaultHttpClient();
		this.clients.add(client);
		return client;
	}

	/**
	 * Obtains the URL of the server ({@link Servlet} application).
	 * 
	 * @return URL of the server.
	 */
	protected String getServerUrl() {
		return "http://localhost:" + this.port;
	}

	/**
	 * Stops the {@link Servlet} application.
	 */
	protected void stopServletApplication() {
		this.officeFloor.closeOfficeFloor();
		this.officeFloor = null;
	}

	/**
	 * Ensure correct design.
	 */
	protected void doDesignTest(String webXmlFileName, DesignRecorder recorder) {
		try {

			// Mocks
			final SectionSourceContext context = this
					.createMock(SectionSourceContext.class);
			final SectionDesigner designer = this
					.createMock(SectionDesigner.class);

			// Obtain the web.xml configuration
			File webXmlConfiguration = this.findFile(this.getClass(),
					webXmlFileName);

			// Record obtaining the web.xml configuration
			this.recordReturn(context, context.getSectionLocation(),
					webXmlFileName);
			this.recordReturn(context, context.getResource(webXmlFileName),
					new FileInputStream(webXmlConfiguration));

			// Record obtaining the meta-data for unmarshalling the web.xml
			String unmarshallerLocation = WebAppModel.class.getPackage()
					.getName().replace('.', '/')
					+ "/UnmarshalWebXml.xml";
			this.recordReturn(
					context,
					context.getResource(unmarshallerLocation),
					this.getClass().getClassLoader()
							.getResourceAsStream(unmarshallerLocation));

			// Do further recording
			if (recorder != null) {
				recorder.designer = designer;
				recorder.record(designer);
			}

			// Test
			this.replayMockObjects();
			new WebXmlSectionSource().sourceSection(designer, context);
			this.verifyMockObjects();

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Records design of section.
	 */
	protected abstract class DesignRecorder {

		/**
		 * Service {@link SectionInput}.
		 */
		protected final SectionInput SERVICE_INPUT = createMock(SectionInput.class);

		/**
		 * Unhandled {@link SectionOutput}.
		 */
		protected final SectionOutput UNHANDLED_OUTPUT = createMock(SectionOutput.class);

		/**
		 * {@link ServletException} {@link SectionOutput}.
		 */
		protected final SectionOutput SERVLET_EXCEPTION_OUTPUT = createMock(SectionOutput.class);

		/**
		 * {@link IOException} {@link SectionOutput}.
		 */
		protected final SectionOutput IO_EXCEPTION_OUTPUT = createMock(SectionOutput.class);

		/**
		 * {@link ServletServer} {@link SectionObject}.
		 */
		protected final SectionObject SERVLET_SERVER_OBJECT = createMock(SectionObject.class);

		/**
		 * {@link ServerHttpConnection} {@link SectionObject}.
		 */
		protected final SectionObject HTTP_CONNECTION_OBJECT = createMock(SectionObject.class);

		/**
		 * Request attributes {@link SectionObject}.
		 */
		protected final SectionObject REQUEST_ATTRIBUTES_OBJECT = createMock(SectionObject.class);

		/**
		 * {@link HttpSession} {@link SectionObject}.
		 */
		protected final SectionObject HTTP_SESSION_OBJECT = createMock(SectionObject.class);

		/**
		 * {@link HttpSecurity} {@link SectionObject}.
		 */
		protected final SectionObject HTTP_SECURITY_OBJECT = createMock(SectionObject.class);

		/**
		 * {@link OfficeServletContext} {@link SectionManagedObject}.
		 */
		protected final SectionManagedObject OFFICE_SERVLET_CONTEXT_MO = createMock(SectionManagedObject.class);

		/**
		 * {@link FilterInstance} instances.
		 */
		protected final List<FilterInstance> filters = new LinkedList<FilterInstance>();

		/**
		 * {@link FilterMappings}.
		 */
		protected final FilterMappings filterMappings = new FilterMappings();

		/**
		 * {@link MimeMapping} instances.
		 */
		protected final List<MimeMapping> mimeMappings = new LinkedList<MimeMapping>();

		/**
		 * {@link SectionDesigner}.
		 */
		private SectionDesigner designer;

		/**
		 * Records design of the {@link Servlet} application section.
		 * 
		 * @param designer
		 *            Mock {@link SectionDesigner} to record design.
		 */
		abstract void record(SectionDesigner designer);

		/**
		 * Records initialising.
		 */
		protected void recordInit() {

			// Record the input
			recordReturn(this.designer,
					this.designer.addSectionInput("service", null),
					SERVICE_INPUT);

			// Record the outputs
			recordReturn(this.designer,
					this.designer.addSectionOutput("unhandled", null, false),
					UNHANDLED_OUTPUT);
			recordReturn(this.designer, this.designer.addSectionOutput(
					ServletException.class.getSimpleName(),
					ServletException.class.getName(), true),
					SERVLET_EXCEPTION_OUTPUT);
			recordReturn(this.designer, this.designer.addSectionOutput(
					IOException.class.getSimpleName(),
					IOException.class.getName(), true), IO_EXCEPTION_OUTPUT);

			// Record the objects
			recordReturn(this.designer, this.designer.addSectionObject(
					"SERVLET_SERVER", ServletServer.class.getName()),
					SERVLET_SERVER_OBJECT);
			recordReturn(this.designer, this.designer.addSectionObject(
					"HTTP_CONNECTION", ServerHttpConnection.class.getName()),
					HTTP_CONNECTION_OBJECT);
			recordReturn(this.designer, this.designer.addSectionObject(
					"REQUEST_ATTRIBUTES", HttpRequestState.class.getName()),
					REQUEST_ATTRIBUTES_OBJECT);
			recordReturn(this.designer, this.designer.addSectionObject(
					"HTTP_SESSION", HttpSession.class.getName()),
					HTTP_SESSION_OBJECT);
			recordReturn(this.designer, this.designer.addSectionObject(
					"HTTP_SECURITY", HttpSecurity.class.getName()),
					HTTP_SECURITY_OBJECT);
		}

		/**
		 * Adds a {@link FilterInstance} for recording.
		 * 
		 * @param filter
		 *            {@link FilterInstance}.
		 */
		protected void recordFilter(FilterInstance filter) {
			this.filters.add(filter);
		}

		/**
		 * Obtains the {@link FilterMappings}.
		 * 
		 * @return {@link FilterMappings}.
		 */
		protected FilterMappings getFilterMappings() {
			return this.filterMappings;
		}

		/**
		 * Records a MIME mapping.
		 * 
		 * @param extension
		 *            Extension.
		 * @param mimeType
		 *            MIME type.
		 */
		protected void recordMimeMapping(String extension, String mimeType) {
			this.mimeMappings.add(new MimeMapping(extension, mimeType));
		}

		/**
		 * Records creating the {@link OfficeServletContext}.
		 * 
		 * @param contextParamNameValuePairs
		 *            Expected context parameters.
		 */
		protected void recordOfficeServletContext(
				String... contextParamNameValuePairs) {

			// Record loading the office servlet context managed object source
			final SectionManagedObjectSource source = createMock(SectionManagedObjectSource.class);
			recordReturn(this.designer,
					this.designer.addSectionManagedObjectSource(
							"OfficeServletContext",
							OfficeServletContextManagedObjectSource.class
									.getName()), source);
			source.addProperty(
					OfficeServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME,
					"OfficeFloor");

			// Record the context parameters
			for (int i = 0; i < contextParamNameValuePairs.length; i += 2) {
				String name = contextParamNameValuePairs[i];
				String value = contextParamNameValuePairs[i + 1];
				source.addProperty(
						OfficeServletContextManagedObjectSource.PROPERTY_PREFIX_INIT_PARAMETER
								+ name, value);
			}

			// Record loading the office servlet context managed object
			recordReturn(source, source.addSectionManagedObject(
					"OfficeServletContext", ManagedObjectScope.PROCESS),
					OFFICE_SERVLET_CONTEXT_MO);

			// Record the MIME mappings
			for (MimeMapping mapping : this.mimeMappings) {
				source.addProperty(
						OfficeServletContextManagedObjectSource.PROPERTY_PREFIX_FILE_EXTENSION_TO_MIME_TYPE
								+ mapping.extension, mapping.mimeType);
			}

			// Record linking servlet server dependency
			final ManagedObjectDependency servletServerDependency = createMock(ManagedObjectDependency.class);
			recordReturn(OFFICE_SERVLET_CONTEXT_MO,
					OFFICE_SERVLET_CONTEXT_MO
							.getManagedObjectDependency("SERVLET_SERVER"),
					servletServerDependency);
			this.designer.link(servletServerDependency, SERVLET_SERVER_OBJECT);

			// Create properties for the filters
			PropertyList filterProperties = OfficeFloorCompiler
					.newPropertyList();
			for (FilterInstance filter : this.filters) {
				filter.outputProperties(filterProperties);
			}
			this.filterMappings.outputProperties(filterProperties);

			// Record the filter properties
			for (Property filterProperty : filterProperties) {
				source.addProperty(filterProperty.getName(),
						filterProperty.getValue());
			}
		}

		/**
		 * Records the {@link ServletRouteWorkSource}.
		 */
		protected void recordRouteService() {

			// Record the route work and its task
			final SectionWork work = createMock(SectionWork.class);
			recordReturn(this.designer, this.designer.addSectionWork("Route",
					ServletRouteWorkSource.class.getName()), work);
			final SectionTask task = createMock(SectionTask.class);
			recordReturn(work, work.addSectionTask("route",
					ServletRouteWorkSource.TASK_ROUTE), task);
			this.designer.link(SERVICE_INPUT, task);

			// Record linking unhandled flow
			final TaskFlow unhandledFlow = createMock(TaskFlow.class);
			recordReturn(task, task.getTaskFlow("UNHANDLED"), unhandledFlow);
			this.designer.link(unhandledFlow, UNHANDLED_OUTPUT,
					FlowInstigationStrategyEnum.SEQUENTIAL);

			// Record linking HTTP connection
			final TaskObject httpConnection = createMock(TaskObject.class);
			recordReturn(task, task.getTaskObject("HTTP_CONNECTION"),
					httpConnection);
			this.designer.link(httpConnection, HTTP_CONNECTION_OBJECT);

			// Record linking Office Servlet Context
			final TaskObject officeServletContext = createMock(TaskObject.class);
			recordReturn(task, task.getTaskObject("OFFICE_SERVLET_CONTEXT"),
					officeServletContext);
			this.designer.link(officeServletContext, OFFICE_SERVLET_CONTEXT_MO);
		}

		/**
		 * Records creating a {@link HttpServlet}.
		 * 
		 * @param servletName
		 *            {@link Servlet} name.
		 * @param servletMapping
		 *            {@link Servlet} mapping.
		 * @param initParamNameValues
		 *            Init param name value pairs.
		 * @return Constructed {@link SectionTask} for the {@link HttpServlet}.
		 */
		protected SectionTask recordHttpServlet(String servletName,
				String servletMapping, String... initParamNameValues) {

			// Record creating the work with parameters
			final SectionWork work = createMock(SectionWork.class);
			recordReturn(this.designer, this.designer.addSectionWork(
					servletName, HttpServletWorkSource.class.getName()), work);
			work.addProperty(HttpServletWorkSource.PROPERTY_SERVLET_NAME,
					servletName);
			work.addProperty(
					HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
					MockHttpServlet.class.getName());
			work.addProperty(HttpServletWorkSource.PROPERTY_SERVLET_MAPPINGS,
					servletMapping);
			for (int i = 0; i < initParamNameValues.length; i += 2) {
				String name = initParamNameValues[i];
				String value = initParamNameValues[i + 1];
				work.addProperty(
						HttpServletWorkSource.PROPERTY_PREFIX_INIT_PARAMETER
								+ name, value);
			}

			// Record creating the task
			final SectionTask task = createMock(SectionTask.class);
			recordReturn(
					work,
					work.addSectionTask("service-by-" + servletName, "service"),
					task);

			// Record linking the servicer mapping
			final TaskObject servicerMapping = createMock(TaskObject.class);
			recordReturn(task, task.getTaskObject("SERVICER_MAPPING"),
					servicerMapping);
			servicerMapping.flagAsParameter();

			// Record linking the office servlet context
			final TaskObject officeServletContext = createMock(TaskObject.class);
			recordReturn(task, task.getTaskObject("OFFICE_SERVLET_CONTEXT"),
					officeServletContext);
			this.designer.link(officeServletContext, OFFICE_SERVLET_CONTEXT_MO);

			// Record linking the HTTP connection
			final TaskObject httpConnection = createMock(TaskObject.class);
			recordReturn(task, task.getTaskObject("HTTP_CONNECTION"),
					httpConnection);
			this.designer.link(httpConnection, HTTP_CONNECTION_OBJECT);

			// Record linking the request attributes
			final TaskObject requestAttributes = createMock(TaskObject.class);
			recordReturn(task, task.getTaskObject("REQUEST_ATTRIBUTES"),
					requestAttributes);
			this.designer.link(requestAttributes, REQUEST_ATTRIBUTES_OBJECT);

			// Record linking the HTTP session
			final TaskObject httpSession = createMock(TaskObject.class);
			recordReturn(task, task.getTaskObject("HTTP_SESSION"), httpSession);
			this.designer.link(httpSession, HTTP_SESSION_OBJECT);

			// Record linking the HTTP security
			final TaskObject httpSecurity = createMock(TaskObject.class);
			recordReturn(task, task.getTaskObject("HTTP_SECURITY"),
					httpSecurity);
			this.designer.link(httpSecurity, HTTP_SECURITY_OBJECT);

			// Record linking the servlet exception escalation
			final TaskFlow servletException = createMock(TaskFlow.class);
			recordReturn(task,
					task.getTaskEscalation(ServletException.class.getName()),
					servletException);
			this.designer.link(servletException, SERVLET_EXCEPTION_OUTPUT,
					FlowInstigationStrategyEnum.SEQUENTIAL);

			// Record linking the io exception escalation
			final TaskFlow ioException = createMock(TaskFlow.class);
			recordReturn(task,
					task.getTaskEscalation(IOException.class.getName()),
					ioException);
			this.designer.link(ioException, IO_EXCEPTION_OUTPUT,
					FlowInstigationStrategyEnum.SEQUENTIAL);

			// Return the task
			return task;
		}
	}

	/**
	 * Mock {@link ConfigurationContext} for testing.
	 */
	private class MockResourceSource implements ResourceSource {

		/**
		 * Delegate {@link ConfigurationContext} instances.
		 */
		private final ConfigurationContext[] delegates;

		/**
		 * Initiate.
		 * 
		 * @param delegates
		 *            Delegate {@link ConfigurationContext} instances.
		 */
		public MockResourceSource(ConfigurationContext... delegates) {
			this.delegates = delegates;
		}

		/*
		 * ======================= ResourceSource =======================
		 */

		@Override
		public InputStream sourceResource(String location) {
			try {
				// Return first available item from a delegate
				for (ConfigurationContext delegate : this.delegates) {
					ConfigurationItem item = delegate
							.getConfigurationItem(location);
					if (item != null) {
						return item.getConfiguration();
					}
				}

				// As here no item available from any delegate
				return null;

			} catch (Exception ex) {
				return null;
			}
		}
	}

	/**
	 * MIME mapping.
	 */
	private static class MimeMapping {

		/**
		 * Extension.
		 */
		public final String extension;

		/**
		 * MIME type.
		 */
		public final String mimeType;

		/**
		 * Initiate.
		 * 
		 * @param extension
		 *            Extension.
		 * @param mimeType
		 *            MIME type.
		 */
		public MimeMapping(String extension, String mimeType) {
			this.extension = extension;
			this.mimeType = mimeType;
		}
	}

}