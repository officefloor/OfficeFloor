/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
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
		SectionInput sectionInput = designer.addSectionInput("renderTemplate",
				null);

		// Register the HTTP template sections requiring a bean
		PropertyList templateProperties = context.createPropertyList();
		templateProperties.addProperty(
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE).setValue(
				templateLocation);
		HttpTemplate template = HttpTemplateWorkSource.getHttpTemplate(
				templateProperties.getProperties(), context.getClassLoader());
		for (HttpTemplateSection templateSection : template.getSections()) {
			String templateSectionName = templateSection.getSectionName();
			boolean isRequireBean = HttpTemplateWorkSource
					.isHttpTemplateSectionRequireBean(templateSection);
			this.isRequireBeanTemplates.put(templateSectionName.toUpperCase(),
					new Boolean(isRequireBean));
		}

		// Load the section class
		HttpTemplateClassSectionSource classSource = new HttpTemplateClassSectionSource();
		classSource.sourceSection(designer, context);

		// Load the HTTP template
		final String TEMPLATE_WORK_NANE = "TEMPLATE";
		SectionWork templateWork = designer.addSectionWork(TEMPLATE_WORK_NANE,
				HttpTemplateWorkSource.class.getName());
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE,
				templateLocation);

		// Load the HTTP template tasks
		SectionTask previousTemplateTask = null;
		for (HttpTemplateSection templateSection : template.getSections()) {

			// Obtain the template task name
			String templateTaskName = templateSection.getSectionName();

			// Add the template task
			SectionTask templateTask = templateWork.addSectionTask(
					templateTaskName, templateTaskName);

			// Obtain the template bean method (if require bean)
			SectionTask beanTask = null;
			boolean isRequireBean = HttpTemplateWorkSource
					.isHttpTemplateSectionRequireBean(templateSection);
			if (isRequireBean) {
				// Obtain the bean task method
				String beanTaskName = "get" + templateTaskName;
				TemplateBeanTask templateBeanTask = this.templateBeanTasksByName
						.get(beanTaskName.toUpperCase());
				if (templateBeanTask == null) {
					// Must have template bean task
					designer.addIssue("Missing method '" + beanTaskName
							+ "' on class " + sectionClassName
							+ " to provide bean for template "
							+ templateLocation, AssetType.WORK,
							TEMPLATE_WORK_NANE);

				} else {
					// Load the template bean task
					beanTask = templateBeanTask.task;

					// Obtain the argument type for the template
					Class<?> argumentType = templateBeanTask.type
							.getReturnType();
					if (argumentType == null) {
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

						// TODO handle arrays
					}
				}
			}

			// Determine if first template task
			if (previousTemplateTask == null) {
				// First template task so link to input
				if (beanTask != null) {
					// Link with bean task
					designer.link(sectionInput, beanTask);
					designer.link(beanTask, templateTask);
				} else {
					// No bean task required
					designer.link(sectionInput, templateTask);
				}

			} else {
				// Subsequent template tasks so link to previous task
				if (beanTask != null) {
					// Link with bean task
					designer.link(previousTemplateTask, beanTask);
					designer.link(beanTask, templateTask);
				} else {
					// No bean task required
					designer.link(previousTemplateTask, templateTask);
				}
			}

			// Template task is always previous task
			previousTemplateTask = templateTask;
		}

		// TODO register the #{link} tasks

		// Link last template task to output
		SectionOutput output = classSource.getOrCreateOutput("output", null,
				false);
		designer.link(previousTemplateTask, output);
	}

	/**
	 * {@link SectionTask} for the template bean.
	 */
	private static class TemplateBeanTask {

		/**
		 * {@link SectionTask}.
		 */
		public SectionTask task;

		/**
		 * {@link TaskType}.
		 */
		public TaskType<?, ?, ?> type;

		/**
		 * Initiate.
		 * 
		 * @param task
		 *            {@link SectionTask}.
		 * @param type
		 *            {@link TaskType}.
		 */
		public TemplateBeanTask(SectionTask task, TaskType<?, ?, ?> type) {
			this.task = task;
			this.type = type;
		}
	}

	/**
	 * {@link ClassSectionSource} specific to the HTTP template.
	 */
	public class HttpTemplateClassSectionSource extends ClassSectionSource {

		@Override
		protected String getSectionClassName() {
			// Obtain class name from property as location is for template
			return this.getContext().getProperty(PROPERTY_CLASS_NAME);
		}

		@Override
		protected void enrichTask(SectionTask task, TaskType<?, ?, ?> taskType,
				Method method, Class<?> parameterType) {

			// Determine if template bean task
			String taskName = taskType.getTaskName();
			if (HttpTemplateSectionSource.this.isTemplateBeanTask(taskName)) {

				// Ensure bean task does not have a parameter
				if (parameterType != null) {
					this.getDesigner().addIssue(
							"Template bean method '" + taskName
									+ "' must not have a "
									+ Parameter.class.getSimpleName()
									+ " annotation", AssetType.TASK, taskName);
				}

				// Register the template bean task
				HttpTemplateSectionSource.this.templateBeanTasksByName.put(
						taskName.toUpperCase(), new TemplateBeanTask(task,
								taskType));
			}
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
		protected void linkTaskFlow(SectionTask task,
				TaskType<?, ?, ?> taskType, Class<?> flowInterfaceType,
				Method flowMethod, Class<?> flowArgumentType) {

			// Determine if template bean task
			String taskName = taskType.getTaskName();
			if (HttpTemplateSectionSource.this.isTemplateBeanTask(taskName)) {
				// Can not have flows for template bean task
				this.getDesigner().addIssue(
						"Template bean method '" + taskName
								+ "' must not have a flow parameter ("
								+ flowInterfaceType.getName() + ")",
						AssetType.TASK, taskName);
				return; // do not link flow
			}

			// Not template bean task, so link flow
			super.linkTaskFlow(task, taskType, flowInterfaceType, flowMethod,
					flowArgumentType);
		}
	}

}