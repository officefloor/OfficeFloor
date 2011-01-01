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
package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.plugin.section.clazz.SectionClassWorkSource.SectionTaskFactory;
import net.officefloor.plugin.work.clazz.FlowInterface;
import net.officefloor.plugin.work.clazz.FlowMethodMetaData;
import net.officefloor.plugin.work.clazz.FlowParameterFactory;
import net.officefloor.plugin.work.clazz.ParameterFactory;

/**
 * Class {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSource extends AbstractSectionSource {

	/*
	 * =================== SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties as uses location for class
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Obtain the class
		String sectionClassName = context.getSectionLocation();
		if ((sectionClassName == null)
				|| (sectionClassName.trim().length() == 0)) {
			designer.addIssue(
					"Must specify section class name within the location",
					null, null);
			return; // not able to load if no section class specified
		}
		Class<?> sectionClass = context.getClassLoader().loadClass(
				sectionClassName);

		// Add the managed object for the section class
		SectionManagedObjectSource managedObjectSource = designer
				.addSectionManagedObjectSource("OBJECT",
						SectionClassManagedObjectSource.class.getName());
		managedObjectSource.addProperty(
				SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				sectionClassName);
		SectionManagedObject managedObject = managedObjectSource
				.addSectionManagedObject("OBJECT", ManagedObjectScope.THREAD);

		// Obtain the dependency meta-data
		DependencyMetaData[] dependencyMetaData = new SectionClassManagedObjectSource()
				.extractDependencyMetaData(sectionClass);

		// Load the managed objects
		Map<String, SectionObject> sectionObjects = new HashMap<String, SectionObject>();
		Map<String, SectionManagedObject> sectionManagedObjects = new HashMap<String, SectionManagedObject>();
		for (DependencyMetaData dependency : dependencyMetaData) {

			// Obtain dependency name and type
			String dependencyName = dependency.name;
			String dependencyTypeName = dependency.field.getType().getName();

			// Obtain the managed object dependency
			ManagedObjectDependency moDependency = managedObject
					.getManagedObjectDependency(dependencyName);

			// Determine if managed object
			ManagedObject moAnnotation = dependency.field
					.getAnnotation(ManagedObject.class);
			if (moAnnotation != null) {
				// Add the managed object
				SectionManagedObjectSource mos = designer
						.addSectionManagedObjectSource(dependencyTypeName,
								moAnnotation.source().getName());
				for (Property property : moAnnotation.properties()) {
					String value = ("".equals(property.value()) ? property
							.valueClass().getName() : property.value());
					mos.addProperty(property.name(), value);
				}
				SectionManagedObject mo = mos.addSectionManagedObject(
						dependencyTypeName, ManagedObjectScope.PROCESS);
				sectionManagedObjects.put(dependencyTypeName, mo);

				// Link dependency to managed object
				designer.link(moDependency, mo);

			} else {
				// Link to external object (by type)
				SectionObject sectionObject = sectionObjects
						.get(dependencyTypeName);
				if (sectionObject == null) {
					// No yet added, so add section object
					sectionObject = designer.addSectionObject(
							dependencyTypeName, dependencyTypeName);
					sectionObjects.put(dependencyTypeName, sectionObject);
				}
				designer.link(moDependency, sectionObject);
			}
		}

		// Link the managed object dependencies
		for (DependencyMetaData dependency : dependencyMetaData) {

			// Obtain managed object name
			String moName = dependency.field.getType().getName();

			// Obtain the managed object annotation
			ManagedObject annotation = dependency.field
					.getAnnotation(ManagedObject.class);
			if (annotation == null) {
				continue; // not managed object dependency
			}

			// Obtain the managed object
			SectionManagedObject mo = sectionManagedObjects.get(moName);

			// Load the managed object type
			PropertyList moProperties = context.createPropertyList();
			for (Property property : annotation.properties()) {
				String value = ("".equals(property.value()) ? property
						.valueClass().getName() : property.value());
				moProperties.addProperty(property.name()).setValue(value);
			}
			ManagedObjectType<?> moType = context.loadManagedObjectType(
					annotation.source().getName(), moProperties);

			// Link the dependencies for the managed object
			for (ManagedObjectDependencyType<?> dependencyType : moType
					.getDependencyTypes()) {

				// Obtain the dependency type
				String dependencyTypeName = dependencyType.getDependencyType()
						.getName();

				// Obtain the managed object dependency
				ManagedObjectDependency moDependency = mo
						.getManagedObjectDependency(dependencyType
								.getDependencyName());

				// First attempt to link internally
				SectionManagedObject dependencyMo = sectionManagedObjects
						.get(dependencyTypeName);
				if (dependencyMo != null) {
					// Link to managed object
					designer.link(moDependency, dependencyMo);

				} else {
					// Link to external object (by type)
					SectionObject sectionObject = sectionObjects
							.get(dependencyTypeName);
					if (sectionObject == null) {
						// No yet added, so add section object
						sectionObject = designer.addSectionObject(
								dependencyTypeName, dependencyTypeName);
						sectionObjects.put(dependencyTypeName, sectionObject);
					}
					designer.link(moDependency, sectionObject);
				}
			}
		}

		// Load the work type for the class
		PropertyList workProperties = context.createPropertyList();
		workProperties.addProperty(
				SectionClassWorkSource.CLASS_NAME_PROPERTY_NAME).setValue(
				sectionClassName);
		WorkType<?> workType = context.loadWorkType(
				SectionClassWorkSource.class.getName(), workProperties);

		// Add the work for the section class
		SectionWork work = designer.addSectionWork("WORK",
				SectionClassWorkSource.class.getName());
		work.addProperty(SectionClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				sectionClassName);

		// Load tasks
		Map<String, SectionTask> tasksByName = new HashMap<String, SectionTask>();
		Map<String, SectionTask> tasksByParameterType = new HashMap<String, SectionTask>();
		Map<String, Integer> parameterIndexes = new HashMap<String, Integer>();
		for (TaskType<?, ?, ?> taskType : workType.getTaskTypes()) {

			// Obtain the task name
			String taskName = taskType.getTaskName();

			// Obtain the method for the task
			SectionTaskFactory taskFactory = (SectionTaskFactory) taskType
					.getTaskFactory();
			Method method = taskFactory.getMethod();

			// Add the task
			SectionTask task = work.addSectionTask(taskName, taskName);
			tasksByName.put(taskName, task);

			// Obtain the parameter for the task
			int objectIndex = 1; // 1 as Section Object first
			String parameterType = null;
			Class<?>[] parameters = method.getParameterTypes();
			Annotation[][] parametersAnnotations = method
					.getParameterAnnotations();
			for (int i = 0; i < parameters.length; i++) {
				Class<?> parameter = parameters[i];
				Annotation[] parameterAnnotations = parametersAnnotations[i];

				// Determine if flow or section interface (not object)
				if ((parameter.getAnnotation(FlowInterface.class) != null)
						|| (parameter.getAnnotation(SectionInterface.class) != null)) {
					continue; // ignore flow and section interfaces
				}

				// Determine if the parameter
				boolean isParameter = false;
				for (Annotation annotation : parameterAnnotations) {
					if (Parameter.class.equals(annotation.annotationType())) {
						isParameter = true;
					}
				}

				// Register as parameter
				if (isParameter) {

					// Ensure only one parameter
					if (parameterType != null) {
						throw new IllegalStateException(
								"Method "
										+ taskName
										+ " may only have one parameter annotated with "
										+ Parameter.class.getSimpleName());
					}

					// Specify the parameter type
					parameterType = parameter.getName();

					// Register the task by its parameter type
					tasksByParameterType.put(parameterType, task);

					// Register the parameter index for the task
					parameterIndexes.put(taskName, new Integer(objectIndex));
				}

				// Increment object index for parameter
				objectIndex++;
			}

			// Add input for task
			SectionInput sectionInput = designer.addSectionInput(taskName,
					parameterType);
			designer.link(sectionInput, task);
		}

		// Link tasks
		Map<Class<?>, SubSection> subSections = new HashMap<Class<?>, SubSection>();
		Map<String, SectionOutput> sectionOutputs = new HashMap<String, SectionOutput>();
		for (TaskType<?, ?, ?> taskType : workType.getTaskTypes()) {

			// Obtain the task name
			String taskName = taskType.getTaskName();

			// Obtain the task
			SectionTask task = tasksByName.get(taskName);

			// Obtain the task method
			SectionTaskFactory taskFactory = (SectionTaskFactory) taskType
					.getTaskFactory();
			Method method = taskFactory.getMethod();

			// Obtain the argument type for the task
			Class<?> returnType = method.getReturnType();
			String argumentType = ((returnType == null) || (returnType == void.class)) ? null
					: returnType.getName();

			// Link potential next task
			NextTask nextTaskAnnotation = method.getAnnotation(NextTask.class);
			if (nextTaskAnnotation != null) {

				// Obtain the next task name
				String nextTaskName = nextTaskAnnotation.value();

				// Attempt to obtain next task internally
				SectionTask nextTask = tasksByName.get(nextTaskName);
				if (nextTask != null) {
					// Link task internally
					designer.link(task, nextTask);

				} else {
					// Not internal task, so link externally
					SectionOutput sectionOutput = sectionOutputs
							.get(nextTaskName);
					if (sectionOutput == null) {
						// Not yet added, so add section output
						sectionOutput = designer.addSectionOutput(nextTaskName,
								argumentType, false);
						sectionOutputs.put(nextTaskName, sectionOutput);
					}
					designer.link(task, sectionOutput);
				}
			}

			// Obtain the flow meta-data for the task
			List<FlowMethodMetaData> flowMetaDatas = new LinkedList<FlowMethodMetaData>();
			ParameterFactory[] parameterFactories = taskFactory
					.getParameterFactories();
			for (ParameterFactory factory : parameterFactories) {

				// Ignore if not flow parameter factory
				if (!(factory instanceof FlowParameterFactory)) {
					continue; // ignore as not flow parameter factory
				}
				FlowParameterFactory flowParameterFactory = (FlowParameterFactory) factory;

				// Add the flow meta-data
				flowMetaDatas.addAll(Arrays.asList(flowParameterFactory
						.getFlowMethodMetaData()));
			}

			// Sort the flows by index
			Collections.sort(flowMetaDatas,
					new Comparator<FlowMethodMetaData>() {
						@Override
						public int compare(FlowMethodMetaData a,
								FlowMethodMetaData b) {
							return a.getFlowIndex() - b.getFlowIndex();
						}
					});

			// Link flows for the task
			for (FlowMethodMetaData flowMetaData : flowMetaDatas) {

				// Obtain the flow name
				Method flowMethod = flowMetaData.getMethod();
				String flowName = flowMethod.getName();

				// Obtain the task flow
				TaskFlow taskFlow = task.getTaskFlow(flowName);

				// Determine if section interface (or flow interface)
				Class<?> parameter = flowMetaData.getFlowType();
				SectionInterface sectionAnnotation = parameter
						.getAnnotation(SectionInterface.class);
				if (sectionAnnotation != null) {
					// Section interface so link to sub section
					SubSection subSection = subSections.get(parameter);
					if (subSection == null) {
						// Sub section not registered, so create and register
						String subSectionSourceClassName = sectionAnnotation
								.source().getName();
						String subSectionLocation = (""
								.equals(sectionAnnotation.location())) ? sectionAnnotation
								.locationClass().getName()
								: sectionAnnotation.location();
						subSection = designer.addSubSection(parameter
								.getSimpleName(), subSectionSourceClassName,
								subSectionLocation);
						PropertyList subSectionProperties = context
								.createPropertyList();
						for (Property property : sectionAnnotation.properties()) {
							String name = property.name();
							String value = ("".equals(property.value())) ? property
									.valueClass().getName()
									: property.value();
							subSection.addProperty(name, value);
							subSectionProperties.addProperty(name).setValue(
									value);
						}

						// Register the sub section
						subSections.put(parameter, subSection);

						// Link outputs of sub section
						for (FlowLink flowLink : sectionAnnotation.outputs()) {

							// Obtain the sub section output
							String subSectionOuputName = flowLink.name();
							SubSectionOutput subSectionOuput = subSection
									.getSubSectionOutput(subSectionOuputName);

							// Obtain the section task for output
							String linkTaskName = flowLink.method();
							SectionTask linkTask = tasksByName
									.get(linkTaskName);
							if (linkTask != null) {
								// Link flow internally
								designer.link(subSectionOuput, linkTask);
							}
						}

						// Load the section type
						SectionType subSectionType = context.loadSectionType(
								subSectionSourceClassName, subSectionLocation,
								subSectionProperties);

						// Link objects of sub section
						for (SectionObjectType subSectionObjectType : subSectionType
								.getSectionObjectTypes()) {

							// Obtain the sub section output
							String objectName = subSectionObjectType
									.getSectionObjectName();
							SubSectionObject subSectionObject = subSection
									.getSubSectionObject(objectName);

							// Link to managed object or external object
							String objectTypeName = subSectionObjectType
									.getObjectType();
							SectionManagedObject sectionManagedObject = sectionManagedObjects
									.get(objectTypeName);
							if (sectionManagedObject != null) {
								// Link to section managed object
								designer.link(subSectionObject,
										sectionManagedObject);

							} else {
								// Link to external object
								SectionObject sectionObject = sectionObjects
										.get(objectTypeName);
								if (sectionObject == null) {
									// Not yet added, so add section object
									sectionObject = designer.addSectionObject(
											objectTypeName, objectTypeName);
									sectionObjects.put(objectTypeName,
											sectionObject);
								}
								designer.link(subSectionObject, sectionObject);
							}
						}
					}

					// Link flow to sub section input
					SubSectionInput subSectionInput = subSection
							.getSubSectionInput(flowName);
					designer.link(taskFlow, subSectionInput,
							FlowInstigationStrategyEnum.SEQUENTIAL);

				} else {
					// Flow interface so attempt to obtain the task internally
					SectionTask linkTask = tasksByName.get(flowName);
					if (linkTask != null) {
						// Link flow internally
						designer.link(taskFlow, linkTask,
								FlowInstigationStrategyEnum.SEQUENTIAL);

					} else {
						// Not internal task, so link externally
						SectionOutput sectionOutput = sectionOutputs
								.get(flowName);
						if (sectionOutput == null) {
							// Not yet added, so add section output
							sectionOutput = designer.addSectionOutput(flowName,
									argumentType, false);
							sectionOutputs.put(flowName, sectionOutput);
						}
						designer.link(taskFlow, sectionOutput,
								FlowInstigationStrategyEnum.SEQUENTIAL);
					}
				}
			}

			// Link escalations for the task
			for (TaskEscalationType escalationType : taskType
					.getEscalationTypes()) {

				// Obtain the escalation class
				Class<?> escalationClass = escalationType.getEscalationType();
				String escalationTypeName = escalationClass.getName();

				// Obtain the task escalation
				TaskFlow taskEscalation = task
						.getTaskEscalation(escalationTypeName);

				// Obtain task handling escalation
				SectionTask escalationHandler = tasksByParameterType
						.get(escalationTypeName);
				if (escalationHandler != null) {
					// Handle escalation internally
					designer.link(taskEscalation, escalationHandler,
							FlowInstigationStrategyEnum.SEQUENTIAL);

				} else {
					// Not internally handled, so link externally
					SectionOutput sectionOutput = sectionOutputs
							.get(escalationTypeName);
					if (sectionOutput == null) {
						// Not yet added, so add section output
						sectionOutput = designer.addSectionOutput(
								escalationTypeName, escalationTypeName, true);
						sectionOutputs.put(escalationTypeName, sectionOutput);
					}
					designer.link(taskEscalation, sectionOutput,
							FlowInstigationStrategyEnum.SEQUENTIAL);
				}
			}

			// Obtain the object index for the parameter
			Integer parameterIndex = parameterIndexes.get(taskName);

			// Obtain the object types
			TaskObjectType<?>[] objectTypes = taskType.getObjectTypes();

			// First object is always the section object
			TaskObjectType<?> sectionObjectType = objectTypes[0];
			TaskObject objectSection = task.getTaskObject(sectionObjectType
					.getObjectName());
			designer.link(objectSection, managedObject);

			// Link remaining objects for task (1 as after section object)
			for (int i = 1; i < objectTypes.length; i++) {
				TaskObjectType<?> objectType = objectTypes[i];

				// Obtain the object name and its type
				String objectName = objectType.getObjectName();
				String objectTypeName = objectType.getObjectType().getName();

				// Obtain the task object
				TaskObject taskObject = task.getTaskObject(objectName);

				// Determine if object is a parameter
				if ((parameterIndex != null)
						&& (parameterIndex.intValue() == i)) {
					// Object is the parameter
					taskObject.flagAsParameter();

				} else {
					// Link to external object (by type)
					SectionObject sectionObject = sectionObjects
							.get(objectTypeName);
					if (sectionObject == null) {
						// No yet added, so add section object
						sectionObject = designer.addSectionObject(
								objectTypeName, objectTypeName);
						sectionObjects.put(objectTypeName, sectionObject);
					}
					designer.link(taskObject, sectionObject);
				}
			}
		}
	}
}