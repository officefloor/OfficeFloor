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
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.FlowMethodMetaData;
import net.officefloor.plugin.managedfunction.clazz.FlowParameterFactory;
import net.officefloor.plugin.managedfunction.clazz.ParameterFactory;
import net.officefloor.plugin.managedfunction.clazz.Qualifier;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.plugin.section.clazz.SectionClassManagedFunctionSource.SectionManagedFunctionFactory;

/**
 * <p>
 * Class {@link SectionSource}.
 * <p>
 * The implementation has been segregated into smaller methods to allow
 * overriding to re-use {@link ClassSectionSource} for other uses.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSource extends AbstractSectionSource implements SectionSourceService<ClassSectionSource> {

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
	 * {@link SectionFunction} instances by name.
	 */
	private final Map<String, SectionFunction> _functionsByName = new HashMap<String, SectionFunction>();

	/**
	 * Obtains the {@link SectionFunction} by its name.
	 * 
	 * @param functionName
	 *            Name of the {@link SectionFunction}.
	 * @return {@link SectionFunction} or <code>null</code> if no
	 *         {@link SectionFunction} by the name.
	 */
	public SectionFunction getFunctionByName(String functionName) {
		return this._functionsByName.get(functionName);
	}

	/**
	 * {@link SectionFunction} instances by {@link ManagedFunctionType} name.
	 */
	private final Map<String, SectionFunction> _functionsByTypeName = new HashMap<String, SectionFunction>();

	/**
	 * Obtains the {@link SectionFunction} by its {@link ManagedFunctionType}
	 * name.
	 * 
	 * @param functionTypeName
	 *            {@link ManagedFunctionType} name.
	 * @return {@link SectionFunction} or <code>null</code> if no
	 *         {@link SectionFunction} by the {@link ManagedFunctionType} name.
	 */
	public SectionFunction getFunctionByTypeName(String functionTypeName) {
		return this._functionsByTypeName.get(functionTypeName);
	}

	/**
	 * <p>
	 * Allows being made aware of further {@link SectionFunction} instances
	 * within the section to be considered for linking flows.
	 * <p>
	 * This allows {@link ClassSectionSource} to be used in conjunction with
	 * other functionality - such as template rendering for dynamic HTTP web
	 * pages.
	 * 
	 * @param functionTypeName
	 *            Name to register the {@link SectionFunction}.
	 * @param function
	 *            {@link SectionFunction}.
	 */
	public void registerFunctionByTypeName(String functionTypeName, SectionFunction function) {
		this._functionsByTypeName.put(functionTypeName, function);
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
			sectionObject = this.getDesigner().addSectionObject(objectName, typeName);
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
	public SectionOutput getOrCreateOutput(String name, String argumentType, boolean isEscalationOnly) {
		SectionOutput sectionOutput = this._outputsByName.get(name);
		if (sectionOutput == null) {
			// Not yet added, so add section output
			sectionOutput = this.getDesigner().addSectionOutput(name, argumentType, isEscalationOnly);
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
	public SubSection getOrCreateSubSection(Class<?> sectionInterfaceType, SectionInterface sectionAnnotation) {

		// Determine if sub section already created for type
		SubSection subSection = this._subSectionsByType.get(sectionInterfaceType);
		if (subSection != null) {
			return subSection;
		}

		// Sub section not registered, so create and register
		String subSectionSourceClassName = sectionAnnotation.source().getName();
		String subSectionLocation = ("".equals(sectionAnnotation.location()))
				? sectionAnnotation.locationClass().getName() : sectionAnnotation.location();
		subSection = this.getDesigner().addSubSection(sectionInterfaceType.getSimpleName(), subSectionSourceClassName,
				subSectionLocation);
		PropertyList subSectionProperties = this.getContext().createPropertyList();
		for (Property property : sectionAnnotation.properties()) {
			String name = property.name();
			String value = ("".equals(property.value())) ? property.valueClass().getName() : property.value();
			subSection.addProperty(name, value);
			subSectionProperties.addProperty(name).setValue(value);
		}

		// Register the sub section
		this._subSectionsByType.put(sectionInterfaceType, subSection);

		// Link outputs of sub section
		for (FlowLink flowLink : sectionAnnotation.outputs()) {

			// Obtain the sub section output
			String subSectionOuputName = flowLink.name();
			SubSectionOutput subSectionOuput = subSection.getSubSectionOutput(subSectionOuputName);

			// Obtain the section function for output
			String linkFunctionName = flowLink.method();
			SectionFunction linkFunction = this.getFunctionByTypeName(linkFunctionName);
			if (linkFunction != null) {
				// Link flow internally
				this.getDesigner().link(subSectionOuput, linkFunction);
			}
		}

		// Load the section type
		SectionType subSectionType = this.getContext().loadSectionType(subSectionSourceClassName, subSectionLocation,
				subSectionProperties);

		// Link objects of sub section
		for (SectionObjectType subSectionObjectType : subSectionType.getSectionObjectTypes()) {

			// Obtain the sub section object
			String objectName = subSectionObjectType.getSectionObjectName();
			SubSectionObject subSectionObject = subSection.getSubSectionObject(objectName);

			// Link to managed object or external object
			String objectTypeName = subSectionObjectType.getObjectType();
			SectionManagedObject sectionManagedObject = this.getManagedObject(objectTypeName);
			if (sectionManagedObject != null) {
				// Link to section managed object
				this.getDesigner().link(subSectionObject, sectionManagedObject);

			} else {
				// Link to external object
				SectionObject sectionObject = this.getOrCreateObject(null, objectTypeName);
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
	protected Class<?> getSectionClass(String sectionClassName) throws Exception {
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
	protected SectionManagedObject createClassManagedObject(String objectName, Class<?> sectionClass) {

		// Create the managed object source
		SectionManagedObjectSource managedObjectSource = this.getDesigner().addSectionManagedObjectSource(objectName,
				SectionClassManagedObjectSource.class.getName());
		managedObjectSource.addProperty(SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				sectionClass.getName());

		// Create the managed object
		SectionManagedObject managedObject = managedObjectSource.addSectionManagedObject(objectName,
				ManagedObjectScope.THREAD);
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
	protected DependencyMetaData[] extractClassManagedObjectDependencies(String objectName, Class<?> sectionClass)
			throws Exception {
		return new SectionClassManagedObjectSource().extractDependencyMetaData(sectionClass);
	}

	/**
	 * Obtains the {@link ManagedFunction} name from the
	 * {@link ManagedFunctionType}.
	 * 
	 * @param functionType
	 *            {@link ManagedFunctionType}.
	 * @return {@link ManagedFunction} name.
	 */
	protected String getFunctionName(ManagedFunctionType<?, ?> functionType) {
		return functionType.getFunctionName();
	}

	/**
	 * Enriches the {@link ManagedFunction}.
	 * 
	 * @param function
	 *            {@link SectionFunction}.
	 * @param functionType
	 *            {@link ManagedFunctionType} for the {@link SectionFunction}.
	 * @param functionMethod
	 *            {@link Method} for the {@link SectionFunction}.
	 * @param parameterType
	 *            Parameter type for the {@link SectionFunction}. May be
	 *            <code>null</code> if no parameter.
	 */
	protected void enrichFunction(SectionFunction function, ManagedFunctionType<?, ?> functionType,
			Method functionMethod, Class<?> parameterType) {

		// Obtain the input name
		String inputName = functionMethod.getName();

		// Obtain the parameter type name
		String parameterTypeName = (parameterType == null ? null : parameterType.getName());

		// Add input for function
		SectionInput sectionInput = this.getDesigner().addSectionInput(inputName, parameterTypeName);
		this.getDesigner().link(sectionInput, function);
	}

	/**
	 * Links the next {@link ManagedFunction}.
	 * 
	 * @param function
	 *            {@link SectionFunction}.
	 * @param functionType
	 *            {@link ManagedFunctionType}.
	 * @param functionMethod
	 *            {@link Method} for the {@link SectionFunction}.
	 * @param argumentType
	 *            Argument type. May be <code>null</code> if no argument type.
	 * @param nextFunctionAnnotation
	 *            {@link NextFunction} annotation on the {@link Method}.
	 */
	protected void linkNextFunction(SectionFunction function, ManagedFunctionType<?, ?> functionType,
			Method functionMethod, Class<?> argumentType, NextFunction nextFunctionAnnotation) {

		// Obtain the next function name
		String nextFunctionName = nextFunctionAnnotation.value();

		// Obtain the argument type name for the function
		String argumentTypeName = (argumentType == null ? null : argumentType.getName());

		// Attempt to obtain next function internally
		SectionFunction nextFunction = this.getFunctionByTypeName(nextFunctionName);
		if (nextFunction != null) {
			// Link function internally
			this.getDesigner().link(function, nextFunction);

		} else {
			// Not internal function, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(nextFunctionName, argumentTypeName, false);
			this.getDesigner().link(function, sectionOutput);
		}
	}

	/**
	 * Links the {@link FunctionFlow}.
	 * 
	 * @param function
	 *            {@link SectionFunction}.
	 * @param functionType
	 *            {@link ManagedFunctionType}.
	 * @param flowInterfaceType
	 *            Interface type specifying the flows.
	 * @param flowMethod
	 *            Method on the interface for the flow to be linked.
	 * @param flowArgumentType
	 *            {@link FunctionFlow} argument type. May be <code>null</code>
	 *            if no argument.
	 */
	protected void linkFunctionFlow(SectionFunction function, ManagedFunctionType<?, ?> functionType,
			Class<?> flowInterfaceType, Method flowMethod, Class<?> flowArgumentType) {

		// Obtain the flow name
		String flowName = flowMethod.getName();

		// Obtain the function flow
		FunctionFlow functionFlow = function.getFunctionFlow(flowName);

		// Determine if section interface (or flow interface)
		SectionInterface sectionAnnotation = flowInterfaceType.getAnnotation(SectionInterface.class);
		if (sectionAnnotation != null) {
			// Section interface so obtain the sub section
			SubSection subSection = this.getOrCreateSubSection(flowInterfaceType, sectionAnnotation);

			// Link flow to sub section input
			SubSectionInput subSectionInput = subSection.getSubSectionInput(flowName);
			this.getDesigner().link(functionFlow, subSectionInput, false);

		} else {
			// Link the function flow
			this.linkFunctionFlow(functionFlow, functionType, flowInterfaceType, flowMethod, flowArgumentType);
		}
	}

	/**
	 * Links the {@link FunctionFlow}.
	 * 
	 * @param functionFlow
	 *            {@link FunctionFlow}.
	 * @param functionType
	 *            {@link ManagedFunctionType}.
	 * @param flowInterfaceType
	 *            Interface type specifying the flows.
	 * @param flowMethod
	 *            Method on the interface for the flow to be linked.
	 * @param flowArgumentType
	 *            {@link FunctionFlow} argument type. May be <code>null</code>
	 *            if no argument.
	 */
	protected void linkFunctionFlow(FunctionFlow functionFlow, ManagedFunctionType<?, ?> functionType,
			Class<?> flowInterfaceType, Method flowMethod, Class<?> flowArgumentType) {

		// Obtain the flow name
		String flowName = functionFlow.getFunctionFlowName();

		// Obtain the flow argument name
		String flowArgumentTypeName = (flowArgumentType == null ? null : flowArgumentType.getName());

		// Flow interface so attempt to obtain the function internally
		SectionFunction linkFunction = this.getFunctionByTypeName(flowName);
		if (linkFunction != null) {
			// Link flow internally
			this.getDesigner().link(functionFlow, linkFunction, false);

		} else {
			// Not internal function, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(flowName, flowArgumentTypeName, false);
			this.getDesigner().link(functionFlow, sectionOutput, false);
		}
	}

	/**
	 * Links the {@link ManagedFunction} escalation.
	 * 
	 * @param function
	 *            {@link SectionFunction}.
	 * @param functionType
	 *            {@link ManagedFunctionType}.
	 * @param escalationType
	 *            {@link ManagedFunctionEscalationType}.
	 * @param escalationHandler
	 *            Potential {@link SectionFunction} that can handle escalation
	 *            based on its parameter. May be <code>null</code> if no
	 *            {@link SectionFunction} can handle the escalation.
	 */
	protected void linkFunctionEscalation(SectionFunction function, ManagedFunctionType<?, ?> functionType,
			ManagedFunctionEscalationType escalationType, SectionFunction escalationHandler) {

		// Obtain the escalation type name
		String escalationTypeName = escalationType.getEscalationType().getName();

		// Obtain the function escalation
		FunctionFlow functionEscalation = function.getFunctionEscalation(escalationTypeName);

		// Link to escalation handler (if available)
		// (Do not allow handling own escalation as potential for infinite loop)
		if ((escalationHandler != null) && (function != escalationHandler)) {
			// Handle escalation internally
			this.getDesigner().link(functionEscalation, escalationHandler, false);

		} else {
			// Not internally handled, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(escalationTypeName, escalationTypeName, true);
			this.getDesigner().link(functionEscalation, sectionOutput, false);
		}
	}

	/**
	 * Links the {@link FunctionObject}.
	 * 
	 * @param function
	 *            {@link SectionFunction}.
	 * @param functionType
	 *            {@link ManagedFunctionType}.
	 * @param objectType
	 *            {@link ManagedFunctionObjectType}.
	 */
	protected void linkFunctionObject(SectionFunction function, ManagedFunctionType<?, ?> functionType,
			ManagedFunctionObjectType<?> objectType) {

		// Obtain the object name and its type
		String objectName = objectType.getObjectName();
		String objectTypeName = objectType.getObjectType().getName();
		String typeQualifier = objectType.getTypeQualifier();

		// Obtain the function object
		FunctionObject functionObject = function.getFunctionObject(objectName);

		// Attempt to link to managed object
		SectionManagedObject mo = this.getManagedObject(objectTypeName);
		if (mo != null) {
			// Link to managed object
			this.getDesigner().link(functionObject, mo);

		} else {
			// Link to external object (by type)
			SectionObject sectionObject = this.getOrCreateObject(typeQualifier, objectTypeName);
			this.getDesigner().link(functionObject, sectionObject);
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
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Ensure only use once
		if (this.isSourced) {
			throw new IllegalStateException("May only use " + this.getClass().getName() + " once per instance");
		}
		this.isSourced = true;

		// Initiate state
		this._designer = designer;
		this._context = context;

		// Obtain the class name
		String sectionClassName = this.getSectionClassName();
		if ((sectionClassName == null) || (sectionClassName.trim().length() == 0)) {
			designer.addIssue("Must specify section class name within the location");
			return; // not able to load if no section class specified
		}

		// Obtain the class
		Class<?> sectionClass = this.getSectionClass(sectionClassName);

		// Add the managed object for the section class
		SectionManagedObject managedObject = this.createClassManagedObject(CLASS_OBJECT_NAME, sectionClass);

		// Obtain the dependency meta-data
		DependencyMetaData[] dependencyMetaData = this.extractClassManagedObjectDependencies(CLASS_OBJECT_NAME,
				sectionClass);

		// Load the managed objects
		for (DependencyMetaData dependency : dependencyMetaData) {

			// Obtain dependency name and type
			String dependencyName = dependency.name;
			String dependencyTypeName = dependency.field.getType().getName();

			// Obtain the managed object dependency
			ManagedObjectDependency moDependency = managedObject.getManagedObjectDependency(dependencyName);

			// Determine if managed object
			ManagedObject moAnnotation = dependency.field.getAnnotation(ManagedObject.class);
			if (moAnnotation != null) {
				// Use name of field to add the managed object
				String moName = dependency.name;

				// Add the managed object
				SectionManagedObjectSource mos = designer.addSectionManagedObjectSource(moName,
						moAnnotation.source().getName());
				for (Property property : moAnnotation.properties()) {
					String value = ("".equals(property.value()) ? property.valueClass().getName() : property.value());
					mos.addProperty(property.name(), value);
				}
				SectionManagedObject mo = mos.addSectionManagedObject(moName, ManagedObjectScope.PROCESS);

				// Add the type qualifiers for managed object
				for (TypeQualifier typeQualifier : moAnnotation.qualifiers()) {
					Class<?> qualifierClass = typeQualifier.qualifier();
					if (TypeQualifier.class.equals(qualifierClass)) {
						// No qualifier (as default value)
						qualifierClass = null;
					}
					String qualifier = (qualifierClass == null ? null : qualifierClass.getName());
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
					designer.addIssue("Unable to obtain type qualifier for dependency " + dependencyName, ex);
					return; // invalid section
				}

				// Link to external object (by qualified type)
				SectionObject sectionObject = this.getOrCreateObject(dependencyTypeQualifier, dependencyTypeName);
				designer.link(moDependency, sectionObject);
			}
		}

		// Link the managed object dependencies
		for (DependencyMetaData dependency : dependencyMetaData) {

			// Obtain the managed object annotation
			ManagedObject annotation = dependency.field.getAnnotation(ManagedObject.class);
			if (annotation == null) {
				continue; // not managed object dependency
			}

			// Obtain the managed object
			SectionManagedObject mo = this.getManagedObject(dependency.field.getType().getName());

			// Load the managed object type
			PropertyList moProperties = context.createPropertyList();
			for (Property property : annotation.properties()) {
				String value = ("".equals(property.value()) ? property.valueClass().getName() : property.value());
				moProperties.addProperty(property.name()).setValue(value);
			}
			ManagedObjectType<?> moType = context.loadManagedObjectType(annotation.source().getName(), moProperties);

			// Link the dependencies for the managed object
			for (ManagedObjectDependencyType<?> dependencyType : moType.getDependencyTypes()) {

				// Obtain the dependency type information
				String dependencyTypeName = dependencyType.getDependencyType().getName();
				String dependencyTypeQualifier = dependencyType.getTypeQualifier();

				// Obtain the managed object dependency
				ManagedObjectDependency moDependency = mo
						.getManagedObjectDependency(dependencyType.getDependencyName());

				// First attempt to link internally
				SectionManagedObject dependencyMo = this.getManagedObject(dependencyTypeName);
				if (dependencyMo != null) {
					// Link to managed object
					designer.link(moDependency, dependencyMo);

				} else {
					// Link to external object (by type)
					SectionObject sectionObject = this.getOrCreateObject(dependencyTypeQualifier, dependencyTypeName);
					designer.link(moDependency, sectionObject);
				}
			}
		}

		// Ensure the section class has functions
		boolean hasFunctionMethod = false;
		for (Method method : sectionClass.getMethods()) {
			if (!(method.getDeclaringClass().equals(Object.class))) {
				hasFunctionMethod = true; // declared a class
			}
		}
		if (!hasFunctionMethod) {
			designer.addIssue("Must have at least one public method on template logic class " + sectionClassName);
		}

		// Load the namespace type for the class
		PropertyList workProperties = context.createPropertyList();
		workProperties.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(sectionClassName);
		FunctionNamespaceType namespaceType = context
				.loadManagedFunctionType(SectionClassManagedFunctionSource.class.getName(), workProperties);

		// Add the namespace for the section class
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("NAMESPACE",
				SectionClassManagedFunctionSource.class.getName());
		namespace.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, sectionClassName);

		// Load functions
		Map<String, SectionFunction> functionsByParameterType = new HashMap<String, SectionFunction>();
		Map<String, Integer> parameterIndexes = new HashMap<String, Integer>();
		for (ManagedFunctionType<?, ?> functionType : namespaceType.getManagedFunctionTypes()) {

			// Obtain the function name
			String functionTypeName = functionType.getFunctionName();
			String functionName = this.getFunctionName(functionType);

			// Obtain the method for the function
			SectionManagedFunctionFactory functionFactory = (SectionManagedFunctionFactory) functionType
					.getManagedFunctionFactory();
			Method method = functionFactory.getMethod();

			// Add function (both by name and type name for internal linking)
			SectionFunction function = namespace.addSectionFunction(functionName, functionTypeName);
			this._functionsByName.put(functionName, function);
			this.registerFunctionByTypeName(functionTypeName, function);

			// Obtain the parameter for the function
			int objectIndex = 1; // 1 as Section Object first
			Class<?> parameterType = null;
			Class<?>[] parameters = method.getParameterTypes();
			Annotation[][] parametersAnnotations = method.getParameterAnnotations();
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
						throw new IllegalStateException("Method " + functionName
								+ " may only have one parameter annotated with " + Parameter.class.getSimpleName());
					}

					// Specify the parameter type
					parameterType = parameter;

					// Register the function by its parameter type
					functionsByParameterType.put(parameterType.getName(), function);

					// Register the parameter index for the function
					parameterIndexes.put(functionTypeName, new Integer(objectIndex));
				}

				// Increment object index for parameter
				objectIndex++;
			}

			// Enrich the function
			this.enrichFunction(function, functionType, method, parameterType);
		}

		// Link functions
		for (ManagedFunctionType<?, ?> functionType : namespaceType.getManagedFunctionTypes()) {

			// Obtain the function name
			String functionName = functionType.getFunctionName();

			// Obtain the function
			SectionFunction function = this.getFunctionByTypeName(functionName);

			// Obtain the function method
			SectionManagedFunctionFactory functionFactory = (SectionManagedFunctionFactory) functionType
					.getManagedFunctionFactory();
			Method method = functionFactory.getMethod();

			// Link the next function
			NextFunction nextFunctionAnnotation = method.getAnnotation(NextFunction.class);
			if (nextFunctionAnnotation != null) {

				// Obtain the argument type for the function
				Class<?> returnType = method.getReturnType();
				Class<?> argumentType = ((returnType == null) || (void.class.equals(returnType))
						|| (Void.TYPE.equals(returnType))) ? null : returnType;

				// Link next function
				this.linkNextFunction(function, functionType, method, argumentType, nextFunctionAnnotation);
			}

			// Obtain the flow meta-data for the function
			List<FlowMethodMetaData> flowMetaDatas = new LinkedList<FlowMethodMetaData>();
			ParameterFactory[] parameterFactories = functionFactory.getParameterFactories();
			for (ParameterFactory factory : parameterFactories) {

				// Ignore if not flow parameter factory
				if (!(factory instanceof FlowParameterFactory)) {
					continue; // ignore as not flow parameter factory
				}
				FlowParameterFactory flowParameterFactory = (FlowParameterFactory) factory;

				// Add the flow meta-data
				flowMetaDatas.addAll(Arrays.asList(flowParameterFactory.getFlowMethodMetaData()));
			}

			// Sort the flows by index
			Collections.sort(flowMetaDatas, new Comparator<FlowMethodMetaData>() {
				@Override
				public int compare(FlowMethodMetaData a, FlowMethodMetaData b) {
					return a.getFlowIndex() - b.getFlowIndex();
				}
			});

			// Link flows for the function
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

				// Link the function flow
				this.linkFunctionFlow(function, functionType, flowInterfaceType, flowMethod, flowArgumentType);
			}

			// Link escalations for the function
			for (ManagedFunctionEscalationType escalationType : functionType.getEscalationTypes()) {

				// Obtain function handling escalation (if available)
				String escalationTypeName = escalationType.getEscalationType().getName();
				SectionFunction escalationHandler = functionsByParameterType.get(escalationTypeName);

				// Link escalation
				this.linkFunctionEscalation(function, functionType, escalationType, escalationHandler);
			}

			// Obtain the object index for the parameter
			Integer parameterIndex = parameterIndexes.get(functionName);

			// Obtain the object types
			ManagedFunctionObjectType<?>[] objectTypes = functionType.getObjectTypes();

			// First object is always the section object
			ManagedFunctionObjectType<?> sectionObjectType = objectTypes[0];
			FunctionObject objectSection = function.getFunctionObject(sectionObjectType.getObjectName());
			designer.link(objectSection, managedObject);

			// Link remaining objects for function (1 as after section object)
			for (int i = 1; i < objectTypes.length; i++) {
				ManagedFunctionObjectType<?> objectType = objectTypes[i];

				// Determine if object is a parameter
				if ((parameterIndex != null) && (parameterIndex.intValue() == i)) {
					// Parameter so flag as parameter
					String objectName = objectType.getObjectName();
					FunctionObject functionObject = function.getFunctionObject(objectName);
					functionObject.flagAsParameter();
					continue; // next object
				}

				// Link the function object
				this.linkFunctionObject(function, functionType, objectType);
			}
		}
	}

}