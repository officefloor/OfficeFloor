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
package net.officefloor.plugin.servlet.bridge;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.impl.spi.team.ProcessContextTeam;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.servlet.bridge.spi.ServletServiceBridger;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;

/**
 * {@link ManagedObjectSource} to trigger a {@link ProcessState} to service a
 * {@link HttpServletRequest} via providing a {@link ServletBridge}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletBridgeManagedObjectSource
		extends
		AbstractManagedObjectSource<None, ServletBridgeManagedObjectSource.FlowKeys> {

	/**
	 * Flow keys for the {@link ServletBridgeManagedObjectSource}.
	 */
	public static enum FlowKeys {
		SERVICE
	}

	/**
	 * Type names for the dependency annotations. Using the qualified name
	 * rather than the class name as some dependencies such as EJBs may not have
	 * the annotation class available within the class path.
	 */
	public static final String[] DEPENDENCY_ANNOTATION_TYPE_NAMES = new String[] {
			"javax.annotation.Resource", "javax.ejb.EJB" };

	/**
	 * Name of property identifying the instance of the {@link Servlet}.
	 */
	public static final String PROPERTY_INSTANCE_IDENTIFIER = "instance.identifier";

	/**
	 * Instance identifier for the next {@link ServletServiceBridger}.
	 */
	private static int nextInstanceIdentifier = 1;

	/**
	 * Name of property identifying whether to use {@link AsyncContext}
	 * servicing.
	 */
	public static final String PROPERTY_USE_ASYNC = "use.async";

	/**
	 * {@link ServletBridgeManagedObjectSource} by instance identifier.
	 */
	private static Map<String, ServletBridgeManagedObjectSource> instances = new HashMap<String, ServletBridgeManagedObjectSource>();

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<FlowKeys> context;

	/**
	 * Convenience method to create a {@link ServletServiceBridger} and also
	 * configure into the {@link AutoWireOfficeFloorSource}.
	 * 
	 * @param <S>
	 *            {@link Servlet} type.
	 * @param servletClass
	 *            {@link Servlet} class.
	 * @param source
	 *            {@link AutoWireOfficeFloorSource}.
	 * @param handlerSectionName
	 *            Name of the section to handle the HTTP request.
	 * @param handlerInputName
	 *            Name of the input on the section to handle the HTTP request.
	 * @return {@link ServletServiceBridger}.
	 */
	public static <S> ServletServiceBridger<S> createServletServiceBridger(
			Class<S> servletClass, AutoWireOfficeFloorSource source,
			final String handlerSectionName, final String handlerInputName) {

		// Create the Servlet Bridger
		ServletServiceBridger<S> bridger = ServletBridgeManagedObjectSource
				.createServletServiceBridger(servletClass);

		// Determine if handling asynchronously
		final boolean isUseAsync = bridger.isUseAsyncContext();

		// Configure the Servlet Bridger
		AutoWireObject bridge = source.addManagedObject(
				ServletBridgeManagedObjectSource.class.getName(),
				new ManagedObjectSourceWirer() {
					@Override
					public void wire(ManagedObjectSourceWirerContext context) {

						// Process scoped
						context.setManagedObjectScope(ManagedObjectScope.PROCESS);

						// Map flow to service
						context.mapFlow(FlowKeys.SERVICE.name(),
								handlerSectionName, handlerInputName);

						// Configure team for recycle task
						if (isUseAsync) {
							context.mapTeam("COMPLETE",
									PassiveTeamSource.class.getName());
						}
					}
				}, new AutoWire(ServletBridge.class));
		bridge.addProperty(
				ServletBridgeManagedObjectSource.PROPERTY_INSTANCE_IDENTIFIER,
				bridger.getInstanceIdentifier());
		bridge.addProperty(ServletBridgeManagedObjectSource.PROPERTY_USE_ASYNC,
				String.valueOf(isUseAsync));

		// Ensure use process context team if not asynchronous
		if (!isUseAsync) {
			source.assignDefaultTeam(ProcessContextTeamSource.class.getName());
		}

		// Return the Servlet Bridger
		return bridger;
	}

	/**
	 * Creates the {@link ServletServiceBridger} for the {@link Servlet}.
	 * 
	 * @param <S>
	 *            {@link Servlet} type.
	 * @param servletClass
	 *            Class of the {@link Servlet}.
	 * @return {@link ServletServiceBridger} for the {@link Servlet}.
	 */
	public synchronized static <S> ServletServiceBridger<S> createServletServiceBridger(
			Class<S> servletClass) {

		// Obtain the instance identifier (and increment for next)
		String instanceIdentifier = String.valueOf(nextInstanceIdentifier++);

		// Create the mapping of injected dependencies
		Map<Class<?>, Field> injectedDependencies = new HashMap<Class<?>, Field>();
		Class<?> clazz = servletClass;
		while (clazz != null) {

			// Interrogate fields for injected dependencies
			for (Field field : clazz.getDeclaredFields()) {

				// Determine if a dependency annotation
				boolean isDependency = false;
				IS_DEPENDENCY: for (Annotation annotation : field
						.getAnnotations()) {
					String annotationTypeName = annotation.annotationType()
							.getName();
					for (String dependencyAnnotationTypeName : DEPENDENCY_ANNOTATION_TYPE_NAMES) {
						if (dependencyAnnotationTypeName
								.equals(annotationTypeName)) {
							isDependency = true; // annotated as dependency
							break IS_DEPENDENCY;
						}
					}
				}

				// Register if dependency
				if (isDependency) {
					// Register the field as injected dependency
					Class<?> dependencyType = field.getType();
					injectedDependencies.put(dependencyType, field);
				}
			}

			// Interrogate parent
			clazz = clazz.getSuperclass();
		}

		// Create and return the servlet service bridger
		return new ServletServiceBridgerImpl<S>(instanceIdentifier,
				injectedDependencies);
	}

	/**
	 * Obtains the {@link ServletBridgeManagedObjectSource} instance.
	 * 
	 * @param instanceIdentifier
	 *            Instance identifier.
	 * @return {@link ServletBridgeManagedObjectSource} for the instance
	 *         identifier.
	 */
	private synchronized static ServletBridgeManagedObjectSource getInstance(
			String instanceIdentifier) {
		return instances.get(instanceIdentifier);
	}

	/*
	 * ========================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_INSTANCE_IDENTIFIER, "Instance");
		context.addProperty(PROPERTY_USE_ASYNC, "Async");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, FlowKeys> context)
			throws Exception {
		ManagedObjectSourceContext<FlowKeys> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the instance identifier
		String instanceIdentifier = mosContext
				.getProperty(PROPERTY_INSTANCE_IDENTIFIER);

		// Register this instance
		synchronized (ServletBridgeManagedObjectSource.class) {
			instances.put(instanceIdentifier, this);
		}

		// Specify the meta-data
		context.setObjectClass(ServletBridge.class);
		context.addFlow(FlowKeys.SERVICE, null);

		// Determine if using async
		boolean isUseAsync = Boolean.parseBoolean(mosContext.getProperty(
				PROPERTY_USE_ASYNC, String.valueOf(Boolean.FALSE)));
		if (isUseAsync) {
			// Register recycle task to complete the async context
			CompleteAsyncContextTask recycleTask = new CompleteAsyncContextTask();
			recycleTask.registerAsRecycleTask(mosContext, "COMPLETE");
		}
	}

	@Override
	public void start(ManagedObjectExecuteContext<FlowKeys> context)
			throws Exception {
		this.context = context;
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Should never be directly used by a task
		throw new IllegalStateException("Can not source managed object from a "
				+ this.getClass().getSimpleName());
	}

	/**
	 * {@link ServletServiceBridger} implementation.
	 * 
	 * @author Daniel Sagenschneider
	 */
	private static class ServletServiceBridgerImpl<S> implements
			ServletServiceBridger<S> {

		/**
		 * Instance identifier.
		 */
		private final String instanceIdentifier;

		/**
		 * Mapping of injected dependency type to its {@link Field} on the
		 * {@link Servlet}.
		 */
		private final Map<Class<?>, Field> injectedDependencies;

		/**
		 * Initiate.
		 * 
		 * @param instanceIdentifier
		 *            Instance identifier.
		 */
		public ServletServiceBridgerImpl(String instanceIdentifier,
				Map<Class<?>, Field> injectedDependencies) {
			this.instanceIdentifier = instanceIdentifier;
			this.injectedDependencies = injectedDependencies;

			// Ensure able to access fields of Servlet
			for (Field field : this.injectedDependencies.values()) {
				field.setAccessible(true);
			}
		}

		/**
		 * Obtains the object from the {@link Servlet}.
		 * 
		 * @param servlet
		 *            {@link Servlet}.
		 * @param objectType
		 *            Object type.
		 * @return Object or <code>null</code> if unkonwn object type.
		 * @throws Exception
		 *             If fails to obtain the object.
		 */
		private Object getObject(Object servlet, Class<?> objectType)
				throws Exception {

			// Look up the field
			Field field = this.injectedDependencies.get(objectType);
			if (field == null) {
				return null; // unknown dependency type
			}

			// Return injected dependency from the Servlet
			return field.get(servlet);
		}

		/*
		 * ==================== ServletServiceBridger ======================
		 */

		@Override
		public String getInstanceIdentifier() {
			return this.instanceIdentifier;
		}

		@Override
		public boolean isUseAsyncContext() {
			// May only use AsyncContext if no JEE container dependencies used
			return (this.injectedDependencies.size() == 0);
		}

		@Override
		public Class<?>[] getObjectTypes() {
			Set<Class<?>> types = this.injectedDependencies.keySet();
			return types.toArray(new Class[types.size()]);
		}

		@Override
		public void service(S servlet, HttpServletRequest request,
				HttpServletResponse response, ServletContext context)
				throws IOException, ServletException {

			// Obtain the instance
			ServletBridgeManagedObjectSource instance = ServletBridgeManagedObjectSource
					.getInstance(this.instanceIdentifier);
			if (instance == null) {
				throw new IllegalStateException("Instance "
						+ this.instanceIdentifier + " is not within an open "
						+ OfficeFloor.class.getSimpleName());
			}

			// Determine if service asynchronously
			if (this.isUseAsyncContext()) {
				// Service asynchronously so start async context
				AsyncContext asyncContext = request.startAsync();

				// Create the managed object to asynchronously service
				ServletBridgeManagedObject managedObject = new ServletBridgeManagedObject(
						this, servlet, request, response, context, asyncContext);

				// Trigger asynchronous servicing
				instance.context.invokeProcess(FlowKeys.SERVICE, null,
						managedObject, 0, managedObject);

			} else {
				// Create the managed object to synchronously service
				ServletBridgeManagedObject managedObject = new ServletBridgeManagedObject(
						this, servlet, request, response, context, null);

				try {
					// Service the request (blocking re-using this thread)
					ProcessContextTeam.doProcess(instance.context,
							FlowKeys.SERVICE, null, managedObject,
							managedObject);

				} catch (InterruptedException ex) {
					// Propagate failure
					throw new ServletException(ex);
				}

				// Propagate any exception
				managedObject.propagateException();
			}
		}
	}

	/**
	 * {@link ManagedObject} for the {@link ServletBridge}.
	 */
	private static class ServletBridgeManagedObject implements ServletBridge,
			ManagedObject, EscalationHandler {

		/**
		 * {@link ServletServiceBridgerImpl}.
		 */
		private final ServletServiceBridgerImpl<?> bridger;

		/**
		 * {@link Servlet}.
		 */
		private final Object servlet;

		/**
		 * {@link HttpServletRequest}.
		 */
		private final HttpServletRequest request;

		/**
		 * {@link HttpServletResponse}.
		 */
		private final HttpServletResponse response;

		/**
		 * {@link ServletContext}.
		 */
		private final ServletContext servletContext;

		/**
		 * {@link AsyncContext}.
		 */
		private final AsyncContext asyncContext;

		/**
		 * Escalation.
		 */
		private volatile Throwable escalation = null;

		/**
		 * Initiate.
		 * 
		 * @param bridger
		 *            {@link ServletServiceBridgerImpl}.
		 * @param servlet
		 *            {@link Servlet}.
		 * @param request
		 *            {@link HttpServletRequest}.
		 * @param response
		 *            {@link HttpServletResponse}.
		 * @param servletContext
		 *            {@link ServletContext}.
		 * @param asyncContext
		 *            {@link AsyncContext}.
		 */
		public ServletBridgeManagedObject(ServletServiceBridgerImpl<?> bridger,
				Object servlet, HttpServletRequest request,
				HttpServletResponse response, ServletContext servletContext,
				AsyncContext asyncContext) {
			this.bridger = bridger;
			this.servlet = servlet;
			this.request = request;
			this.response = response;
			this.servletContext = servletContext;
			this.asyncContext = asyncContext;
		}

		/**
		 * Propagates the {@link Exception}.
		 * 
		 * @throws IOException
		 *             As per {@link Servlet} API.
		 * @throws ServletException
		 *             As per {@link Servlet} API.
		 */
		public void propagateException() throws IOException, ServletException {

			// Obtain possible exception
			Throwable exception = this.escalation;

			// Do nothing if no escalation
			if (exception == null) {
				return;
			}

			// Propagate exception
			if (exception instanceof IOException) {
				throw (IOException) exception; // propagate as is
			} else if (exception instanceof ServletException) {
				throw (ServletException) exception; // propagate as is
			} else {
				// Wrap as Servlet exception
				throw new ServletException(exception);
			}
		}

		/*
		 * ========================== ServletBridge =========================
		 */

		@Override
		public HttpServletRequest getRequest() {
			return this.request;
		}

		@Override
		public HttpServletResponse getResponse() {
			return this.response;
		}

		@Override
		public ServletContext getServletContext() {
			return this.servletContext;
		}

		@Override
		public AsyncContext getAsyncContext() {
			return this.asyncContext;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <O> O getObject(Class<? extends O> objectType) throws Exception {
			return (O) this.bridger.getObject(this.servlet, objectType);
		}

		/*
		 * ===================== ManagedObject ============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ==================== EscalationHandler =========================
		 */

		@Override
		public void handleEscalation(Throwable escalation) throws Throwable {
			this.escalation = escalation;

			// Handle failure if asynchronous
			if (this.asyncContext != null) {
				try {
					// Flag to send error
					this.response.sendError(
							HttpStatus.SC_INTERNAL_SERVER_ERROR,
							escalation.getMessage() + "["
									+ escalation.getClass().getSimpleName()
									+ "]");

				} finally {
					// Ensure complete async context
					this.asyncContext.complete();
				}
			}
		}
	}

	/**
	 * {@link Task} to complete the {@link AsyncContext}.
	 */
	public static class CompleteAsyncContextTask extends
			AbstractSingleTask<CompleteAsyncContextTask, None, None> {

		@Override
		public Object doTask(
				TaskContext<CompleteAsyncContextTask, None, None> context) {

			// Obtain the recycle parameter
			RecycleManagedObjectParameter<ServletBridgeManagedObject> parameter = this
					.getRecycleManagedObjectParameter(context,
							ServletBridgeManagedObject.class);

			// Complete the async context
			parameter.getManagedObject().getAsyncContext().complete();

			// No further functionality
			return null;
		}
	}

}