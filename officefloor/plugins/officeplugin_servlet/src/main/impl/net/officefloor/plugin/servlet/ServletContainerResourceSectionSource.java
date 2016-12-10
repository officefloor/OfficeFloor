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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.servlet.bridge.ServletBridge;

/**
 * {@link SectionSource} for servicing by a {@link Servlet} container resource.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContainerResourceSectionSource extends
		AbstractSectionSource {

	/**
	 * {@link HttpServletRequest} attribute name to indicate a {@link Servlet}
	 * container resource path.
	 */
	private static final String ATTRIBUTE_SERVLET_RESOURCE_PATH = "officefloor.servlet.resource.path";

	/**
	 * Dependency keys for the {@link ServletContainerResourceWorkSource}.
	 */
	public static enum DependencyKeys {
		SERVLET_BRIDGE
	}

	/**
	 * Completes the servicing after {@link OfficeFloor} functionality for the
	 * {@link OfficeFloorServlet}.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}.
	 * @param response
	 *            {@link HttpServletResponse}.
	 * @return <code>true</code> if serviced.
	 * @throws ServletException
	 *             As per {@link Servlet} API.
	 * @throws IOException
	 *             As per {@link Servlet} API.
	 */
	public static boolean completeServletService(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// Obtain the request attribute
		String path;
		synchronized (request) {
			path = (String) request
					.getAttribute(ATTRIBUTE_SERVLET_RESOURCE_PATH);
		}

		// Determine if requires handling
		if (path == null) {
			return true; // serviced
		}

		// Override the request path
		final String servletPath = (path.startsWith("/") ? path : "/" + path);
		HttpServletRequestWrapper requestWithPath = new HttpServletRequestWrapper(
				request) {
			@Override
			public String getServletPath() {
				return servletPath;
			}
		};

		// Dispatch for JSP
		if (path.toLowerCase().endsWith(".jsp")) {
			RequestDispatcher dispatcher = request.getRequestDispatcher(path);
			if (dispatcher != null) {
				// Allow JSP Servlet to service
				dispatcher.forward(requestWithPath, response);
				return true;
			}
		}

		// Dispatch request to Servlet container resource
		RequestDispatcher dispatcher = request.getServletContext()
				.getNamedDispatcher("default");
		if (dispatcher != null) {
			// Allow Default Servlet to service with static resource
			dispatcher.forward(requestWithPath, response);
			return true;
		}

		// Not serviced
		return false;
	}

	/*
	 * ==================== SectionSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required specification
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Create the Servlet Bridge dependency
		SectionObject servletBridge = designer.addSectionObject(
				"SERVLET_BRIDGE", ServletBridge.class.getName());

		// Create the non-handled task
		String nonHandledInputName = context.getSectionLocation();
		this.addServletResource(nonHandledInputName, null, servletBridge,
				designer);

		// Create the Servlet container resource tasks
		Set<String> registeredResources = new HashSet<String>();
		for (String inputName : context.getPropertyNames()) {

			// Obtain the request dispatcher path
			String requestDispatcherPath = context.getProperty(inputName);

			// Ensure only register the resource once
			if (registeredResources.contains(requestDispatcherPath)) {
				continue;
			}

			// Add Servlet Resource
			this.addServletResource(inputName, requestDispatcherPath,
					servletBridge, designer);

			// Resource registered
			registeredResources.add(requestDispatcherPath);
		}
	}

	/**
	 * Adds handling for a {@link Servlet} resource.
	 * 
	 * @param inputName
	 *            {@link SectionInput} name.
	 * @param requestDispatcherPath
	 *            {@link RequestDispatcher} path.
	 * @param servletBridge
	 *            {@link ServletBridge} {@link SectionObject}.
	 * @param designer
	 *            {@link SectionDesigner}.
	 */
	private void addServletResource(String inputName,
			String requestDispatcherPath, SectionObject servletBridge,
			SectionDesigner designer) {

		// Create the task to indicate Servlet container resource
		SectionWork work = designer.addSectionWork(inputName,
				ServletContainerResourceWorkSource.class.getName());
		work.addProperty(
				ServletContainerResourceWorkSource.PROPERTY_SERVLET_CONTAINER_RESOURCE,
				requestDispatcherPath);
		SectionTask task = work.addSectionTask(inputName, "RESOURCE");
		TaskObject dependency = task
				.getTaskObject(DependencyKeys.SERVLET_BRIDGE.name());
		designer.link(dependency, servletBridge);

		// Link input for task
		SectionInput input = designer.addSectionInput(inputName, null);
		designer.link(input, task);
	}

	/**
	 * {@link Task} to link to {@link Servlet} container resource.
	 */
	public static class ServletContainerResourceTask
			extends
			AbstractSingleTask<ServletContainerResourceTask, DependencyKeys, None> {

		/**
		 * {@link RequestDispatcher} path.
		 */
		public final String requestDispatcherPath;

		/**
		 * Initiate.
		 * 
		 * @param requestDispatcherPath
		 *            {@link RequestDispatcher} path.
		 */
		public ServletContainerResourceTask(String requestDispatcherPath) {
			this.requestDispatcherPath = requestDispatcherPath;
		}

		/*
		 * ===================== Task ============================
		 */

		@Override
		public Object doTask(
				TaskContext<ServletContainerResourceTask, DependencyKeys, None> context) {

			// Obtain the Servlet bridge
			ServletBridge bridge = (ServletBridge) context
					.getObject(DependencyKeys.SERVLET_BRIDGE);

			// Determine the path
			HttpServletRequest request = bridge.getRequest();
			String path = (this.requestDispatcherPath != null ? this.requestDispatcherPath
					: request.getServletPath());

			// Indicate to use Servlet container resource
			synchronized (request) {
				request.setAttribute(ATTRIBUTE_SERVLET_RESOURCE_PATH, path);
			}

			// Nothing further
			return null;
		}
	}

	/**
	 * {@link WorkSource} to link to {@link Servlet} container resource.
	 */
	public static class ServletContainerResourceWorkSource extends
			AbstractWorkSource<ServletContainerResourceTask> {

		/**
		 * Name of the property for the {@link Servlet} container resource.
		 */
		public static final String PROPERTY_SERVLET_CONTAINER_RESOURCE = "servlet.container.resource";

		/*
		 * ====================== WorkSource ===========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_SERVLET_CONTAINER_RESOURCE);
		}

		@Override
		public void sourceWork(
				WorkTypeBuilder<ServletContainerResourceTask> workTypeBuilder,
				WorkSourceContext context) throws Exception {

			// Obtain the request dispatcher path
			String requestDispatcherPath = context.getProperty(
					PROPERTY_SERVLET_CONTAINER_RESOURCE, null);

			// Add the task
			ServletContainerResourceTask factory = new ServletContainerResourceTask(
					requestDispatcherPath);
			workTypeBuilder.setWorkFactory(factory);
			TaskTypeBuilder<DependencyKeys, None> task = workTypeBuilder
					.addTaskType("RESOURCE", factory, DependencyKeys.class,
							None.class);
			task.addObject(ServletBridge.class).setKey(
					DependencyKeys.SERVLET_BRIDGE);
		}
	}

}