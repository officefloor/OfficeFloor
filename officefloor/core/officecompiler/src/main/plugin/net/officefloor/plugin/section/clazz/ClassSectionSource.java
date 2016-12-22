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

import net.officefloor.compile.SectionSourceService;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
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
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.plugin.section.clazz.SectionClassWorkSource.SectionTaskFactory;
import net.officefloor.plugin.work.clazz.FlowInterface;
import net.officefloor.plugin.work.clazz.FlowMethodMetaData;
import net.officefloor.plugin.work.clazz.FlowParameterFactory;
import net.officefloor.plugin.work.clazz.ParameterFactory;
import net.officefloor.plugin.work.clazz.Qualifier;

/**
 * <p>
 * Class {@link SectionSource}.
 * <p>
 * The implementation has been segregated into smaller methods to allow
 * overriding to re-use {@link ClassSectionSource} for other uses.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSource extends AbstractSectionSource implements
		SectionSourceService<ClassSectionSource> {

	/**
	 * Name of the {@link SectionManagedObject} for the section class.
	 */
	public static final String CLASS_OBJECT_NAME = "OBJECT";

	/**
	 * Flag indicating if sourced.
	 */
	private boolean isSourced = false;

	/**
	 * {@link SectionDesigner}.
	 */
	private SectionDesigner _designer;

	/**
	 * Obtains the {@link SectionDesigner}.
	 * 
	 * @return {@link SectionDesigner};
	 */
	protected SectionDesigner getDesigner() {
		return this._designer;
	}

	/**
	 * {@link SectionSourceContext}.
	 */
	private SectionSourceContext _context;

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	protected SectionSourceContext getContext() {
		return this._context;
	}

	/**
	 * {@link SectionTask} instances by name.
	 */
	private final Map<String, SectionTask> _tasksByName = new HashMap<String, SectionTask>();

	/**
	 * Obtains the {@link SectionTask} by its name.
	 * 
	 * @param taskName
	 *            Name of the {@link SectionTask}.
	 * @return {@link SectionTask} or <code>null</code> if no
	 *         {@link SectionTask} by the name.
	 */
	public SectionTask getTaskByName(String taskName) {
		return this._tasksByName.get(taskName);
	}

	/**
	 * {@link SectionTask} instances by {@link ManagedFunctionType} name.
	 */
	private final Map<String, SectionTask> _tasksByTypeName = new HashMap<String, SectionTask>();

	/**
	 * Obtains the {@link SectionTask} by its {@link ManagedFunctionType} name.
	 * 
	 * @param taskTypeName
	 *            {@link ManagedFunctionType} name.
	 * @return {@link SectionTask} or <code>null</code> if no
	 *         {@link SectionTask} by the {@link ManagedFunctionType} name.
	 */
	public SectionTask getTaskByTypeName(String taskTypeName) {
		return this._tasksByTypeName.get(taskTypeName);
	}

	/**
	 * <p>
	 * Allows being made aware of further {@link SectionTask} instances within
	 * the section to be considered for linking flows.
	 * <p>
	 * This allows {@link ClassSectionSource} to be used in conjunction with
	 * other functionality - such as template rendering for dynamic HTTP web
	 * pages.
	 * 
	 * @param taskTypeName
	 *            Name to register the {@link SectionTask}.
	 * @param task
	 *            {@link SectionTask}.
	 */
	public void registerTaskByTypeName(String taskTypeName, SectionTask task) {
		this._tasksByTypeName.put(taskTypeName, task);
	}

	/**
	 * {@link SectionObject} instances by fully qualified type name.
	 */
	private final Map<String, SectionObject> _objectsByTypeName = new HashMap<String, SectionObject>();

	/**
	 * <p>
	 * Obtains the {@link SectionObject}.
	 * <p>
	 * Should the {@link SectionObject} not yet be added, it is added.
	 * 
	 * @param qualifier
	 *            {@link Qualifier} for the {@link SectionObject}. If not
	 *            {@link Qualifier} should be the same as the type name.
	 * @param typeName
	 *            Fully qualified type name of the {@link SectionObject}.
	 * @return {@link SectionObject}.
	 */
	public SectionObject getOrCreateObject(String qualifier, String typeName) {

		// Determine the object name
		boolean isQualifier = !CompileUtil.isBlank(qualifier);
		String objectName = (isQualifier ? qualifier + "-" : "") + typeName;

		// Obtain or create the Section Object
		SectionObject sectionObject = this._objectsByTypeName.get(objectName);
		if (sectionObject == null) {
			// No yet added, so add section object
			sectionObject = this.getDesigner().addSectionObject(objectName,
					typeName);
			this._objectsByTypeName.put(objectName, sectionObject);

			// Provide type qualifier (if specified)
			if (isQualifier) {
				sectionObject.setTypeQualifier(qualifier);
			}
		}
		return sectionObject;
	}

	/**
	 * {@link SectionManagedObject} instances by fully qualified type name.
	 */
	private final Map<String, SectionManagedObject> _managedObjectsByTypeName = new HashMap<String, SectionManagedObject>();

	/**
	 * Obtains the {@link SectionManagedObject} for the type.
	 * 
	 * @param typeName
	 *            Fully qualified type name of the object for the
	 *            {@link SectionManagedObject}.
	 * @return {@link SectionManagedObject} or <code>null</code> if no
	 *         {@link SectionManagedObject} for the type.
	 */
	public SectionManagedObject getManagedObject(String typeName) {
		return this._managedObjectsByTypeName.get(typeName);
	}

	/**
	 * {@link SectionOutput} instances by name.
	 */
	private final Map<String, SectionOutput> _outputsByName = new HashMap<String, SectionOutput>();

	/**
	 * <p>
	 * Obtains the {@link SectionOutput}.
	 * <p>
	 * Should the {@link SectionOutput} not yet be added, it is added.
	 * 
	 * @param name
	 *            Name of the {@link SectionOutput}.
	 * @param argumentType
	 *            Type of the argument. May be <code>null</code> if no argument.
	 * @param isEscalationOnly
	 *            <code>true</code> if escalation only.
	 * @return {@link SectionObject}.
	 */
	public SectionOutput getOrCreateOutput(String name, String argumentType,
			boolean isEscalationOnly) {
		SectionOutput sectionOutput = this._outputsByName.get(name);
		if (sectionOutput == null) {
			// Not yet added, so add section output
			sectionOutput = this.getDesigner().addSectionOutput(name,
					argumentType, isEscalationOnly);
			this._outputsByName.put(name, sectionOutput);
		}
		return sectionOutput;
	}

	/**
	 * {@link SubSection} instances by the {@link SectionInterface} annotated
	 * type.
	 */
	private final Map<Class<?>, SubSection> _subSectionsByType = new HashMap<Class<?>, SubSection>();

	/**
	 * <p>
	 * Obtains the {@link SubSection}.
	 * <p>
	 * Should the {@link SubSection} not already be created, it is created.
	 * 
	 * @param sectionInterfaceType
	 *            Type that is annotated with {@link SectionInterface}.
	 * @param sectionAnnotation
	 *            {@link SectionInterface} annotation.
	 * @return {@link SubSection}.
	 */
	public SubSection getOrCreateSubSection(Class<?> sectionInterfaceType,
			SectionInterface sectionAnnotation) {

		// Determine if sub section already created for type
		SubSection subSection = this._subSectionsByType
				.get(sectionInterfaceType);
		if (subSection != null) {
			return subSection;
		}

		// Sub section not registered, so create and register
		String subSectionSourceClassName = sectionAnnotation.source().getName();
		String subSectionLocation = ("".equals(sectionAnnotation.location())) ? sectionAnnotation
				.locationClass().getName() : sectionAnnotation.location();
		subSection = this.getDesigner().addSubSection(
				sectionInterfaceType.getSimpleName(),
				subSectionSourceClassName, subSectionLocation);
		PropertyList subSectionProperties = this.getContext()
				.createPropertyList();
		for (Property property : sectionAnnotation.properties()) {
			String name = property.name();
			String value = ("".equals(property.value())) ? property
					.valueClass().getName() : property.value();
			subSection.addProperty(name, value);
			subSectionProperties.addProperty(name).setValue(value);
		}

		// Register the sub section
		this._subSectionsByType.put(sectionInterfaceType, subSection);

		// Link outputs of sub section
		for (FlowLink flowLink : sectionAnnotation.outputs()) {

			// Obtain the sub section output
			String subSectionOuputName = flowLink.name();
			SubSectionOutput subSectionOuput = subSection
					.getSubSectionOutput(subSectionOuputName);

			// Obtain the section task for output
			String linkTaskName = flowLink.method();
			SectionTask linkTask = this.getTaskByTypeName(linkTaskName);
			if (linkTask != null) {
				// Link flow internally
				this.getDesigner().link(subSectionOuput, linkTask);
			}
		}

		// Load the section type
		SectionType subSectionType = this.getContext().loadSectionType(
				subSectionSourceClassName, subSectionLocation,
				subSectionProperties);

		// Link objects of sub section
		for (SectionObjectType subSectionObjectType : subSectionType
				.getSectionObjectTypes()) {

			// Obtain the sub section object
			String objectName = subSectionObjectType.getSectionObjectName();
			SubSectionObject subSectionObject = subSection
					.getSubSectionObject(objectName);

			// Link to managed object or external object
			String objectTypeName = subSectionObjectType.getObjectType();
			SectionManagedObject sectionManagedObject = this
					.getManagedObject(objectTypeName);
			if (sectionManagedObject != null) {
				// Link to section managed object
				this.getDesigner().link(subSectionObject, sectionManagedObject);

			} else {
				// Link to external object
				SectionObject sectionObject = this.getOrCreateObject(null,
						objectTypeName);
				this.getDesigner().link(subSectionObject, sectionObject);
			}
		}

		// Return the sub section
		return subSection;
	}

	/**
	 * Obtains the name of the class for the section.
	 * 
	 * @return Class name for the backing class of the section.
	 */
	protected String getSectionClassName() {
		String sectionClassName = this.getContext().getSectionLocation();
		return sectionClassName;
	}

	/**
	 * Obtains the section class.
	 * 
	 * @param sectionClassName
	 *            Name of the section class.
	 * @return Section class.
	 * @throws Exception
	 *             If fails to obtain the section class.
	 */
	protected Class<?> getSectionClass(String sectionClassName)
			throws Exception {
		return this.getContext().getClassLoader().loadClass(sectionClassName);
	}

	/**
	 * Creates the {@link SectionManagedObject} for providing the section
	 * object.
	 * 
	 * @param objectName
	 *            Name of the object within the section.
	 * @param sectionClass
	 *            Section object class.
	 * @return {@link SectionManagedObject}.
	 */
	protected SectionManagedObject createClassManagedObject(String objectName,
			Class<?> sectionClass) {

		// Create the managed object source
		SectionManagedObjectSource managedObjectSource = this.getDesigner()
				.addSectionManagedObjectSource(objectName,
						SectionClassManagedObjectSource.class.getName());
		managedObjectSource.addProperty(
				SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				sectionClass.getName());

		// Create the managed object
		SectionManagedObject managedObject = managedObjectSource
				.addSectionManagedObject(objectName, ManagedObjectScope.THREAD);
		return managedObject;
	}

	/**
	 * Extracts the {@link DependencyMetaData} instances for the section object.
	 * 
	 * @param objectName
	 *            Name of the object within the section.
	 * @param sectionClass
	 *            Section object class.
	 * @return Extracted {@link DependencyMetaData} instances for the section
	 *         object.
	 * @throws Exception
	 *             If fails to extract the {@link DependencyMetaData} instances.
	 */
	protected DependencyMetaData[] extractClassManagedObjectDependencies(
			String objectName, Class<?> sectionClass) throws Exception {
		return new SectionClassManagedObjectSource()
				.extractDependencyMetaData(sectionClass);
	}

	/**
	 * Obtains the {@link ManagedFunction} name from the {@link ManagedFunctionType}.
	 * 
	 * @param taskType
	 *            {@link ManagedFunctionType}.
	 * @return {@link ManagedFunction} name.
	 */
	protected String getTaskName(ManagedFunctionType<?, ?, ?> taskType) {
		return taskType.getFunctionName();
	}

	/**
	 * Enriches the {@link ManagedFunction}.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param taskType
	 *            {@link ManagedFunctionType} for the {@link SectionTask}.
	 * @param taskMethod
	 *            {@link Method} for the {@link SectionTask}.
	 * @param parameterType
	 *            Parameter type for the {@link SectionTask}. May be
	 *            <code>null</code> if no parameter.
	 */
	protected void enrichTask(SectionTask task, ManagedFunctionType<?, ?, ?> taskType,
			Method taskMethod, Class<?> parameterType) {

		// Obtain the task name
		String taskName = task.getSectionTaskName();

		// Obtain the parameter type name
		String parameterTypeName = (parameterType == null ? null
				: parameterType.getName());

		// Add input for task
		SectionInput sectionInput = this.getDesigner().addSectionInput(
				taskName, parameterTypeName);
		this.getDesigner().link(sectionInput, task);
	}

	/**
	 * Links the next {@link ManagedFunction}.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param taskType
	 *            {@link ManagedFunctionType}.
	 * @param taskMethod
	 *            {@link Method} for the {@link SectionTask}.
	 * @param argumentType
	 *            Argument type. May be <code>null</code> if no argument type.
	 * @param nextTaskAnnotation
	 *            {@link NextTask} annotation on the {@link Method}.
	 */
	protected void linkNextTask(SectionTask task, ManagedFunctionType<?, ?, ?> taskType,
			Method taskMethod, Class<?> argumentType,
			NextTask nextTaskAnnotation) {

		// Obtain the next task name
		String nextTaskName = nextTaskAnnotation.value();

		// Obtain the argument type name for the task
		String argumentTypeName = (argumentType == null ? null : argumentType
				.getName());

		// Attempt to obtain next task internally
		SectionTask nextTask = this.getTaskByTypeName(nextTaskName);
		if (nextTask != null) {
			// Link task internally
			this.getDesigner().link(task, nextTask);

		} else {
			// Not internal task, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(nextTaskName,
					argumentTypeName, false);
			this.getDesigner().link(task, sectionOutput);
		}
	}

	/**
	 * Links the {@link TaskFlow}.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param taskType
	 *            {@link ManagedFunctionType}.
	 * @param flowInterfaceType
	 *            Interface type specifying the flows.
	 * @param flowMethod
	 *            Method on the interface for the flow to be linked.
	 * @param flowArgumentType
	 *            {@link TaskFlow} argument type. May be <code>null</code> if no
	 *            argument.
	 */
	protected void linkTaskFlow(SectionTask task, ManagedFunctionType<?, ?, ?> taskType,
			Class<?> flowInterfaceType, Method flowMethod,
			Class<?> flowArgumentType) {

		// Obtain the flow name
		String flowName = flowMethod.getName();

		// Obtain the task flow
		TaskFlow taskFlow = task.getTaskFlow(flowName);

		// Determine if section interface (or flow interface)
		SectionInterface sectionAnnotation = flowInterfaceType
				.getAnnotation(SectionInterface.class);
		if (sectionAnnotation != null) {
			// Section interface so obtain the sub section
			SubSection subSection = this.getOrCreateSubSection(
					flowInterfaceType, sectionAnnotation);

			// Link flow to sub section input
			SubSectionInput subSectionInput = subSection
					.getSubSectionInput(flowName);
			this.getDesigner().link(taskFlow, subSectionInput,
					FlowInstigationStrategyEnum.SEQUENTIAL);

		} else {
			// Link the task flow
			this.linkTaskFlow(taskFlow, taskType, flowInterfaceType,
					flowMethod, flowArgumentType);
		}
	}

	/**
	 * Links the {@link TaskFlow}.
	 * 
	 * @param taskFlow
	 *            {@link TaskFlow}.
	 * @param taskType
	 *            {@link ManagedFunctionType}.
	 * @param flowInterfaceType
	 *            Interface type specifying the flows.
	 * @param flowMethod
	 *            Method on the interface for the flow to be linked.
	 * @param flowArgumentType
	 *            {@link TaskFlow} argument type. May be <code>null</code> if no
	 *            argument.
	 */
	protected void linkTaskFlow(TaskFlow taskFlow, ManagedFunctionType<?, ?, ?> taskType,
			Class<?> flowInterfaceType, Method flowMethod,
			Class<?> flowArgumentType) {

		// Obtain the flow name
		String flowName = taskFlow.getTaskFlowName();

		// Obtain the flow argument name
		String flowArgumentTypeName = (flowArgumentType == null ? null
				: flowArgumentType.getName());

		// Flow interface so attempt to obtain the task internally
		SectionTask linkTask = this.getTaskByTypeName(flowName);
		if (linkTask != null) {
			// Link flow internally
			this.getDesigner().link(taskFlow, linkTask,
					FlowInstigationStrategyEnum.SEQUENTIAL);

		} else {
			// Not internal task, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(flowName,
					flowArgumentTypeName, false);
			this.getDesigner().link(taskFlow, sectionOutput,
					FlowInstigationStrategyEnum.SEQUENTIAL);
		}
	}

	/**
	 * Links the {@link ManagedFunction} escalation.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param taskType
	 *            {@link ManagedFunctionType}.
	 * @param escalationType
	 *            {@link ManagedFunctionEscalationType}.
	 * @param escalationHandler
	 *            Potential {@link SectionTask} that can handle escalation based
	 *            on its parameter. May be <code>null</code> if no
	 *            {@link SectionTask} can handle the escalation.
	 */
	protected void linkTaskEscalation(SectionTask task,
			ManagedFunctionType<?, ?, ?> taskType, ManagedFunctionEscalationType escalationType,
			SectionTask escalationHandler) {

		// Obtain the escalation type name
		String escalationTypeName = escalationType.getEscalationType()
				.getName();

		// Obtain the task escalation
		TaskFlow taskEscalation = task.getTaskEscalation(escalationTypeName);

		// Link to escalation handler (if available)
		// (Do not allow handling own escalation as potential for infinite loop)
		if ((escalationHandler != null) && (task != escalationHandler)) {
			// Handle escalation internally
			this.getDesigner().link(taskEscalation, escalationHandler,
					FlowInstigationStrategyEnum.SEQUENTIAL);

		} else {
			// Not internally handled, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(
					escalationTypeName, escalationTypeName, true);
			this.getDesigner().link(taskEscalation, sectionOutput,
					FlowInstigationStrategyEnum.SEQUENTIAL);
		}
	}

	/**
	 * Links the {@link TaskObject}.
	 * 
	 * @param task
	 *            {@link SectionTask}.
	 * @param taskType
	 *            {@link ManagedFunctionType}.
	 * @param objectType
	 *            {@link ManagedFunctionObjectType}.
	 */
	protected void linkTaskObject(SectionTask task, ManagedFunctionType<?, ?, ?> taskType,
			ManagedFunctionObjectType<?> objectType) {

		// Obtain the object name and its type
		String objectName = objectType.getObjectName();
		String objectTypeName = objectType.getObjectType().getName();
		String typeQualifier = objectType.getTypeQualifier();

		// Obtain the task object
		TaskObject taskObject = task.getTaskObject(objectName);

		// Attempt to link to managed object
		SectionManagedObject mo = this.getManagedObject(objectTypeName);
		if (mo != null) {
			// Link to managed object
			this.getDesigner().link(taskObject, mo);

		} else {
			// Link to external object (by type)
			SectionObject sectionObject = this.getOrCreateObject(typeQualifier,
					objectTypeName);
			this.getDesigner().link(taskObject, sectionObject);
		}
	}

	/*
	 * ================ SectionSourceService ========================
	 */

	@Override
	public String getSectionSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassSectionSource> getSectionSourceClass() {
		return ClassSectionSource.class;
	}

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

		// Ensure only use once
		if (this.isSourced) {
			throw new IllegalStateException("May only use "
					+ this.getClass().getName() + " once per instance");
		}
		this.isSourced = true;

		// Initiate state
		this._designer = designer;
		this._context = context;

		// Obtain the class name
		String sectionClassName = this.getSectionClassName();
		if ((sectionClassName == null)
				|| (sectionClassName.trim().length() == 0)) {
			designer.addIssue("Must specify section class name within the location");
			return; // not able to load if no section class specified
		}

		// Obtain the class
		Class<?> sectionClass = this.getSectionClass(sectionClassName);

		// Add the managed object for the section class
		SectionManagedObject managedObject = this.createClassManagedObject(
				CLASS_OBJECT_NAME, sectionClass);

		// Obtain the dependency meta-data
		DependencyMetaData[] dependencyMetaData = this
				.extractClassManagedObjectDependencies(CLASS_OBJECT_NAME,
						sectionClass);

		// Load the managed objects
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
				// Use name of field to add the managed object
				String moName = dependency.name;

				// Add the managed object
				SectionManagedObjectSource mos = designer
						.addSectionManagedObjectSource(moName, moAnnotation
								.source().getName());
				for (Property property : moAnnotation.properties()) {
					String value = ("".equals(property.value()) ? property
							.valueClass().getName() : property.value());
					mos.addProperty(property.name(), value);
				}
				SectionManagedObject mo = mos.addSectionManagedObject(moName,
						ManagedObjectScope.PROCESS);

				// Add the type qualifiers for managed object
				for (TypeQualifier typeQualifier : moAnnotation.qualifiers()) {
					Class<?> qualifierClass = typeQualifier.qualifier();
					if (TypeQualifier.class.equals(qualifierClass)) {
						// No qualifier (as default value)
						qualifierClass = null;
					}
					String qualifier = (qualifierClass == null ? null
							: qualifierClass.getName());
					String type = typeQualifier.type().getName();
					mo.addTypeQualification(qualifier, type);
				}

				// Register the managed object
				this._managedObjectsByTypeName.put(dependencyTypeName, mo);

				// Link dependency to managed object
				designer.link(moDependency, mo);

			} else {
				// Obtain the type qualifier for external object
				String dependencyTypeQualifier;
				try {
					dependencyTypeQualifier = dependency.getTypeQualifier();
				} catch (IllegalArgumentException ex) {
					designer.addIssue(
							"Unable to obtain type qualifier for dependency "
									+ dependencyName, ex);
					return; // invalid section
				}

				// Link to external object (by qualified type)
				SectionObject sectionObject = this.getOrCreateObject(
						dependencyTypeQualifier, dependencyTypeName);
				designer.link(moDependency, sectionObject);
			}
		}

		// Link the managed object dependencies
		for (DependencyMetaData dependency : dependencyMetaData) {

			// Obtain the managed object annotation
			ManagedObject annotation = dependency.field
					.getAnnotation(ManagedObject.class);
			if (annotation == null) {
				continue; // not managed object dependency
			}

			// Obtain the managed object
			SectionManagedObject mo = this.getManagedObject(dependency.field
					.getType().getName());

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

				// Obtain the dependency type information
				String dependencyTypeName = dependencyType.getDependencyType()
						.getName();
				String dependencyTypeQualifier = dependencyType
						.getTypeQualifier();

				// Obtain the managed object dependency
				ManagedObjectDependency moDependency = mo
						.getManagedObjectDependency(dependencyType
								.getDependencyName());

				// First attempt to link internally
				SectionManagedObject dependencyMo = this
						.getManagedObject(dependencyTypeName);
				if (dependencyMo != null) {
					// Link to managed object
					designer.link(moDependency, dependencyMo);

				} else {
					// Link to external object (by type)
					SectionObject sectionObject = this.getOrCreateObject(
							dependencyTypeQualifier, dependencyTypeName);
					designer.link(moDependency, sectionObject);
				}
			}
		}

		// Ensure the section class has tasks
		boolean hasTaskMethod = false;
		for (Method method : sectionClass.getMethods()) {
			if (!(method.getDeclaringClass().equals(Object.class))) {
				hasTaskMethod = true; // declared a class
			}
		}
		if (!hasTaskMethod) {
			designer.addIssue("Must have at least one public method on template logic class "
					+ sectionClassName);
		}

		// Load the work type for the class
		PropertyList workProperties = context.createPropertyList();
		workProperties.addProperty(
				SectionClassWorkSource.CLASS_NAME_PROPERTY_NAME).setValue(
				sectionClassName);
		FunctionNamespaceType<?> workType = context.loadWorkType(
				SectionClassWorkSource.class.getName(), workProperties);

		// Add the work for the section class
		SectionWork work = designer.addSectionWork("WORK",
				SectionClassWorkSource.class.getName());
		work.addProperty(SectionClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				sectionClassName);

		// Load tasks
		Map<String, SectionTask> tasksByParameterType = new HashMap<String, SectionTask>();
		Map<String, Integer> parameterIndexes = new HashMap<String, Integer>();
		for (ManagedFunctionType<?, ?, ?> taskType : workType.getManagedFunctionTypes()) {

			// Obtain the task name
			String taskTypeName = taskType.getFunctionName();
			String taskName = this.getTaskName(taskType);

			// Obtain the method for the task
			SectionTaskFactory taskFactory = (SectionTaskFactory) taskType
					.getManagedFunctionFactory();
			Method method = taskFactory.getMethod();

			// Add the task (both by name and type name for internal linking)
			SectionTask task = work.addSectionTask(taskName, taskTypeName);
			this._tasksByName.put(taskName, task);
			this.registerTaskByTypeName(taskTypeName, task);

			// Obtain the parameter for the task
			int objectIndex = 1; // 1 as Section Object first
			Class<?> parameterType = null;
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
					parameterType = parameter;

					// Register the task by its parameter type
					tasksByParameterType.put(parameterType.getName(), task);

					// Register the parameter index for the task
					parameterIndexes
							.put(taskTypeName, new Integer(objectIndex));
				}

				// Increment object index for parameter
				objectIndex++;
			}

			// Enrich the task
			this.enrichTask(task, taskType, method, parameterType);
		}

		// Link tasks
		for (ManagedFunctionType<?, ?, ?> taskType : workType.getManagedFunctionTypes()) {

			// Obtain the task name
			String taskName = taskType.getFunctionName();

			// Obtain the task
			SectionTask task = this.getTaskByTypeName(taskName);

			// Obtain the task method
			SectionTaskFactory taskFactory = (SectionTaskFactory) taskType
					.getManagedFunctionFactory();
			Method method = taskFactory.getMethod();

			// Link the next task
			NextTask nextTaskAnnotation = method.getAnnotation(NextTask.class);
			if (nextTaskAnnotation != null) {

				// Obtain the argument type for the task
				Class<?> returnType = method.getReturnType();
				Class<?> argumentType = ((returnType == null)
						|| (void.class.equals(returnType)) || (Void.TYPE
						.equals(returnType))) ? null : returnType;

				// Link next task
				this.linkNextTask(task, taskType, method, argumentType,
						nextTaskAnnotation);
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

				// Obtain the flow interface type
				Class<?> flowInterfaceType = flowMetaData.getFlowType();

				// Obtain the flow method
				Method flowMethod = flowMetaData.getMethod();

				// Obtain the argument type for the flow
				Class<?> flowArgumentType = null;
				Class<?>[] flowParameters = flowMethod.getParameterTypes();
				if (flowParameters.length > 0) {
					// Argument is always the first (and only) parameter
					flowArgumentType = flowParameters[0];
				}

				// Link the task flow
				this.linkTaskFlow(task, taskType, flowInterfaceType,
						flowMethod, flowArgumentType);
			}

			// Link escalations for the task
			for (ManagedFunctionEscalationType escalationType : taskType
					.getEscalationTypes()) {

				// Obtain task handling escalation (if available)
				String escalationTypeName = escalationType.getEscalationType()
						.getName();
				SectionTask escalationHandler = tasksByParameterType
						.get(escalationTypeName);

				// Link escalation
				this.linkTaskEscalation(task, taskType, escalationType,
						escalationHandler);
			}

			// Obtain the object index for the parameter
			Integer parameterIndex = parameterIndexes.get(taskName);

			// Obtain the object types
			ManagedFunctionObjectType<?>[] objectTypes = taskType.getObjectTypes();

			// First object is always the section object
			ManagedFunctionObjectType<?> sectionObjectType = objectTypes[0];
			TaskObject objectSection = task.getTaskObject(sectionObjectType
					.getObjectName());
			designer.link(objectSection, managedObject);

			// Link remaining objects for task (1 as after section object)
			for (int i = 1; i < objectTypes.length; i++) {
				ManagedFunctionObjectType<?> objectType = objectTypes[i];

				// Determine if object is a parameter
				if ((parameterIndex != null)
						&& (parameterIndex.intValue() == i)) {
					// Parameter so flag as parameter
					String objectName = objectType.getObjectName();
					TaskObject taskObject = task.getTaskObject(objectName);
					taskObject.flagAsParameter();
					continue; // next object
				}

				// Link the task object
				this.linkTaskObject(task, taskType, objectType);
			}
		}
	}

}