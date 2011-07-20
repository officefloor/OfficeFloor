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
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.plugin.web.http.session.clazz.source.HttpSessionClassManagedObjectSource;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.RawHttpTemplateLoader;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;

/**
 * {@link SectionSource} for the HTTP template.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionSource extends ClassSectionSource {

	/**
	 * Registers the {@link RawHttpTemplateLoader}.
	 * 
	 * @param rawHttpTemplateLoader
	 *            {@link RawHttpTemplateLoader}.
	 */
	public static void registerRawHttpTemplateLoader(
			RawHttpTemplateLoader rawHttpTemplateLoader) {
		HttpTemplateWorkSource
				.registerRawHttpTemplateLoader(rawHttpTemplateLoader);
	}

	/**
	 * <p>
	 * Unregisters all the {@link RawHttpTemplateLoader} instances.
	 * <p>
	 * This is typically only made available to allow resetting content for
	 * testing.
	 */
	public static void unregisterAllRawHttpTemplateLoaders() {
		HttpTemplateWorkSource.unregisterAllRawHttpTemplateLoaders();
	}

	/**
	 * Property name for the {@link Class} providing the backing logic to the
	 * template.
	 */
	public static final String PROPERTY_CLASS_NAME = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME;

	/**
	 * Name of property for the prefix on the {@link HttpTemplateWorkSource}
	 * link {@link Task} instances.
	 */
	public static final String PROPERTY_LINK_TASK_NAME_PREFIX = "link.task.name.prefix";

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
	 * {@link Class} providing the logic for the HTTP template - also the
	 * {@link Class} for the {@link ClassSectionSource}.
	 */
	private Class<?> sectionClass = null;

	/**
	 * {@link SectionManagedObject} for the section object.
	 */
	private SectionManagedObject sectionClassManagedObject = null;

	/**
	 * {@link TemplateBeanTask} for the section {@link Class} method by its
	 * name.
	 */
	private final Map<String, TemplateClassTask> sectionClassMethodTasksByName = new HashMap<String, TemplateClassTask>();

	/**
	 * Listing of the {@link TemplateFlowLink} instances.
	 */
	private final List<TemplateFlowLink> flowLinks = new LinkedList<TemplateFlowLink>();

	/*
	 * ===================== SectionSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLASS_NAME, "Class");
		context.addProperty(PROPERTY_LINK_TASK_NAME_PREFIX,
				"Link service Task name prefix");
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Obtain the class loader
		ClassLoader classLoader = context.getClassLoader();

		// Load the section class work and tasks
		String sectionClassName = context.getProperty(PROPERTY_CLASS_NAME);
		this.sectionClass = classLoader.loadClass(sectionClassName);
		super.sourceSection(designer, context);

		// Obtain the HTTP template content
		String templateLocation = context.getSectionLocation();
		SourcePropertiesImpl templateProperties = new SourcePropertiesImpl(
				context);
		templateProperties
				.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE,
						templateLocation);
		Reader templateContentReader = HttpTemplateWorkSource
				.getHttpTemplateContent(templateProperties, classLoader);
		StringBuilder templateContentBuffer = new StringBuilder();
		for (int character = templateContentReader.read(); character != -1; character = templateContentReader
				.read()) {
			templateContentBuffer.append((char) character);
		}
		String templateContent = templateContentBuffer.toString();

		// Extend the template content as necessary
		final String EXTENSION_PREFIX = "extension.";
		int extensionIndex = 1;
		String extensionClassName = context.getProperty(EXTENSION_PREFIX
				+ extensionIndex, null);
		while (extensionClassName != null) {

			// Create an instance of the extension class
			HttpTemplateSectionExtension extension = (HttpTemplateSectionExtension) classLoader
					.loadClass(extensionClassName).newInstance();

			// Extend the template
			String extensionPropertyPrefix = EXTENSION_PREFIX + extensionIndex
					+ ".";
			HttpTemplateSectionExtensionContext extensionContext = new HttpTemplateSectionExtensionContextImpl(
					templateContent, extensionPropertyPrefix);
			extension.extendTemplate(extensionContext);

			// Override template details
			templateContent = extensionContext.getTemplateContent();
			sectionClass = extensionContext.getTemplateClass();

			// Initiate for next extension
			extensionIndex++;
			extensionClassName = context.getProperty(EXTENSION_PREFIX
					+ extensionIndex, null);
		}

		// Obtain the HTTP template
		HttpTemplate template = HttpTemplateWorkSource
				.getHttpTemplate(new StringReader(templateContent));

		// Create the input to the section
		SectionInput sectionInput = designer.addSectionInput(
				RENDER_TEMPLATE_INPUT_NAME, null);

		// Name of work is exposed on URL for links.
		// Result is: /<section>.links-<link>.task
		final String TEMPLATE_WORK_NANE = "links";

		// Load the HTTP template
		SectionWork templateWork = designer.addSectionWork(TEMPLATE_WORK_NANE,
				HttpTemplateWorkSource.class.getName());
		templateWork.addProperty(
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_CONTENT,
				templateContent);

		// Create the template tasks and ensure registered for logic flows
		Map<String, SectionTask> templateTasks = new HashMap<String, SectionTask>();
		for (HttpTemplateSection templateSection : template.getSections()) {

			// Obtain the template task name
			String templateTaskName = templateSection.getSectionName();

			// Add the template task
			SectionTask templateTask = templateWork.addSectionTask(
					templateTaskName, templateTaskName);

			// Register the template task
			templateTasks.put(templateTaskName, templateTask);
		}

		// Keep track of template bean task keys
		Set<String> templateBeanTaskKeys = new HashSet<String>();

		// Load the HTTP template tasks
		Map<String, SectionTask> contentTasksByName = new HashMap<String, SectionTask>();
		SectionTask firstTemplateTask = null;
		SectionTask previousTemplateTask = null;
		for (HttpTemplateSection templateSection : template.getSections()) {

			// Obtain the template task
			String templateTaskName = templateSection.getSectionName();
			SectionTask templateTask = templateTasks.get(templateTaskName);

			// Keep track of task for later flow linking
			contentTasksByName
					.put(templateTaskName.toUpperCase(), templateTask);

			// Determine if template section requires a bean
			boolean isRequireBean = HttpTemplateWorkSource
					.isHttpTemplateSectionRequireBean(templateSection);

			// Link the Server HTTP Connection dependency
			SectionObject connectionObject = this
					.getOrCreateObject(ServerHttpConnection.class.getName());
			designer.link(templateTask.getTaskObject("SERVER_HTTP_CONNECTION"),
					connectionObject);

			// Flag bean as parameter if requires a bean
			if (isRequireBean) {
				templateTask.getTaskObject("OBJECT").flagAsParameter();
			}

			// Link the I/O escalation
			SectionOutput ioEscalation = this.getOrCreateOutput(
					IOException.class.getName(), IOException.class.getName(),
					true);
			designer.link(
					templateTask.getTaskEscalation(IOException.class.getName()),
					ioEscalation, FlowInstigationStrategyEnum.SEQUENTIAL);

			// Obtain the bean task method
			String beanTaskName = "get" + templateTaskName;
			String beanTaskKey = beanTaskName.toUpperCase();
			TemplateClassTask beanTask = this.sectionClassMethodTasksByName
					.get(beanTaskKey);

			// Keep track of bean task keys
			templateBeanTaskKeys.add(beanTaskKey);

			// Ensure correct configuration, if template section requires bean
			if (isRequireBean) {

				// Must have template bean task
				if (beanTask == null) {
					designer.addIssue("Missing method '" + beanTaskName
							+ "' on class " + sectionClass.getName()
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

		// Link flows to template content tasks
		for (TemplateFlowLink flowLink : this.flowLinks) {

			// Obtain the task flow and its name
			TaskFlow taskFlow = flowLink.taskFlow;
			String flowName = taskFlow.getTaskFlowName();

			// Determine if linking to content task
			SectionTask contentTask = contentTasksByName.get(flowName
					.toUpperCase());
			if (contentTask != null) {
				// Link to content task
				designer.link(taskFlow, contentTask,
						FlowInstigationStrategyEnum.SEQUENTIAL);

			} else {
				// Not linked to content task, so use default behaviour
				super.linkTaskFlow(flowLink.taskFlow, flowLink.taskType,
						flowLink.flowInterfaceType, flowLink.flowMethod,
						flowLink.flowArgumentType);
			}
		}

		// Obtain the link task name prefix
		String linkTaskNamePrefix = context
				.getProperty(PROPERTY_LINK_TASK_NAME_PREFIX);

		// Register the #{link} tasks
		String[] linkNames = HttpTemplateWorkSource
				.getHttpTemplateLinkNames(template);
		for (String linkTaskName : linkNames) {

			// Add the task for handling the link
			String linkServiceTaskName = linkTaskNamePrefix + linkTaskName;
			SectionTask linkTask = templateWork.addSectionTask(
					linkServiceTaskName, linkTaskName);

			// Obtain the link method task
			String linkMethodTaskKey = linkTaskName.toUpperCase();
			TemplateClassTask methodTask = this.sectionClassMethodTasksByName
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
		List<String> sectionClassMethodTaskNames = new ArrayList<String>(
				this.sectionClassMethodTasksByName.keySet());
		Collections.sort(sectionClassMethodTaskNames);
		for (String beanTaskKey : sectionClassMethodTaskNames) {

			// Obtain the class method
			TemplateClassTask methodTask = this.sectionClassMethodTasksByName
					.get(beanTaskKey);
			Method method = methodTask.method;

			// Determine if method providing bean to template
			if (templateBeanTaskKeys.contains(beanTaskKey)) {
				// Method providing bean, so ensure no next task
				if (method.isAnnotationPresent(NextTask.class)) {
					designer.addIssue(
							"Template bean method '" + method.getName()
									+ "' must not be annotated with "
									+ NextTask.class.getSimpleName(),
							AssetType.TASK, beanTaskKey);
				}

			} else {
				// Determine if method already indicating next task
				if (!(methodTask.method.isAnnotationPresent(NextTask.class))) {
					// Next task not linked, so link to render template
					designer.link(methodTask.task, firstTemplateTask);
				}
			}
		}

		// Link last template task to output
		SectionOutput output = this.getOrCreateOutput(
				ON_COMPLETION_OUTPUT_NAME, null, false);
		designer.link(previousTemplateTask, output);
	}

	/**
	 * {@link SectionTask} for the template class.
	 */
	private static class TemplateClassTask {

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
		public TemplateClassTask(SectionTask task, TaskType<?, ?, ?> type,
				Method method, Class<?> parameter) {
			this.task = task;
			this.type = type;
			this.method = method;
			this.parameter = parameter;
		}
	}

	/**
	 * Template {@link TaskFlow} instances to be linked.
	 */
	private static class TemplateFlowLink {

		/**
		 * {@link TaskFlow} to be linked.
		 */
		public final TaskFlow taskFlow;

		/**
		 * {@link TaskType} of the {@link Task} for the {@link TaskFlow}.
		 */
		public final TaskType<?, ?, ?> taskType;

		/**
		 * Flow interface type.
		 */
		public final Class<?> flowInterfaceType;

		/**
		 * Flow interface method.
		 */
		public final Method flowMethod;

		/**
		 * Flow interface method argument type.
		 */
		public final Class<?> flowArgumentType;

		/**
		 * Initiate.
		 * 
		 * @param taskFlow
		 *            {@link TaskFlow} to be linked.
		 * @param taskType
		 *            {@link TaskType} of the {@link Task} for the
		 *            {@link TaskFlow}.
		 * @param flowInterfaceType
		 *            Flow interface type.
		 * @param flowMethod
		 *            Flow interface method.
		 * @param flowArgumentType
		 *            Flow interface method argument type.
		 */
		public TemplateFlowLink(TaskFlow taskFlow, TaskType<?, ?, ?> taskType,
				Class<?> flowInterfaceType, Method flowMethod,
				Class<?> flowArgumentType) {
			this.taskFlow = taskFlow;
			this.taskType = taskType;
			this.flowInterfaceType = flowInterfaceType;
			this.flowMethod = flowMethod;
			this.flowArgumentType = flowArgumentType;
		}
	}

	/**
	 * {@link HttpTemplateSectionExtensionContext} implementation.
	 */
	private class HttpTemplateSectionExtensionContextImpl implements
			HttpTemplateSectionExtensionContext {

		/**
		 * Raw {@link HttpTemplate} content.
		 */
		private String templateContent;

		/**
		 * Prefix for a property of this extension.
		 */
		private final String extensionPropertyPrefix;

		/**
		 * Initiate.
		 * 
		 * @param templateContent
		 *            Raw {@link HttpTemplate} content.
		 * @param extensionPropertyPrefix
		 *            Prefix for a property of this extension.
		 * @param sectionDesigner
		 *            {@link SectionDesigner}.
		 * @param sectionSourceContext
		 *            {@link SectionSourceContext}.
		 * @param templateLogicObject
		 *            {@link SectionManagedObject} for the template logic
		 *            object.
		 */
		public HttpTemplateSectionExtensionContextImpl(String templateContent,
				String extensionPropertyPrefix) {
			this.templateContent = templateContent;
			this.extensionPropertyPrefix = extensionPropertyPrefix;
		}

		/*
		 * ============== HttpTemplateSectionExtensionContext ================
		 */

		@Override
		public String getTemplateContent() {
			return this.templateContent;
		}

		@Override
		public void setTemplateContent(String templateContent) {
			this.templateContent = templateContent;
		}

		@Override
		public Class<?> getTemplateClass() {
			return HttpTemplateSectionSource.this.sectionClass;
		}

		@Override
		public String[] getPropertyNames() {

			// Obtain all the property names
			String[] contextNames = HttpTemplateSectionSource.this.getContext()
					.getPropertyNames();

			// Filter to just this extension's properties
			List<String> extensionNames = new LinkedList<String>();
			for (String contextName : contextNames) {
				if (contextName.startsWith(this.extensionPropertyPrefix)) {
					// Add the extension property name
					String extensionName = contextName
							.substring(this.extensionPropertyPrefix.length());
					extensionNames.add(extensionName);
				}
			}

			// Return the extension names
			return extensionNames.toArray(new String[extensionNames.size()]);
		}

		@Override
		public String getProperty(String name) throws UnknownPropertyError {
			// Obtain the extension property value
			return HttpTemplateSectionSource.this.getContext().getProperty(
					this.extensionPropertyPrefix + name);
		}

		@Override
		public String getProperty(String name, String defaultValue) {
			// Obtain the extension property value
			return HttpTemplateSectionSource.this.getContext().getProperty(
					this.extensionPropertyPrefix + name, defaultValue);
		}

		@Override
		public Properties getProperties() {

			// Obtain all the properties
			Properties properties = new Properties();

			// Filter to just this extension's properties
			String[] contextNames = HttpTemplateSectionSource.this.getContext()
					.getPropertyNames();
			for (String contextName : contextNames) {
				if (contextName.startsWith(this.extensionPropertyPrefix)) {
					// Add the extension property name
					String extensionName = contextName
							.substring(this.extensionPropertyPrefix.length());
					String value = HttpTemplateSectionSource.this.getContext()
							.getProperty(contextName);
					properties.setProperty(extensionName, value);
				}
			}

			// Return the properties
			return properties;
		}

		@Override
		public SectionSourceContext getSectionSourceContext() {
			return HttpTemplateSectionSource.this.getContext();
		}

		@Override
		public SectionDesigner getSectionDesigner() {
			return HttpTemplateSectionSource.this.getDesigner();
		}

		@Override
		public SectionManagedObject getTemplateLogicObject() {
			return HttpTemplateSectionSource.this.sectionClassManagedObject;
		}

		@Override
		public SectionTask getTask(String taskName) {
			return HttpTemplateSectionSource.this.getTaskByName(taskName);
		}

		@Override
		public SectionObject getOrCreateSectionObject(String typeName) {
			return HttpTemplateSectionSource.this.getOrCreateObject(typeName);
		}

		@Override
		public SectionOutput getOrCreateSectionOutput(String name,
				String argumentType, boolean isEscalationOnly) {
			return HttpTemplateSectionSource.this.getOrCreateOutput(name,
					argumentType, isEscalationOnly);
		}
	}

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
		return this.sectionClass.getName();
	}

	@Override
	protected Class<?> getSectionClass(String sectionClassName)
			throws Exception {
		return this.sectionClass;
	}

	@Override
	protected SectionManagedObject createClassManagedObject(String objectName,
			Class<?> sectionClass) {

		// Determine if already loaded the Section Managed Object
		if (this.sectionClassManagedObject != null) {
			return this.sectionClassManagedObject; // instance
		}

		// Determine if stateful
		boolean isStateful = this.isHttpSessionStateful(sectionClass);

		// Default behaviour if not stateful
		if (!isStateful) {
			// Defer to default behaviour
			this.sectionClassManagedObject = super.createClassManagedObject(
					objectName, sectionClass);

		} else {
			// As stateful, the class must be serialisable
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
			this.sectionClassManagedObject = managedObjectSource
					.addSectionManagedObject(objectName,
							ManagedObjectScope.PROCESS);
		}

		// Return the managed object
		return this.sectionClassManagedObject;
	}

	@Override
	protected DependencyMetaData[] extractClassManagedObjectDependencies(
			String objectName, Class<?> sectionClass) throws Exception {

		// Extract the dependency meta-data for default behaviour
		DependencyMetaData[] metaData = super
				.extractClassManagedObjectDependencies(objectName, sectionClass);

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
									+ HttpSessionStateful.class.getSimpleName()
									+ " and therefore can not have dependencies injected into the object (only its methods)",
							AssetType.MANAGED_OBJECT, objectName);
		}

		// Return the dependency meta-data for stateful template logic
		return new DependencyMetaData[] { new StatefulDependencyMetaData() };
	}

	@Override
	protected void enrichTask(SectionTask task, TaskType<?, ?, ?> taskType,
			Method method, Class<?> parameterType) {

		// Keep track of the tasks to allow linking by case-insensitive names
		String taskKey = task.getSectionTaskName().toUpperCase();
		this.sectionClassMethodTasksByName.put(taskKey, new TemplateClassTask(
				task, taskType, method, parameterType));

		// Enrich the task
		super.enrichTask(task, taskType, method, parameterType);
	}

	@Override
	protected void linkTaskFlow(TaskFlow taskFlow, TaskType<?, ?, ?> taskType,
			Class<?> flowInterfaceType, Method flowMethod,
			Class<?> flowArgumentType) {
		// At this stage, the template content tasks are not available.
		// Therefore just keep track of flows for later linking.
		this.flowLinks.add(new TemplateFlowLink(taskFlow, taskType,
				flowInterfaceType, flowMethod, flowArgumentType));
	}

}