/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectDependency;
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
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.managedobject.clazz.DependencyMetaData;
import net.officefloor.plugin.variable.VariableAnnotation;

/**
 * Abstract function {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFunctionSectionSource extends AbstractSectionSource {

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
	private final Map<String, SectionFunction> _functionsByName = new HashMap<>();

	/**
	 * Obtains the {@link SectionFunction} by its name.
	 * 
	 * @param functionName Name of the {@link SectionFunction}.
	 * @return {@link SectionFunction} or <code>null</code> if no
	 *         {@link SectionFunction} by the name.
	 */
	public SectionFunction getFunctionByName(String functionName) {
		return this._functionsByName.get(functionName);
	}

	/**
	 * {@link ManagedFunctionType} instances for the {@link SectionFunction}
	 * instances by name.
	 */
	private final Map<String, ManagedFunctionType<?, ?>> _functionTypesByName = new HashMap<>();

	/**
	 * Obtains the {@link ManagedFunctionType} for the {@link SectionFunction} by
	 * its name.
	 * 
	 * @param functionName Name of the {@link SectionFunction}.
	 * @return {@link ManagedFunctionType} or <code>null</code> if no
	 *         {@link SectionFunction} by the name.
	 */
	public ManagedFunctionType<?, ?> getFunctionTypeByName(String functionName) {
		return this._functionTypesByName.get(functionName);
	}

	/**
	 * {@link SectionFunction} instances by {@link ManagedFunctionType} name.
	 */
	private final Map<String, SectionFunction> _functionsByTypeName = new HashMap<>();

	/**
	 * Obtains the {@link SectionFunction} by its {@link ManagedFunctionType} name.
	 * 
	 * @param functionTypeName {@link ManagedFunctionType} name.
	 * @return {@link SectionFunction} or <code>null</code> if no
	 *         {@link SectionFunction} by the {@link ManagedFunctionType} name.
	 */
	public SectionFunction getFunctionByTypeName(String functionTypeName) {
		return this._functionsByTypeName.get(functionTypeName);
	}

	/**
	 * <p>
	 * Allows being made aware of further {@link SectionFunction} instances within
	 * the section to be considered for linking flows.
	 * <p>
	 * This allows {@link ClassSectionSource} to be used in conjunction with other
	 * functionality - such as template rendering for dynamic HTTP web pages.
	 * 
	 * @param functionTypeName Name to register the {@link SectionFunction}.
	 * @param function         {@link SectionFunction}.
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
	 * @param qualifier {@link Qualifier} for the {@link SectionObject}. If not
	 *                  {@link Qualifier} should be the same as the type name.
	 * @param typeName  Fully qualified type name of the {@link SectionObject}.
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
	 * @param typeName Fully qualified type name of the object for the
	 *                 {@link SectionManagedObject}.
	 * @return {@link SectionManagedObject} or <code>null</code> if no
	 *         {@link SectionManagedObject} for the type.
	 */
	public SectionManagedObject getManagedObject(String typeName) {
		return this._managedObjectsByTypeName.get(typeName);
	}

	/**
	 * {@link SectionInput} instances by name.
	 */
	private final Map<String, SectionInput> _inputsByName = new HashMap<>();

	/**
	 * <p>
	 * Obtains the {@link SectionInput}.
	 * <p>
	 * Should the {@link SectionInput} not yet be added, it is added.
	 * 
	 * @param name         Name of the {@link SectionInput}.
	 * @param argumentType Type of the argument. May be <code>null</code> if no
	 *                     argument.
	 * @return {@link SectionInput}.
	 */
	public SectionInput getOrCreateInput(String name, String argumentType) {
		SectionInput sectionInput = this._inputsByName.get(name);
		if (sectionInput == null) {
			// Not yet added, so add section input
			sectionInput = this.getDesigner().addSectionInput(name, argumentType);
			this._inputsByName.put(name, sectionInput);
		}
		return sectionInput;
	}

	/**
	 * {@link SectionOutput} instances by name.
	 */
	private final Map<String, SectionOutput> _outputsByName = new HashMap<>();

	/**
	 * <p>
	 * Obtains the {@link SectionOutput}.
	 * <p>
	 * Should the {@link SectionOutput} not yet be added, it is added.
	 * 
	 * @param name             Name of the {@link SectionOutput}.
	 * @param argumentType     Type of the argument. May be <code>null</code> if no
	 *                         argument.
	 * @param isEscalationOnly <code>true</code> if escalation only.
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
	 * {@link SubSection} instances by the section name.
	 */
	private final Map<String, SubSection> _subSectionsByName = new HashMap<>();

	/**
	 * <p>
	 * Obtains the {@link SubSection}.
	 * <p>
	 * Should the {@link SubSection} not already be created, it is created.
	 * 
	 * @param sectionInterfaceAnnotation {@link SectionInterfaceAnnotation}.
	 * @return {@link SubSection}.
	 */
	public SubSection getOrCreateSubSection(SectionInterfaceAnnotation sectionInterfaceAnnotation) {

		// Obtain the section name
		String subSectionName = sectionInterfaceAnnotation.getSectionName();

		// Determine if sub section already created for type
		SubSection subSection = this._subSectionsByName.get(subSectionName);
		if (subSection != null) {
			return subSection;
		}

		// Sub section not registered, so create and register
		String subSectionSourceClassName = sectionInterfaceAnnotation.getSource().getName();
		String subSectionLocation = sectionInterfaceAnnotation.getLocation();
		subSection = this.getDesigner().addSubSection(subSectionName, subSectionSourceClassName, subSectionLocation);
		PropertyList subSectionProperties = this.getContext().createPropertyList();
		for (PropertyValueAnnotation property : sectionInterfaceAnnotation.getProperties()) {
			String name = property.getName();
			String value = property.getValue();
			subSection.addProperty(name, value);
			subSectionProperties.addProperty(name).setValue(value);
		}

		// Register the sub section
		this._subSectionsByName.put(subSectionName, subSection);

		// Link outputs of sub section
		for (FlowLinkAnnotation flowLink : sectionInterfaceAnnotation.getOutputs()) {

			// Obtain the sub section output
			String subSectionOuputName = flowLink.getName();
			SubSectionOutput subSectionOuput = subSection.getSubSectionOutput(subSectionOuputName);

			// Obtain the section function for output
			String linkFunctionName = flowLink.getMethod();
			SectionFunction linkFunction = this.getFunctionByTypeName(linkFunctionName);
			if (linkFunction != null) {
				// Link flow internally
				this.getDesigner().link(subSectionOuput, linkFunction);
			}
		}

		// Load the section type
		SectionType subSectionType = this.getContext().loadSectionType(subSectionName, subSectionSourceClassName,
				subSectionLocation, subSectionProperties);

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
	 * @param sectionClassName Name of the section class.
	 * @return Section class.
	 * @throws Exception If fails to obtain the section class.
	 */
	protected Class<?> getSectionClass(String sectionClassName) throws Exception {
		return this.getContext().getClassLoader().loadClass(sectionClassName);
	}

	/**
	 * Creates the {@link SectionManagedObject} for providing the section object.
	 * 
	 * @param objectName   Name of the object within the section.
	 * @param sectionClass Section object class.
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
	 * @param objectName   Name of the object within the section.
	 * @param sectionClass Section object class.
	 * @return Extracted {@link DependencyMetaData} instances for the section
	 *         object.
	 * @throws Exception If fails to extract the {@link DependencyMetaData}
	 *                   instances.
	 */
	protected DependencyMetaData[] extractClassManagedObjectDependencies(String objectName, Class<?> sectionClass)
			throws Exception {
		return new SectionClassManagedObjectSource().extractDependencyMetaData(sectionClass);
	}

	/**
	 * Obtains the {@link ManagedFunction} name from the
	 * {@link ManagedFunctionType}.
	 * 
	 * @param functionType {@link ManagedFunctionType}.
	 * @return {@link ManagedFunction} name.
	 */
	protected String getFunctionName(ManagedFunctionType<?, ?> functionType) {
		return functionType.getFunctionName();
	}

	/**
	 * Enriches the {@link ManagedFunction}.
	 * 
	 * @param function      {@link SectionFunction}.
	 * @param functionType  {@link ManagedFunctionType} for the
	 *                      {@link SectionFunction}.
	 * @param parameterType Parameter type for the {@link SectionFunction}. May be
	 *                      <code>null</code> if no parameter.
	 */
	protected void enrichFunction(SectionFunction function, ManagedFunctionType<?, ?> functionType,
			Class<?> parameterType) {

		// Obtain the input name
		String inputName = this.getFunctionName(functionType);

		// Obtain the parameter type name
		String parameterTypeName = (parameterType == null ? null : parameterType.getName());

		// Add input for function
		SectionInput sectionInput = this.getOrCreateInput(inputName, parameterTypeName);
		this.getDesigner().link(sectionInput, function);
	}

	/**
	 * Links the next {@link ManagedFunction}.
	 * 
	 * @param function               {@link SectionFunction}.
	 * @param functionType           {@link ManagedFunctionType}.
	 * @param nextFunctionAnnotation {@link NextAnnotation}.
	 */
	protected void linkNextFunction(SectionFunction function, ManagedFunctionType<?, ?> functionType,
			NextAnnotation nextFunctionAnnotation) {

		// Obtain the next function details
		String nextFunctionName = nextFunctionAnnotation.getNextName();
		Class<?> argumentType = nextFunctionAnnotation.getArgumentType();
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
	 * @param functionFlow   {@link FunctionFlow}.
	 * @param functionType   {@link ManagedFunctionType}.
	 * @param flowAnnotation {@link FlowAnnotation}.
	 */
	protected void linkFunctionFlow(FunctionFlow functionFlow, ManagedFunctionType<?, ?> functionType,
			FlowAnnotation flowAnnotation) {

		// Obtain the flow name
		String flowName = functionFlow.getFunctionFlowName();

		// Obtain the flow argument name
		Class<?> flowArgumentType = flowAnnotation.getParameterType();
		String flowArgumentTypeName = (flowArgumentType == null ? null : flowArgumentType.getName());

		// Determine if spawn thread state
		boolean isSpawnThreadState = flowAnnotation.isSpawn();

		// Flow interface so attempt to obtain the function internally
		SectionFunction linkFunction = this.getFunctionByTypeName(flowName);
		if (linkFunction != null) {
			// Link flow internally
			this.getDesigner().link(functionFlow, linkFunction, isSpawnThreadState);

		} else {
			// Not internal function, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(flowName, flowArgumentTypeName, false);
			this.getDesigner().link(functionFlow, sectionOutput, isSpawnThreadState);
		}
	}

	/**
	 * Links the {@link FunctionFlow}.
	 * 
	 * @param functionFlow               {@link FunctionFlow}.
	 * @param functionType               {@link ManagedFunctionType}.
	 * @param sectionInterfaceAnnotation {@link SectionInterfaceAnnotation}.
	 */
	protected void linkFunctionFlow(FunctionFlow functionFlow, ManagedFunctionType<?, ?> functionType,
			SectionInterfaceAnnotation sectionInterfaceAnnotation) {

		// Section interface so obtain the sub section
		SubSection subSection = this.getOrCreateSubSection(sectionInterfaceAnnotation);

		// Link flow to sub section input
		SubSectionInput subSectionInput = subSection.getSubSectionInput(functionFlow.getFunctionFlowName());
		this.getDesigner().link(functionFlow, subSectionInput, false);
	}

	/**
	 * Links the {@link ManagedFunction} escalation.
	 * 
	 * @param function          {@link SectionFunction}.
	 * @param functionType      {@link ManagedFunctionType}.
	 * @param escalationType    {@link ManagedFunctionEscalationType}.
	 * @param escalationHandler Potential {@link SectionFunction} that can handle
	 *                          escalation based on its parameter. May be
	 *                          <code>null</code> if no {@link SectionFunction} can
	 *                          handle the escalation.
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
	 * @param function     {@link SectionFunction}.
	 * @param functionType {@link ManagedFunctionType}.
	 * @param objectType   {@link ManagedFunctionObjectType}.
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

	/**
	 * {@link Object} for {@link Method}.
	 */
	protected class ObjectForMethod {

		/**
		 * {@link Object} for {@link Method}.
		 */
		protected final SectionManagedObject object;

		/**
		 * Error message for failing to obtain the {@link Object} for the
		 * {@link Method}.
		 */
		protected final String errorMessage;

		/**
		 * Successfully obtained {@link SectionManagedObject}.
		 * 
		 * @param object {@link SectionManagedObject}.
		 */
		protected ObjectForMethod(SectionManagedObject object) {
			if (object == null) {
				throw new IllegalArgumentException("Must provide " + SectionManagedObject.class.getSimpleName());
			}
			this.object = object;
			this.errorMessage = null;
		}

		/**
		 * Failed to obtain {@link SectionManagedObject}.
		 * 
		 * @param errorMessage Error message.
		 * @param cause        Possible cause for error. May be <code>null</code>.
		 */
		protected ObjectForMethod(String errorMessage, Throwable cause) {
			if (CompileUtil.isBlank(errorMessage)) {
				throw new IllegalArgumentException("Must provide error message");
			}
			this.errorMessage = errorMessage;
			this.object = null;

			// Detail the error
			if (cause != null) {
				AbstractFunctionSectionSource.this.getDesigner().addIssue(errorMessage, cause);
			} else {
				AbstractFunctionSectionSource.this.getDesigner().addIssue(errorMessage);
			}
		}
	}

	/**
	 * Loads the {@link SectionManagedObject} for the {@link Object} for the
	 * {@link Method}.
	 * 
	 * @param sectionClass {@link Class} of the {@link Object}.
	 * @return {@link ObjectForMethod}. May be <code>null</code> for no
	 *         {@link Object} for {@link Method}.
	 * @throws Exception If fails to load {@link Object} for the {@link Method}.
	 */
	protected ObjectForMethod loadObjectForMethod(Class<?> sectionClass) throws Exception {

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
			SectionManagedObjectDependency moDependency = managedObject
					.getSectionManagedObjectDependency(dependencyName);

			// Determine if managed object
			ManagedObject moAnnotation = dependency.field.getAnnotation(ManagedObject.class);
			if (moAnnotation != null) {
				// Use name of field to add the managed object
				String moName = dependency.name;

				// Add the managed object
				SectionManagedObjectSource mos = this.getDesigner().addSectionManagedObjectSource(moName,
						moAnnotation.source().getName());
				for (PropertyValue property : moAnnotation.properties()) {
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
				this.getDesigner().link(moDependency, mo);

			} else {
				// Obtain the type qualifier for external object
				String dependencyTypeQualifier;
				try {
					dependencyTypeQualifier = dependency.getTypeQualifier();
				} catch (IllegalArgumentException ex) {
					return new ObjectForMethod("Unable to obtain type qualifier for dependency " + dependencyName, ex);
				}

				// Link to external object (by qualified type)
				SectionObject sectionObject = this.getOrCreateObject(dependencyTypeQualifier, dependencyTypeName);
				this.getDesigner().link(moDependency, sectionObject);
			}
		}

		// Link the managed object dependencies
		NEXT_DEPENDENCY: for (DependencyMetaData dependency : dependencyMetaData) {

			// Obtain the managed object annotation
			ManagedObject annotation = dependency.field.getAnnotation(ManagedObject.class);
			if (annotation == null) {
				continue NEXT_DEPENDENCY; // not managed object dependency
			}

			// Obtain the managed object
			String managedObjectName = dependency.field.getType().getName();
			SectionManagedObject mo = this.getManagedObject(managedObjectName);

			// Load the managed object type
			PropertyList moProperties = this.getContext().createPropertyList();
			for (PropertyValue property : annotation.properties()) {
				String value = ("".equals(property.value()) ? property.valueClass().getName() : property.value());
				moProperties.addProperty(property.name()).setValue(value);
			}
			ManagedObjectType<?> moType = this.getContext().loadManagedObjectType(managedObjectName,
					annotation.source().getName(), moProperties);

			// Link the dependencies for the managed object
			for (ManagedObjectDependencyType<?> dependencyType : moType.getDependencyTypes()) {

				// Obtain the dependency type information
				String dependencyTypeName = dependencyType.getDependencyType().getName();
				String dependencyTypeQualifier = dependencyType.getTypeQualifier();

				// Obtain the managed object dependency
				SectionManagedObjectDependency moDependency = mo
						.getSectionManagedObjectDependency(dependencyType.getDependencyName());

				// First attempt to link internally
				SectionManagedObject dependencyMo = this.getManagedObject(dependencyTypeName);
				if (dependencyMo != null) {
					// Link to managed object
					this.getDesigner().link(moDependency, dependencyMo);

				} else {
					// Link to external object (by type)
					SectionObject sectionObject = this.getOrCreateObject(dependencyTypeQualifier, dependencyTypeName);
					this.getDesigner().link(moDependency, sectionObject);
				}
			}
		}

		// Return the object
		return new ObjectForMethod(managedObject);
	}

	/**
	 * Loads the {@link FunctionNamespaceType}.
	 * 
	 * @param namespace    Namespace.
	 * @param sectionClass Section {@link Class}.
	 * @return {@link FunctionNamespaceType}.
	 */
	protected FunctionNamespaceType loadFunctionNamespaceType(String namespace, Class<?> sectionClass) {
		PropertyList workProperties = this.getContext().createPropertyList();
		workProperties.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(sectionClass.getName());
		return this.getContext().loadManagedFunctionType(namespace, SectionClassManagedFunctionSource.class.getName(),
				workProperties);
	}

	/**
	 * Adds the {@link SectionFunctionNamespace}.
	 * 
	 * @param namespace    Namespace.
	 * @param sectionClass Section {@link Class}.
	 * @return {@link SectionFunctionNamespace}.
	 */
	protected SectionFunctionNamespace adddSectionFunctionNamespace(String namespace, Class<?> sectionClass) {
		SectionFunctionNamespace functionNamespace = this.getDesigner().addSectionFunctionNamespace(namespace,
				SectionClassManagedFunctionSource.class.getName());
		functionNamespace.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				sectionClass.getName());
		return functionNamespace;
	}

	/**
	 * Indicates if include {@link ManagedFunctionType}.
	 * 
	 * @param functionType {@link ManagedFunctionType}.
	 * @return <code>true</code> include the {@link ManagedFunctionType}.
	 */
	protected boolean isIncludeManagedFunctionType(ManagedFunctionType<?, ?> functionType) {
		return true;
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
		if (sectionClass == null) {
			return;
		}

		// Load the object for the methods
		ObjectForMethod objectForMethod = this.loadObjectForMethod(sectionClass);
		SectionManagedObject sectionObject;
		if (objectForMethod != null) {
			if (objectForMethod.errorMessage != null) {
				return; // invalid section
			}
			sectionObject = objectForMethod.object;
		} else {
			// No object required
			sectionObject = null;
		}

		// Ensure the section class has functions
		boolean hasFunctionMethod = false;
		HAS_METHOD: for (Method method : sectionClass.getMethods()) {
			if (!(method.getDeclaringClass().equals(Object.class))) {
				// Has non-object method
				hasFunctionMethod = true;
				break HAS_METHOD;
			}
		}
		if (!hasFunctionMethod) {
			designer.addIssue("Must have at least one public method on template logic class " + sectionClassName);
		}

		// Load the namespace type for the class
		String functionNamespace = "NAMESPACE";
		FunctionNamespaceType namespaceType = this.loadFunctionNamespaceType(functionNamespace, sectionClass);

		// Add the namespace for the section class
		SectionFunctionNamespace namespace = this.adddSectionFunctionNamespace(functionNamespace, sectionClass);

		// Load functions
		Map<String, SectionFunction> functionsByParameterType = new HashMap<>();
		Map<String, Integer> parameterIndexes = new HashMap<>();
		Set<String> includedFunctionTypeNames = new HashSet<>();
		NEXT_FUNCTION: for (ManagedFunctionType<?, ?> functionType : namespaceType.getManagedFunctionTypes()) {

			// Obtain the function type name
			String functionTypeName = functionType.getFunctionName();

			// Determine if include function
			if (!this.isIncludeManagedFunctionType(functionType)) {
				continue NEXT_FUNCTION;
			}
			includedFunctionTypeNames.add(functionTypeName);

			// Obtain the function name
			String functionName = this.getFunctionName(functionType);

			// Add function (both by name and type name for internal linking)
			SectionFunction function = namespace.addSectionFunction(functionName, functionTypeName);
			this._functionsByName.put(functionName, function);
			this._functionTypesByName.put(functionName, functionType);
			this.registerFunctionByTypeName(functionTypeName, function);

			// Obtain the parameter for the function
			ParameterAnnotation parameterAnnotation = functionType.getAnnotation(ParameterAnnotation.class);
			Class<?> parameterType = null;
			if (parameterAnnotation != null) {
				// Specify the parameter type
				parameterType = parameterAnnotation.getParameterType();

				// Register the function by its parameter type (for exception handling)
				functionsByParameterType.put(parameterType.getName(), function);

				// Register the parameter index for the function
				parameterIndexes.put(functionTypeName, Integer.valueOf(parameterAnnotation.getParameterIndex()));
			}

			// Enrich the function
			this.enrichFunction(function, functionType, parameterType);
		}

		// Link functions
		NEXT_FUNCTION: for (ManagedFunctionType<?, ?> functionType : namespaceType.getManagedFunctionTypes()) {

			// Obtain the function type name
			String functionTypeName = functionType.getFunctionName();

			// Ensure include the function
			if (!includedFunctionTypeNames.contains(functionTypeName)) {
				continue NEXT_FUNCTION;
			}

			// Obtain the function
			SectionFunction function = this.getFunctionByTypeName(functionTypeName);

			// Link the next (if available)
			NextAnnotation nextFunctionAnnotation = functionType.getAnnotation(NextAnnotation.class);
			if (nextFunctionAnnotation != null) {
				this.linkNextFunction(function, functionType, nextFunctionAnnotation);
			}

			// Obtain the flow meta-data for the function
			FlowAnnotation[] flowAnnotations = functionType.getAnnotation(FlowAnnotation[].class);
			if (flowAnnotations != null) {

				// Sort the flows by index
				List<FlowAnnotation> flowList = Arrays.asList(flowAnnotations);
				Collections.sort(flowList, (a, b) -> a.getFlowIndex() - b.getFlowIndex());

				// Link flows for the function
				for (FlowAnnotation flow : flowList) {

					// Obtain the function flow
					FunctionFlow functionFlow = function.getFunctionFlow(flow.getFlowName());

					// Link the function flow
					this.linkFunctionFlow(functionFlow, functionType, flow);
				}
			}

			// Obtain the flow meta-data for the function
			SectionInterfaceAnnotation[] sectionAnnotations = functionType
					.getAnnotation(SectionInterfaceAnnotation[].class);
			if (sectionAnnotations != null) {

				// Sort the flows by index
				List<SectionInterfaceAnnotation> flowList = Arrays.asList(sectionAnnotations);
				Collections.sort(flowList, (a, b) -> a.getFlowIndex() - b.getFlowIndex());

				// Link flows for the function
				for (SectionInterfaceAnnotation flow : flowList) {

					// Obtain the function flow
					FunctionFlow functionFlow = function.getFunctionFlow(flow.getFlowName());

					// Link the function flow
					this.linkFunctionFlow(functionFlow, functionType, flow);
				}
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
			Integer parameterIndex = parameterIndexes.get(functionTypeName);

			// Obtain the object types
			ManagedFunctionObjectType<?>[] objectTypes = functionType.getObjectTypes();

			// Determine first object is the section object
			int objectIndex = 0;
			if (sectionObject != null) {
				ManagedFunctionObjectType<?> sectionObjectType = objectTypes[objectIndex++];
				FunctionObject objectSection = function.getFunctionObject(sectionObjectType.getObjectName());
				designer.link(objectSection, sectionObject);
			}

			// Link remaining objects for function (1 as after section object)
			NEXT_OBJECT: for (int i = objectIndex; i < objectTypes.length; i++) {
				ManagedFunctionObjectType<?> objectType = objectTypes[i];

				// Determine if object is a parameter
				if ((parameterIndex != null) && ((parameterIndex.intValue() + objectIndex) == i)) {
					// Parameter so flag as parameter
					String objectName = objectType.getObjectName();
					FunctionObject functionObject = function.getFunctionObject(objectName);
					functionObject.flagAsParameter();
					continue NEXT_OBJECT;
				}

				// Variable registered via augmentation
				String variableName = VariableAnnotation.extractPossibleVariableName(objectType);
				if (variableName != null) {
					continue NEXT_OBJECT; // not include variable
				}

				// Link the function object
				this.linkFunctionObject(function, functionType, objectType);
			}
		}
	}

}
