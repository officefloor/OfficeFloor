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

package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.PrivateSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersObjectManagedObjectSource;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersObjectManagedObjectSource.Dependencies;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.clazz.source.HttpSessionClassManagedObjectSource;
import net.officefloor.plugin.web.http.template.HttpParameters;
import net.officefloor.plugin.web.http.template.HttpSessionStateful;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;

/**
 * {@link SectionSource} for the HTTP template.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionSource extends AbstractSectionSource {

	/**
	 * Property name for the {@link Class} providing the backing logic to the
	 * template.
	 */
	public static final String PROPERTY_CLASS_NAME = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME;

	/**
	 * Name of the {@link SectionInput} for rendering this {@link HttpTemplate}.
	 */
	public static final String RENDER_TEMPLATE_INPUT_NAME = "renderTemplate";

	/**
	 * Name of the {@link SectionOutput} for flow after completion of rending
	 * the {@link HttpTemplate}.
	 */
	public static final String ON_COMPLETION_OUTPUT_NAME = "output";

	/**
	 * Prefix on the {@link Task} name of the class {@link Method} handling the
	 * link.
	 */
	private static final String LINK_METHOD_TASK_NAME_PREFIX = "ServiceLink_";

	/**
	 * {@link TemplateBeanTask} instances by the template bean
	 * {@link SectionTask} name.
	 */
	private final Map<String, TemplateBeanTask> templateBeanTasksByName = new HashMap<String, TemplateBeanTask>();

	/**
	 * {@link Boolean} by template {@link SectionTask} name indicating if
	 * requires bean.
	 */
	private final Map<String, Boolean> isRequireBeanTemplates = new HashMap<String, Boolean>();

	/**
	 * {@link Task} link names.
	 */
	private final Set<String> taskLinkNames = new HashSet<String>();

	/**
	 * Indicates if the {@link SectionTask} by the name is to provide a template
	 * bean.
	 * 
	 * @param taskName
	 *            Name of the {@link SectionTask}.
	 * @return <code>true</code> if is a template bean {@link SectionTask}.
	 */
	private boolean isTemplateBeanTask(String taskName) {

		// Determine if template bean method
		if (taskName.startsWith("get")) {
			String templateName = taskName.substring("get".length());
			Boolean isRequireBean = this.isRequireBeanTemplates
					.get(templateName.toUpperCase());
			if (isRequireBean != null) {
				// Is template bean method if require bean
				return isRequireBean.booleanValue();
			}
		}

		// As here, not a template bean task
		return false;
	}

	/*
	 * ===================== SectionSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLASS_NAME, "Class");
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Obtain the template location
		String templateLocation = context.getSectionLocation();

		// Obtain the class name
		String sectionClassName = context.getProperty(PROPERTY_CLASS_NAME);

		// Create the input to the section
		SectionInput sectionInput = designer.addSectionInput(
				RENDER_TEMPLATE_INPUT_NAME, null);

		// Obtain the HTTP template
		PropertyList templateProperties = context.createPropertyList();
		templateProperties.addProperty(
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE).setValue(
				templateLocation);
		HttpTemplate template = HttpTemplateWorkSource.getHttpTemplate(
				templateProperties.getProperties(), context.getClassLoader());

		// Obtain the listing of task link names
		String[] linkNames = HttpTemplateWorkSource
				.getHttpTemplateLinkNames(template);
		this.taskLinkNames.addAll(Arrays.asList(linkNames));

		// Register the HTTP template sections requiring a bean
		for (HttpTemplateSection templateSection : template.getSections()) {
			String templateSectionName = templateSection.getSectionName();
			boolean isRequireBean = HttpTemplateWorkSource
					.isHttpTemplateSectionRequireBean(templateSection);
			this.isRequireBeanTemplates.put(templateSectionName.toUpperCase(),
					new Boolean(isRequireBean));
		}

		// Name of work is exposed on URL for links.
		// Result is: /<section>.links/<link>.task
		final String TEMPLATE_WORK_NANE = "links";

		// Load the HTTP template
		SectionWork templateWork = designer.addSectionWork(TEMPLATE_WORK_NANE,
				HttpTemplateWorkSource.class.getName());
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE,
				templateLocation);

		// Create the template tasks and ensure registered for logic flows
		HttpTemplateClassSectionSource classSource = new HttpTemplateClassSectionSource();
		Map<String, SectionTask> templateTasks = new HashMap<String, SectionTask>();
		for (HttpTemplateSection templateSection : template.getSections()) {

			// Obtain the template task name
			String templateTaskName = templateSection.getSectionName();

			// Add the template task
			SectionTask templateTask = templateWork.addSectionTask(
					templateTaskName, templateTaskName);

			// Register the template task
			templateTasks.put(templateTaskName, templateTask);

			// Make template task available to logic flow
			classSource.registerTaskByTypeName(templateTaskName, templateTask);
		}

		// Load the section class (with ability to link in template tasks)
		classSource.sourceSection(designer, context);

		// Keep track of template bean task keys
		Set<String> templateBeanTaskKeys = new HashSet<String>();

		// Load the HTTP template tasks
		SectionTask firstTemplateTask = null;
		SectionTask previousTemplateTask = null;
		for (HttpTemplateSection templateSection : template.getSections()) {

			// Obtain the template task
			String templateTaskName = templateSection.getSectionName();
			SectionTask templateTask = templateTasks.get(templateTaskName);

			// Determine if template section requires a bean
			boolean isRequireBean = HttpTemplateWorkSource
					.isHttpTemplateSectionRequireBean(templateSection);

			// Link the Server HTTP Connection dependency
			SectionObject connectionObject = classSource
					.getOrCreateObject(ServerHttpConnection.class.getName());
			designer.link(templateTask.getTaskObject("SERVER_HTTP_CONNECTION"),
					connectionObject);

			// Flag bean as parameter if requires a bean
			if (isRequireBean) {
				templateTask.getTaskObject("OBJECT").flagAsParameter();
			}

			// Link the I/O escalation
			SectionOutput ioEscalation = classSource.getOrCreateOutput(
					IOException.class.getName(), IOException.class.getName(),
					true);
			designer.link(
					templateTask.getTaskEscalation(IOException.class.getName()),
					ioEscalation, FlowInstigationStrategyEnum.SEQUENTIAL);

			// Obtain the bean task method
			String beanTaskName = "get" + templateTaskName;
			String beanTaskKey = beanTaskName.toUpperCase();
			TemplateBeanTask beanTask = this.templateBeanTasksByName
					.get(beanTaskKey);

			// Keep track of bean task keys
			templateBeanTaskKeys.add(beanTaskKey);

			// Ensure correct configuration, if template section requires bean
			if (isRequireBean) {

				// Must have template bean task
				if (beanTask == null) {
					designer.addIssue("Missing method '" + beanTaskName
							+ "' on class " + sectionClassName
							+ " to provide bean for template "
							+ templateLocation, AssetType.WORK,
							TEMPLATE_WORK_NANE);

				} else {
					// Ensure bean task does not have a parameter
					if (beanTask.parameter != null) {
						designer.addIssue("Template bean method '"
								+ beanTaskName + "' must not have a "
								+ Parameter.class.getSimpleName()
								+ " annotation", AssetType.TASK, beanTaskName);
					}

					// Obtain the argument type for the template
					Class<?> argumentType = beanTask.type.getReturnType();
					if ((argumentType == null)
							|| (Void.class.equals(argumentType))) {
						// Must provide argument from bean task
						designer.addIssue("Bean method '" + beanTaskName
								+ "' must have return value", AssetType.TASK,
								beanTaskName);

					} else {
						// Determine bean type and whether an array
						Class<?> beanType = argumentType;
						boolean isArray = argumentType.isArray();
						if (isArray) {
							beanType = argumentType.getComponentType();
						}

						// Inform template of bean type
						templateWork.addProperty(
								HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX
										+ templateTaskName, beanType.getName());

						// Handle iterating over array of beans
						if (isArray) {
							// Provide iterator task if array
							SectionWork arrayIteratorWork = designer
									.addSectionWork(
											templateTaskName + "ArrayIterator",
											HttpTemplateArrayIteratorWorkSource.class
													.getName());
							arrayIteratorWork
									.addProperty(
											HttpTemplateArrayIteratorWorkSource.PROPERTY_COMPONENT_TYPE_NAME,
											beanType.getName());
							SectionTask arrayIteratorTask = arrayIteratorWork
									.addSectionTask(
											templateTaskName + "ArrayIterator",
											HttpTemplateArrayIteratorWorkSource.TASK_NAME);
							arrayIteratorTask
									.getTaskObject(
											HttpTemplateArrayIteratorWorkSource.OBJECT_NAME)
									.flagAsParameter();

							// Link iteration of array to rendering
							designer.link(
									arrayIteratorTask
											.getTaskFlow(HttpTemplateArrayIteratorWorkSource.FLOW_NAME),
									templateTask,
									FlowInstigationStrategyEnum.PARALLEL);

							// Iterator is now controller for template
							templateTask = arrayIteratorTask;
						}
					}
				}
			}

			// Determine if first template task
			if (firstTemplateTask == null) {
				// First template task so link to input
				if (beanTask != null) {
					// First template task is bean task
					firstTemplateTask = beanTask.task;

					// Link input to bean then template
					designer.link(sectionInput, beanTask.task);
					designer.link(beanTask.task, templateTask);
				} else {
					// First template task is section rendering
					firstTemplateTask = templateTask;

					// Link input to just template
					designer.link(sectionInput, templateTask);
				}

			} else {
				// Subsequent template tasks so link to previous task
				if (beanTask != null) {
					// Link with bean task then template
					designer.link(previousTemplateTask, beanTask.task);
					designer.link(beanTask.task, templateTask);
				} else {
					// No bean task so link to template
					designer.link(previousTemplateTask, templateTask);
				}
			}

			// Template task is always previous task
			previousTemplateTask = templateTask;
		}

		// Register the #{link} tasks
		for (String linkTaskName : linkNames) {

			// Add the task for handling the link
			SectionTask linkTask = templateWork.addSectionTask(linkTaskName,
					linkTaskName);

			// Obtain the link method task
			String linkMethodTaskName = LINK_METHOD_TASK_NAME_PREFIX
					+ linkTaskName;
			String linkMethodTaskKey = linkMethodTaskName.toUpperCase();
			TemplateBeanTask methodTask = this.templateBeanTasksByName
					.get(linkMethodTaskKey);
			if (methodTask == null) {
				designer.addIssue("No backing method for link '" + linkTaskName
						+ "'", AssetType.TASK, linkTaskName);
				continue; // must have link method
			}

			// Link handling of request to method
			designer.link(linkTask, methodTask.task);
		}

		// Link bean tasks to re-render template by default
		List<String> beanTaskNames = new ArrayList<String>(
				this.templateBeanTasksByName.keySet());
		Collections.sort(beanTaskNames);
		for (String beanTaskKey : this.templateBeanTasksByName.keySet()) {

			// Ignore template bean methods
			if (templateBeanTaskKeys.contains(beanTaskKey)) {
				continue;
			}

			// Obtain the bean method
			TemplateBeanTask methodTask = this.templateBeanTasksByName
					.get(beanTaskKey);

			// Determine if method already indicating next task
			if (!(methodTask.method.isAnnotationPresent(NextTask.class))) {
				// Next task not linked, so link to render template
				designer.link(methodTask.task, firstTemplateTask);
			}
		}

		// Link last template task to output
		SectionOutput output = classSource.getOrCreateOutput(
				ON_COMPLETION_OUTPUT_NAME, null, false);
		designer.link(previousTemplateTask, output);
	}

	/**
	 * {@link SectionTask} for the template bean.
	 */
	private static class TemplateBeanTask {

		/**
		 * {@link SectionTask}.
		 */
		public final SectionTask task;

		/**
		 * {@link TaskType}.
		 */
		public final TaskType<?, ?, ?> type;

		/**
		 * {@link Method} for the {@link SectionTask}.
		 */
		public final Method method;

		/**
		 * Type of parameter for {@link SectionTask}. <code>null</code>
		 * indicates no parameter.
		 */
		public final Class<?> parameter;

		/**
		 * Initiate.
		 * 
		 * @param task
		 *            {@link SectionTask}.
		 * @param type
		 *            {@link TaskType}.
		 * @param method
		 *            {@link Method} for the {@link SectionTask}.
		 * @param parameter
		 *            Type of parameter for {@link SectionTask}.
		 *            <code>null</code> indicates no parameter.
		 */
		public TemplateBeanTask(SectionTask task, TaskType<?, ?, ?> type,
				Method method, Class<?> parameter) {
			this.task = task;
			this.type = type;
			this.method = method;
			this.parameter = parameter;
		}
	}

	/**
	 * {@link ClassSectionSource} specific to the HTTP template.
	 */
	@PrivateSource
	public class HttpTemplateClassSectionSource extends ClassSectionSource {

		/**
		 * {@link HttpParametersObjectManagedObjectSource} instances by their
		 * type.
		 */
		private final Map<Class<?>, SectionManagedObject> httpParmeters = new HashMap<Class<?>, SectionManagedObject>();

		/**
		 * {@link HttpSessionClassManagedObjectSource} instances by their type.
		 */
		private final Map<Class<?>, SectionManagedObject> httpSessionObjects = new HashMap<Class<?>, SectionManagedObject>();

		/**
		 * Determine if the section class is stateful - annotated with
		 * {@link HttpSessionStateful}.
		 * 
		 * @param sectionClass
		 *            Section class.
		 * @return <code>true</code> if stateful.
		 */
		private boolean isHttpSessionStateful(Class<?> sectionClass) {

			// Determine if stateful
			boolean isStateful = sectionClass
					.isAnnotationPresent(HttpSessionStateful.class);

			// Return indicating if stateful
			return isStateful;
		}

		/*
		 * =================== ClassSectionSource ==========================
		 */

		@Override
		protected String getSectionClassName() {
			// Obtain class name from property as location is for template
			return this.getContext().getProperty(PROPERTY_CLASS_NAME);
		}

		@Override
		protected SectionManagedObject createClassManagedObject(
				String objectName, Class<?> sectionClass) {

			// Determine if stateful
			boolean isStateful = this.isHttpSessionStateful(sectionClass);

			// Default behaviour if not stateful
			if (!isStateful) {
				return super.createClassManagedObject(objectName, sectionClass);
			}

			// If stateful, the class must be serialisable
			if (!(Serializable.class.isAssignableFrom(sectionClass))) {
				this.getDesigner().addIssue(
						"Template logic class " + sectionClass.getName()
								+ " is annotated with "
								+ HttpSessionStateful.class.getSimpleName()
								+ " but is not "
								+ Serializable.class.getSimpleName(),
						AssetType.MANAGED_OBJECT, objectName);
			}

			// Create the managed object for the stateful template logic
			SectionManagedObjectSource managedObjectSource = this
					.getDesigner()
					.addSectionManagedObjectSource(objectName,
							HttpSessionClassManagedObjectSource.class.getName());
			managedObjectSource.addProperty(
					HttpSessionClassManagedObjectSource.PROPERTY_CLASS_NAME,
					sectionClass.getName());

			// Create the managed object
			SectionManagedObject managedObject = managedObjectSource
					.addSectionManagedObject(objectName,
							ManagedObjectScope.PROCESS);
			return managedObject;
		}

		@Override
		protected DependencyMetaData[] extractClassManagedObjectDependencies(
				String objectName, Class<?> sectionClass) throws Exception {

			// Extract the dependency meta-data for default behaviour
			DependencyMetaData[] metaData = super
					.extractClassManagedObjectDependencies(objectName,
							sectionClass);

			// Determine if stateful
			boolean isStateful = this.isHttpSessionStateful(sectionClass);

			// If not stateful, return meta-data for default behaviour
			if (!isStateful) {
				return metaData;
			}

			// As stateful, must not have any dependencies into object
			if (metaData.length > 0) {
				this.getDesigner()
						.addIssue(
								"Template logic class "
										+ sectionClass.getName()
										+ " is annotated with "
										+ HttpSessionStateful.class
												.getSimpleName()
										+ " and therefore can not have dependencies injected into the object (only its methods)",
								AssetType.MANAGED_OBJECT, objectName);
			}

			// Return the dependency meta-data for stateful template logic
			return new DependencyMetaData[] { new StatefulDependencyMetaData() };
		}

		@Override
		protected String getTaskName(TaskType<?, ?, ?> taskType) {

			// Obtain the task type name
			String taskTypeName = taskType.getTaskName();

			// Determine if backing method to link task
			boolean isLinkMethod = HttpTemplateSectionSource.this.taskLinkNames
					.contains(taskTypeName);

			// Return prefix on link method task
			return (isLinkMethod ? LINK_METHOD_TASK_NAME_PREFIX + taskTypeName
					: taskTypeName);
		}

		@Override
		protected void enrichTask(SectionTask task, TaskType<?, ?, ?> taskType,
				Method method, Class<?> parameterType) {

			// Determine name of task
			String taskName = task.getSectionTaskName();

			// Register the template bean task (case insensitive)
			HttpTemplateSectionSource.this.templateBeanTasksByName.put(taskName
					.toUpperCase(), new TemplateBeanTask(task, taskType,
					method, parameterType));
		}

		@Override
		protected void linkNextTask(SectionTask task,
				TaskType<?, ?, ?> taskType, Method taskMethod,
				Class<?> argumentType, NextTask nextTaskAnnotation) {

			// Determine if template bean task
			String taskName = taskType.getTaskName();
			if (HttpTemplateSectionSource.this.isTemplateBeanTask(taskName)) {
				// Can not have next task annotation for template bean task
				this.getDesigner().addIssue(
						"Template bean method '" + taskName
								+ "' must not be annotated with "
								+ NextTask.class.getSimpleName(),
						AssetType.TASK, taskName);
				return; // do not link next task
			}

			// Not template bean task, so link next task
			super.linkNextTask(task, taskType, taskMethod, argumentType,
					nextTaskAnnotation);
		}

		@Override
		protected void linkTaskObject(SectionTask task,
				TaskType<?, ?, ?> taskType, TaskObjectType<?> objectType) {

			// Obtain the object name and its type
			String objectName = objectType.getObjectName();
			Class<?> type = objectType.getObjectType();

			// Determine if a HttpParameters object
			if (type.isAnnotationPresent(HttpParameters.class)) {
				// Is a HttpParameters object, so configure as such
				TaskObject taskObject = task.getTaskObject(objectName);

				// Lazy obtain the HttpParmeters object
				SectionManagedObject mo = this.httpParmeters.get(type);
				if (mo == null) {
					// Add the HttpParameters object
					SectionManagedObjectSource source = this
							.getDesigner()
							.addSectionManagedObjectSource(
									"HTTP_PARAMETER_" + type.getName(),
									HttpParametersObjectManagedObjectSource.class
											.getName());
					source.addProperty(
							HttpParametersObjectManagedObjectSource.PROPERTY_CLASS_NAME,
							type.getName());
					mo = source.addSectionManagedObject("HTTP_PARAMETER_MO_"
							+ type.getName(), ManagedObjectScope.PROCESS);

					// Link Server HTTP Connection dependency
					SectionObject serverHttpConnectionObject = this
							.getOrCreateObject(ServerHttpConnection.class
									.getName());
					ManagedObjectDependency serverHttpConnectionDependency = mo
							.getManagedObjectDependency(Dependencies.SERVER_HTTP_CONNECTION
									.name());
					this.getDesigner().link(serverHttpConnectionDependency,
							serverHttpConnectionObject);

					// Register the HttpParameters object
					this.httpParmeters.put(type, mo);
				}

				// Link parameter as HttpParameter
				this.getDesigner().link(taskObject, mo);

				// Setup as HttpParameters object
				return;
			}

			// Determine if a HttpSessionStateful object
			if (type.isAnnotationPresent(HttpSessionStateful.class)) {
				// Is a HttpSessionStateful object, so configure as such
				TaskObject taskObject = task.getTaskObject(objectName);

				// Lazy obtain the HttpSessionStateful object
				SectionManagedObject mo = this.httpSessionObjects.get(type);
				if (mo == null) {
					// Add the HttpSessionStateful object
					SectionManagedObjectSource source = this.getDesigner()
							.addSectionManagedObjectSource(
									"HTTP_SESSION_" + type.getName(),
									HttpSessionClassManagedObjectSource.class
											.getName());
					source.addProperty(
							HttpSessionClassManagedObjectSource.PROPERTY_CLASS_NAME,
							type.getName());
					mo = source.addSectionManagedObject("HTTP_SESSION_MO_"
							+ type.getName(), ManagedObjectScope.PROCESS);

					// Link HTTP Session dependency
					SectionObject httpSessionObject = this
							.getOrCreateObject(HttpSession.class.getName());
					ManagedObjectDependency httpSessionDependency = mo
							.getManagedObjectDependency(net.officefloor.plugin.web.http.session.clazz.source.HttpSessionClassManagedObject.Dependencies.HTTP_SESSION
									.name());
					this.getDesigner().link(httpSessionDependency,
							httpSessionObject);

					// Register the HttpSessionStateful object
					this.httpSessionObjects.put(type, mo);
				}

				// Link parameter as HttpSessionStateful
				this.getDesigner().link(taskObject, mo);

				// Setup as HttpSessionStateful object
				return;
			}

			// Not HttpParameters or HttpSessionStateful object so do default
			super.linkTaskObject(task, taskType, objectType);
		}
	}

}