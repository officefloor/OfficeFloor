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
package net.officefloor.plugin.section.work;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * {@link SectionSource} implementation that wraps a {@link WorkSource} to
 * expose it as a section.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSectionSource extends AbstractSectionSource {

	/**
	 * Name of property prefix to specify the index of the parameter on the
	 * {@link Work}. The resulting parameter name and value are as:
	 * 
	 * <pre>
	 *      PROPERTY_PARAMETER_PREFIX + "TaskName" = "index in objects for the parameter (starting at 1)"
	 * </pre>
	 */
	public static final String PROPERTY_PARAMETER_PREFIX = "parameter.index.prefix.";

	/**
	 * Name of property specifying a comma separated list of {@link Task} names
	 * that will have a {@link SectionOutput} created and linked as next.
	 */
	public static final String PROPERTY_TASKS_NEXT_TO_OUTPUTS = "tasks.next.to.outputs";

	/*
	 * ====================== SectionSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Obtain the work source name
		String workSourceName = context.getSectionLocation();

		// Obtain the properties
		PropertyList properties = context.createPropertyList();
		for (String name : context.getPropertyNames()) {
			String value = context.getProperty(name);
			properties.addProperty(name).setValue(value);
		}

		// Obtain the work type
		WorkType<?> workType = context.loadWorkType(workSourceName, properties);

		// Add the work
		SectionWork work = designer.addSectionWork("WORK", workSourceName);
		for (Property property : properties) {
			work.addProperty(property.getName(), property.getValue());
		}

		// Determine the tasks which have next outputs
		Set<String> tasksWithNextOutput = new HashSet<String>();
		String nextTasksText = context.getProperty(
				PROPERTY_TASKS_NEXT_TO_OUTPUTS, "");
		for (String nextTaskName : nextTasksText.split(",")) {
			tasksWithNextOutput.add(nextTaskName.trim());
		}

		// Add the tasks
		Map<String, SectionObject> sectionObjects = new HashMap<String, SectionObject>();
		Map<String, SectionOutput> sectionOutputs = new HashMap<String, SectionOutput>();
		for (TaskType<?, ?, ?> taskType : workType.getTaskTypes()) {

			// Obtain the task name
			String taskName = taskType.getTaskName();

			// Add the task
			SectionTask task = work.addSectionTask(taskName, taskName);

			// Determine the index of the parameter
			int parameterIndex = Integer.parseInt(context.getProperty(
					PROPERTY_PARAMETER_PREFIX + taskName, "-1"));

			// Link objects and flag parameter
			Class<?> parameterType = null;
			int objectIndex = 1;
			for (TaskObjectType<?> objectType : taskType.getObjectTypes()) {

				// Obtain object details
				String objectName = objectType.getObjectName();
				Class<?> objectClass = objectType.getObjectType();
				String typeQualifier = objectType.getTypeQualifier();

				// Determine the section object name
				String sectionObjectName = (typeQualifier == null ? ""
						: typeQualifier + "-") + objectClass.getName();

				// Obtain the object
				TaskObject object = task.getTaskObject(objectName);

				// Determine if parameter
				if (objectIndex == parameterIndex) {
					// Flag as parameter
					parameterType = objectClass;
					object.flagAsParameter();

				} else {
					// Obtain the section object
					SectionObject sectionObject = sectionObjects
							.get(sectionObjectName);
					if (sectionObject == null) {
						sectionObject = designer.addSectionObject(
								sectionObjectName, objectClass.getName());
						if (typeQualifier != null) {
							sectionObject.setTypeQualifier(typeQualifier);
						}
						sectionObjects.put(sectionObjectName, sectionObject);
					}

					// Specify as section object dependency
					designer.link(object, sectionObject);
				}

				// Increment object index for next iteration
				objectIndex++;
			}

			// Link next output (if configured)
			if (tasksWithNextOutput.contains(taskName)) {

				// Obtain argument type for task
				Class<?> returnType = taskType.getReturnType();

				// Obtain the section output
				SectionOutput sectionOutput = sectionOutputs.get(taskName);
				if (sectionOutput == null) {
					sectionOutput = designer.addSectionOutput(taskName,
							(returnType == null ? null : returnType.getName()),
							false);
					sectionOutputs.put(taskName, sectionOutput);
				}

				// Link task to next output
				designer.link(task, sectionOutput);
			}

			// Link task flows to section outputs
			for (TaskFlowType<?> flowType : taskType.getFlowTypes()) {

				// Obtain the flow details
				String flowName = flowType.getFlowName();
				Class<?> argumentType = flowType.getArgumentType();

				// Obtain the flow
				TaskFlow flow = task.getTaskFlow(flowName);

				// Obtain the section output
				SectionOutput sectionOutput = sectionOutputs.get(flowName);
				if (sectionOutput == null) {
					sectionOutput = designer.addSectionOutput(
							flowName,
							(argumentType == null ? null : argumentType
									.getName()), false);
					sectionOutputs.put(flowName, sectionOutput);
				}

				// Link flow to output
				designer.link(flow, sectionOutput,
						FlowInstigationStrategyEnum.SEQUENTIAL);
			}

			// Link task escalations to section outputs
			for (TaskEscalationType escalationType : taskType
					.getEscalationTypes()) {

				// Obtain the escalation type
				Class<? extends Throwable> escalation = escalationType
						.getEscalationType();

				// Obtain the escalation
				TaskFlow flow = task.getTaskEscalation(escalation.getName());

				// Obtain the section output
				String outputName = escalation.getName();
				SectionOutput sectionOutput = sectionOutputs.get(outputName);
				if (sectionOutput == null) {
					sectionOutput = designer.addSectionOutput(outputName,
							escalation.getName(), true);
					sectionOutputs.put(outputName, sectionOutput);
				}

				// Link the escalation to output
				designer.link(flow, sectionOutput,
						FlowInstigationStrategyEnum.SEQUENTIAL);
			}

			// Link task for input
			SectionInput input = designer.addSectionInput(taskName,
					(parameterType == null ? null : parameterType.getName()));
			designer.link(input, task);
		}
	}

}