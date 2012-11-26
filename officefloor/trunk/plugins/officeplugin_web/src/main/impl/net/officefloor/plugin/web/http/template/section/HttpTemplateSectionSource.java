/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.compile.impl.properties.PropertiesUtil;
import net.officefloor.compile.impl.util.CompileUtil;
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
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationWorkSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.session.clazz.source.HttpSessionClassManagedObjectSource;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;

/**
 * {@link SectionSource} for the HTTP template.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionSource extends ClassSectionSource {

	/**
	 * Property name for the {@link HttpTemplate} URI path.
	 */
	public static final String PROPERTY_TEMPLATE_URI = HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI;

	/**
	 * Property name for the {@link Class} providing the backing logic to the
	 * template.
	 */
	public static final String PROPERTY_CLASS_NAME = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME;

	/**
	 * Creates the {@link Task} key from the {@link Task} name.
	 * 
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @return Key for the {@link Task}.
	 */
	private static String createTaskKey(String taskName) {
		// Provide name in upper case to avoid case sensitivity
		return taskName.toUpperCase();
	}

	/**
	 * <p>
	 * Class to use if no class specified.
	 * <p>
	 * Must have a public method for {@link ClassSectionSource}.
	 */
	public static final class NoLogicClass {
		public void notIncludedInput() {
		}
	}

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
		context.addProperty(PROPERTY_TEMPLATE_URI, "URI Path");
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Obtain the section class
		String sectionClassName = context
				.getProperty(PROPERTY_CLASS_NAME, null);
		if (CompileUtil.isBlank(sectionClassName)) {
			// Use the no logic class
			sectionClassName = NoLogicClass.class.getName();
		}

		// Load the section class work and tasks
		this.sectionClass = context.loadClass(sectionClassName);
		super.sourceSection(designer, context);

		// Obtain the HTTP template content
		String templateLocation = context.getSectionLocation();
		SourcePropertiesImpl templateProperties = new SourcePropertiesImpl(
				context);
		templateProperties
				.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE,
						templateLocation);
		SourceContext templateContext = new SourceContextImpl(
				context.isLoadingType(), context, templateProperties);
		Reader templateContentReader = HttpTemplateWorkSource
				.getHttpTemplateContent(templateContext);
		StringBuilder templateContentBuffer = new StringBuilder();
		for (int character = templateContentReader.read(); character != -1; character = templateContentReader
				.read()) {
			templateContentBuffer.append((char) character);
		}
		String templateContent = templateContentBuffer.toString();

		// Obtain the template URI path
		String templateUriPath = context.getProperty(PROPERTY_TEMPLATE_URI);

		// Keep track of tasks that do not render template on their completion
		Set<String> nonRenderTemplateTaskKeys = new HashSet<String>();

		// Extend the template content as necessary
		final String EXTENSION_PREFIX = "extension.";
		int extensionIndex = 1;
		String extensionClassName = context.getProperty(EXTENSION_PREFIX
				+ extensionIndex, null);
		while (extensionClassName != null) {

			// Create an instance of the extension class
			HttpTemplateSectionExtension extension = (HttpTemplateSectionExtension) context
					.loadClass(extensionClassName).newInstance();

			// Extend the template
			String extensionPropertyPrefix = EXTENSION_PREFIX + extensionIndex
					+ ".";
			HttpTemplateSectionExtensionContext extensionContext = new HttpTemplateSectionExtensionContextImpl(
					templateContent, extensionPropertyPrefix,
					nonRenderTemplateTaskKeys);
			extension.extendTemplate(extensionContext);

			// Override template details
			templateContent = extensionContext.getTemplateContent();

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

		// Load the HTTP template
		final String TEMPLATE_WORK_NANE = "TEMPLATE";
		SectionWork templateWork = designer.addSectionWork(TEMPLATE_WORK_NANE,
				HttpTemplateWorkSource.class.getName());
		templateWork.addProperty(
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_CONTENT,
				templateContent);

		// Copy the template configuration
		PropertiesUtil.copyProperties(context, templateWork,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE);
		PropertiesUtil.copyPrefixedProperties(context,
				HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX,
				templateWork);

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

		// Load the HTTP template tasks
		Map<String, SectionTask> contentTasksByName = new HashMap<String, SectionTask>();
		SectionTask firstTemplateTask = null;
		SectionTask previousTemplateTask = null;
		for (HttpTemplateSection templateSection : template.getSections()) {

			// Obtain the template task
			String templateTaskName = templateSection.getSectionName();
			SectionTask templateTask = templateTasks.get(templateTaskName);

			// Keep track of task for later flow linking
			contentTasksByName.put(createTaskKey(templateTaskName),
					templateTask);

			// Determine if template section requires a bean
			boolean isRequireBean = HttpTemplateWorkSource
					.isHttpTemplateSectionRequireBean(templateSection);

			// Link the Server HTTP Connection dependency
			SectionObject connectionObject = this.getOrCreateObject(null,
					ServerHttpConnection.class.getName());
			designer.link(templateTask.getTaskObject("SERVER_HTTP_CONNECTION"),
					connectionObject);

			// Link the HTTP Application Location dependency
			SectionObject locationObject = this.getOrCreateObject(null,
					HttpApplicationLocation.class.getName());
			designer.link(
					templateTask.getTaskObject("HTTP_APPLICATION_LOCATION"),
					locationObject);

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
			String beanMethodName = "get" + templateTaskName;
			String beanTaskKey = createTaskKey(beanMethodName);
			TemplateClassTask beanTask = this.sectionClassMethodTasksByName
					.get(beanTaskKey);
			if (beanTask == null) {
				// Attempt to find with Data suffix
				beanTaskKey = beanTaskKey + "DATA";
				beanTask = this.sectionClassMethodTasksByName.get(beanTaskKey);
			}

			// Ensure that not annotated with NextTask
			if (beanTask != null) {
				// Method providing bean, so ensure no next task
				Method method = beanTask.method;
				if (method.isAnnotationPresent(NextTask.class)) {
					designer.addIssue(
							"Template bean method '" + method.getName()
									+ "' must not be annotated with "
									+ NextTask.class.getSimpleName(),
							AssetType.TASK, beanTaskKey);

					// As NextTask annotation, do not render section
					continue;
				}
			}

			// Bean task to not render template on completion
			nonRenderTemplateTaskKeys.add(beanTaskKey);

			// Ensure correct configuration, if template section requires bean
			if (isRequireBean) {

				// Must have template bean task
				if (beanTask == null) {
					designer.addIssue("Missing method '" + beanMethodName
							+ "' on class " + this.sectionClass.getName()
							+ " to provide bean for template "
							+ templateLocation, AssetType.WORK,
							TEMPLATE_WORK_NANE);

				} else {
					// Ensure bean task does not have a parameter
					if (beanTask.parameter != null) {
						designer.addIssue("Template bean method '"
								+ beanMethodName + "' must not have a "
								+ Parameter.class.getSimpleName()
								+ " annotation", AssetType.TASK, beanMethodName);
					}

					// Obtain the argument type for the template
					Class<?> argumentType = beanTask.type.getReturnType();
					if ((argumentType == null)
							|| (Void.class.equals(argumentType))) {
						// Must provide argument from bean task
						designer.addIssue("Bean method '" + beanMethodName
								+ "' must have return value", AssetType.TASK,
								beanMethodName);

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
			SectionTask contentTask = contentTasksByName
					.get(createTaskKey(flowName));
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

		// Register the #{link} URL continuation tasks
		String[] linkNames = HttpTemplateWorkSource
				.getHttpTemplateLinkNames(template);
		final String linkUrlContinuationPrefix = "HTTP_URL_CONTINUATION_";
		for (String linkTaskName : linkNames) {

			// Create HTTP URL continuation
			SectionWork urlContinuationWork = designer.addSectionWork(
					linkUrlContinuationPrefix + linkTaskName,
					HttpUrlContinuationWorkSource.class.getName());
			urlContinuationWork.addProperty(
					HttpUrlContinuationWorkSource.PROPERTY_URI_PATH,
					templateUriPath + "-" + linkTaskName);
			SectionTask urlContinuationTask = urlContinuationWork
					.addSectionTask(linkUrlContinuationPrefix + linkTaskName,
							HttpUrlContinuationWorkSource.TASK_NAME);

			// Obtain the link method task
			String linkMethodTaskKey = createTaskKey(linkTaskName);
			TemplateClassTask methodTask = this.sectionClassMethodTasksByName
					.get(linkMethodTaskKey);
			if (methodTask == null) {
				// No backing method, so output flow from template
				SectionOutput sectionOutput = this.getOrCreateOutput(
						linkTaskName, null, false);
				designer.link(urlContinuationTask, sectionOutput);
				continue; // linked
			}

			// Link servicing of request to the method
			designer.link(urlContinuationTask, methodTask.task);
		}

		// Link bean tasks to re-render template by default
		List<String> sectionClassMethodTaskNames = new ArrayList<String>(
				this.sectionClassMethodTasksByName.keySet());
		Collections.sort(sectionClassMethodTaskNames);
		for (String beanTaskKey : sectionClassMethodTaskNames) {

			// Determine if render template on completion
			if (!(nonRenderTemplateTaskKeys.contains(beanTaskKey))) {

				// Potentially rendering so obtain the class method
				TemplateClassTask methodTask = this.sectionClassMethodTasksByName
						.get(beanTaskKey);
				Method method = methodTask.method;

				// Determine if method already indicating next task
				if (!(method.isAnnotationPresent(NextTask.class))) {
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
		 * {@link Set} to be populated with keys to {@link Task} instances that
		 * are not to have the template rendered on their completion.
		 */
		private final Set<String> nonRenderTemplateTaskKeys;

		/**
		 * Initiate.
		 * 
		 * @param templateContent
		 *            Raw {@link HttpTemplate} content.
		 * @param extensionPropertyPrefix
		 *            Prefix for a property of this extension.
		 * @param nonRenderTemplateTaskKeys
		 *            {@link Set} to be populated with keys to {@link Task}
		 *            instances that are not to have the template rendered on
		 *            their completion.
		 */
		public HttpTemplateSectionExtensionContextImpl(String templateContent,
				String extensionPropertyPrefix,
				Set<String> nonRenderTemplateTaskKeys) {
			this.templateContent = templateContent;
			this.extensionPropertyPrefix = extensionPropertyPrefix;
			this.nonRenderTemplateTaskKeys = nonRenderTemplateTaskKeys;
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
		public void flagAsNonRenderTemplateMethod(String templateClassMethodName) {
			this.nonRenderTemplateTaskKeys
					.add(createTaskKey(templateClassMethodName));
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
			return HttpTemplateSectionSource.this.getOrCreateObject(null,
					typeName);
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

		// Do not include if no logic class
		if (NoLogicClass.class.equals(this.sectionClass)) {
			return;
		}

		// Keep track of the tasks to allow linking by case-insensitive names
		String taskKey = createTaskKey(task.getSectionTaskName());
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