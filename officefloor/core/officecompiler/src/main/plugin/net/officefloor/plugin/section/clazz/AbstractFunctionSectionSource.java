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
import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
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
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
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
	 * Obtains the name of the class for the section.
	 * 
	 * @return Class name for the backing class of the section.
	 */
	protected String getSectionClassName(SectionSourceContext context) {
		return context.getSectionLocation();
	}

	/**
	 * Indicates if have qualifier.
	 * 
	 * @param qualifier Qualifier.
	 * @return <code>true</code> if have qualifier.
	 */
	protected boolean isQualifier(String qualifier) {
		return !CompileUtil.isBlank(qualifier);
	}

	/**
	 * Obtains the {@link SectionObject} name.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param typeName  Type of object.
	 * @return {@link SectionObject} name.
	 */
	protected String getSectionObjectName(String qualifier, String typeName) {
		return (this.isQualifier(qualifier) ? qualifier + "-" : "") + typeName;
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
	 * <p>
	 * Obtains the {@link SectionDependencyObjectNode}.
	 * <p>
	 * Should the {@link SectionDependencyObjectNode} not yet be added, it is added.
	 * 
	 * @param qualifier      {@link Qualifier} for the
	 *                       {@link SectionDependencyObjectNode}. If not
	 *                       {@link Qualifier} should be the same as the type name.
	 * @param typeName       Fully qualified type name of the {@link SectionObject}.
	 * @param designer       {@link SectionDesigner}.
	 * @param sectionObjects {@link SectionDependencyObjectNode} instances by
	 *                       dependency name.
	 * @return {@link SectionDependencyObjectNode}.
	 */
	public SectionDependencyObjectNode getOrCreateObject(String qualifier, String typeName, SectionDesigner designer,
			Map<String, SectionDependencyObjectNode> sectionObjects) {

		// Determine the object name
		String objectName = this.getSectionObjectName(qualifier, typeName);

		// Attempt to obtain existing
		SectionDependencyObjectNode sectionObjectNode = sectionObjects.get(objectName);
		if (sectionObjectNode != null) {
			return sectionObjectNode;
		}

		// No yet added, so create section object
		SectionObject sectionObject = designer.addSectionObject(objectName, typeName);
		sectionObjects.put(objectName, sectionObject);

		// Provide type qualifier (if specified)
		if (this.isQualifier(qualifier)) {
			sectionObject.setTypeQualifier(qualifier);
		}

		// Return the section object
		return sectionObject;
	}

	protected SectionDependencyObjectNode getDependencyObject(String typeQualifier, String typeName,
			SectionDesigner designer, SectionSourceContext context,
			Map<String, SectionDependencyObjectNode> sectionObjects,
			Map<String, ManagedObjectType<?>> sectionManagedObjectTypes,
			ManagedObjectDependencyType<?> dependencyType) {

		// TODO consider making dependencies pluggable

		// Determine if managed object
		ManagedObject moAnnotation = dependencyType == null ? null : dependencyType.getAnnotation(ManagedObject.class);
		if (moAnnotation != null) {

			// Use the dependency type name as managed object name
			String moName = dependencyType.getDependencyName();

			// Load the managed object type
			PropertyList properties = context.createPropertyList();
			for (PropertyValue property : moAnnotation.properties()) {
				String value = ("".equals(property.value()) ? property.valueClass().getName() : property.value());
				properties.addProperty(property.name()).setValue(value);
			}
			ManagedObjectType<?> moType = context.loadManagedObjectType(moName, moAnnotation.source().getName(),
					properties);

			// Register the managed object to link dependencies
			sectionManagedObjectTypes.put(moName, moType);

			// Add the managed object
			SectionManagedObjectSource mos = designer.addSectionManagedObjectSource(moName,
					moAnnotation.source().getName());
			for (PropertyValue property : moAnnotation.properties()) {
				String value = ("".equals(property.value()) ? property.valueClass().getName() : property.value());
				mos.addProperty(property.name(), value);
			}
			SectionManagedObject mo = mos.addSectionManagedObject(moName, ManagedObjectScope.PROCESS);

			// Add the type qualifiers for managed object
			for (TypeQualifier qualifierAnnotation : moAnnotation.qualifiers()) {
				Class<?> qualifierClass = qualifierAnnotation.qualifier();
				if (TypeQualifier.class.equals(qualifierClass)) {
					// No qualifier (as default value)
					qualifierClass = null;
				}
				String qualifier = (qualifierClass == null ? null : qualifierClass.getName());
				String type = qualifierAnnotation.type().getName();
				mo.addTypeQualification(qualifier, type);
			}

			// Register the managed object
			String objectName = this.getSectionObjectName(typeQualifier, typeName);
			sectionObjects.put(objectName, mo);

			// Return the managed object
			return mo;

		} else {

			// Link to external object (by qualified type)
			SectionDependencyObjectNode sectionObject = this.getOrCreateObject(typeQualifier, typeName, designer,
					sectionObjects);

			// Return the section object
			return sectionObject;
		}
	}

	/**
	 * <p>
	 * Obtains the {@link SectionInput}.
	 * <p>
	 * Should the {@link SectionInput} not yet be added, it is added.
	 * 
	 * @param name         Name of the {@link SectionInput}.
	 * @param argumentType Type of the argument. May be <code>null</code> if no
	 *                     argument.
	 * @param designer     {@link SectionDesigner}.
	 * @param inputsByName {@link SectionInput} instances by name.
	 * @return {@link SectionInput}.
	 */
	public SectionInput getOrCreateInput(String name, String argumentType, SectionDesigner designer,
			Map<String, SectionInput> inputsByName) {
		SectionInput sectionInput = inputsByName.get(name);
		if (sectionInput == null) {
			// Not yet added, so add section input
			sectionInput = designer.addSectionInput(name, argumentType);
			inputsByName.put(name, sectionInput);
		}
		return sectionInput;
	}

	/**
	 * Enriches the {@link ManagedFunction}.
	 * 
	 * @param function      {@link SectionFunction}.
	 * @param functionType  {@link ManagedFunctionType} for the
	 *                      {@link SectionFunction}.
	 * @param parameterType Parameter type for the {@link SectionFunction}. May be
	 *                      <code>null</code> if no parameter.
	 * @param designer      {@link SectionDesigner}.
	 * @param inputsByName  {@link SectionInput} instances by name.
	 */
	protected void enrichFunction(SectionFunction function, ManagedFunctionType<?, ?> functionType,
			Class<?> parameterType, SectionDesigner designer, Map<String, SectionInput> inputsByName) {

		// Obtain the input name
		String inputName = this.getFunctionName(functionType);

		// Obtain the parameter type name
		String parameterTypeName = (parameterType == null ? null : parameterType.getName());

		// Add input for function
		SectionInput sectionInput = this.getOrCreateInput(inputName, parameterTypeName, designer, inputsByName);
		designer.link(sectionInput, function);
	}

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
	 * @param designer         {@link SectionDesigner}.
	 * @param outputsByName    {@link SectionOutput} instances by name.
	 * @return {@link SectionObject}.
	 */
	public SectionOutput getOrCreateOutput(String name, String argumentType, boolean isEscalationOnly,
			SectionDesigner designer, Map<String, SectionOutput> outputsByName) {
		SectionOutput sectionOutput = outputsByName.get(name);
		if (sectionOutput == null) {
			// Not yet added, so add section output
			sectionOutput = designer.addSectionOutput(name, argumentType, isEscalationOnly);
			outputsByName.put(name, sectionOutput);
		}
		return sectionOutput;
	}

	/**
	 * Links the next {@link ManagedFunction}.
	 * 
	 * @param function               {@link SectionFunction}.
	 * @param functionType           {@link ManagedFunctionType}.
	 * @param nextFunctionAnnotation {@link NextAnnotation}.
	 */
	protected void linkNextFunction(SectionFunction function, ManagedFunctionType<?, ?> functionType,
			NextAnnotation nextFunctionAnnotation, SectionDesigner designer,
			Map<String, SectionFunction> functionsByName, Map<String, SectionOutput> outputsByName) {

		// Obtain the next function details
		String nextFunctionName = nextFunctionAnnotation.getNextName();
		Class<?> argumentType = nextFunctionAnnotation.getArgumentType();
		String argumentTypeName = (argumentType == null ? null : argumentType.getName());

		// Attempt to obtain next function internally
		SectionFunction nextFunction = functionsByName.get(nextFunctionName);
		if (nextFunction != null) {
			// Link function internally
			designer.link(function, nextFunction);

		} else {
			// Not internal function, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(nextFunctionName, argumentTypeName, false, designer,
					outputsByName);
			designer.link(function, sectionOutput);
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
			FlowAnnotation flowAnnotation, SectionDesigner designer, Map<String, SectionFunction> functionsByName,
			Map<String, SectionOutput> outputsByName) {

		// Obtain the flow name
		String flowName = functionFlow.getFunctionFlowName();

		// Obtain the flow argument name
		Class<?> flowArgumentType = flowAnnotation.getParameterType();
		String flowArgumentTypeName = (flowArgumentType == null ? null : flowArgumentType.getName());

		// Determine if spawn thread state
		boolean isSpawnThreadState = flowAnnotation.isSpawn();

		// Flow interface so attempt to obtain the function internally
		SectionFunction linkFunction = functionsByName.get(flowName);
		if (linkFunction != null) {
			// Link flow internally
			designer.link(functionFlow, linkFunction, isSpawnThreadState);

		} else {
			// Not internal function, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(flowName, flowArgumentTypeName, false, designer,
					outputsByName);
			designer.link(functionFlow, sectionOutput, isSpawnThreadState);
		}
	}

	/**
	 * <p>
	 * Obtains the {@link SubSection}.
	 * <p>
	 * Should the {@link SubSection} not already be created, it is created.
	 * 
	 * @param sectionInterfaceAnnotation {@link SectionInterfaceAnnotation}.
	 * @return {@link SubSection}.
	 */
	public SubSection getOrCreateSubSection(SectionInterfaceAnnotation sectionInterfaceAnnotation,
			SectionDesigner designer, SectionSourceContext context, Map<String, SubSection> subSectionsByName,
			Map<String, SectionFunction> functionsByName, Map<String, SectionDependencyObjectNode> sectionObjects,
			Map<String, ManagedObjectType<?>> sectionManagedObjectTypes) {

		// Obtain the section name
		String subSectionName = sectionInterfaceAnnotation.getSectionName();

		// Determine if sub section already created for type
		SubSection subSection = subSectionsByName.get(subSectionName);
		if (subSection != null) {
			return subSection;
		}

		// Sub section not registered, so create and register
		String subSectionSourceClassName = sectionInterfaceAnnotation.getSource().getName();
		String subSectionLocation = sectionInterfaceAnnotation.getLocation();
		subSection = designer.addSubSection(subSectionName, subSectionSourceClassName, subSectionLocation);
		PropertyList subSectionProperties = context.createPropertyList();
		for (PropertyValueAnnotation property : sectionInterfaceAnnotation.getProperties()) {
			String name = property.getName();
			String value = property.getValue();
			subSection.addProperty(name, value);
			subSectionProperties.addProperty(name).setValue(value);
		}

		// Register the sub section
		subSectionsByName.put(subSectionName, subSection);

		// Link outputs of sub section
		for (FlowLinkAnnotation flowLink : sectionInterfaceAnnotation.getOutputs()) {

			// Obtain the sub section output
			String subSectionOuputName = flowLink.getName();
			SubSectionOutput subSectionOuput = subSection.getSubSectionOutput(subSectionOuputName);

			// Obtain the section function for output
			String linkFunctionName = flowLink.getMethod();
			SectionFunction linkFunction = functionsByName.get(linkFunctionName);
			if (linkFunction != null) {
				// Link flow internally
				designer.link(subSectionOuput, linkFunction);
			}
		}

		// Load the section type
		SectionType subSectionType = context.loadSectionType(subSectionName, subSectionSourceClassName,
				subSectionLocation, subSectionProperties);

		// Link objects of sub section
		for (SectionObjectType subSectionObjectType : subSectionType.getSectionObjectTypes()) {

			// Obtain the sub section object
			String objectName = subSectionObjectType.getSectionObjectName();
			SubSectionObject subSectionObject = subSection.getSubSectionObject(objectName);

			// Link to dependency
			String objectTypeQualifier = subSectionObjectType.getTypeQualifier();
			String objectTypeName = subSectionObjectType.getObjectType();
			SectionDependencyObjectNode dependency = this.getDependencyObject(objectTypeQualifier, objectTypeName,
					designer, context, sectionObjects, sectionManagedObjectTypes, null);
			designer.link(subSectionObject, dependency);
		}

		// Return the sub section
		return subSection;
	}

	/**
	 * Links the {@link FunctionFlow}.
	 * 
	 * @param functionFlow               {@link FunctionFlow}.
	 * @param functionType               {@link ManagedFunctionType}.
	 * @param sectionInterfaceAnnotation {@link SectionInterfaceAnnotation}.
	 */
	protected void linkFunctionFlow(FunctionFlow functionFlow, ManagedFunctionType<?, ?> functionType,
			SectionInterfaceAnnotation sectionInterfaceAnnotation, SectionDesigner designer,
			SectionSourceContext context, Map<String, SubSection> subSectionsByName,
			Map<String, SectionFunction> functionsByName, Map<String, SectionDependencyObjectNode> sectionObjects,
			Map<String, ManagedObjectType<?>> sectionManagedObjectTypes) {

		// Section interface so obtain the sub section
		SubSection subSection = this.getOrCreateSubSection(sectionInterfaceAnnotation, designer, context,
				subSectionsByName, functionsByName, sectionObjects, sectionManagedObjectTypes);

		// Link flow to sub section input
		SubSectionInput subSectionInput = subSection.getSubSectionInput(functionFlow.getFunctionFlowName());
		designer.link(functionFlow, subSectionInput, false);
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
			ManagedFunctionEscalationType escalationType, SectionFunction escalationHandler, SectionDesigner designer,
			Map<String, SectionOutput> sectionOutputs) {

		// Obtain the escalation type name
		String escalationTypeName = escalationType.getEscalationType().getName();

		// Obtain the function escalation
		FunctionFlow functionEscalation = function.getFunctionEscalation(escalationTypeName);

		// Link to escalation handler (if available)
		// (Do not allow handling own escalation as potential for infinite loop)
		if ((escalationHandler != null) && (function != escalationHandler)) {
			// Handle escalation internally
			designer.link(functionEscalation, escalationHandler, false);

		} else {
			// Not internally handled, so link externally
			SectionOutput sectionOutput = this.getOrCreateOutput(escalationTypeName, escalationTypeName, true, designer,
					sectionOutputs);
			designer.link(functionEscalation, sectionOutput, false);
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
			ManagedFunctionObjectType<?> objectType, SectionDesigner designer, SectionSourceContext context,
			Map<String, SectionDependencyObjectNode> sectionObjects,
			Map<String, ManagedObjectType<?>> sectionManagedObjectTypes) {

		// Obtain the object name and its type
		String objectName = objectType.getObjectName();
		String objectTypeName = objectType.getObjectType().getName();
		String typeQualifier = objectType.getTypeQualifier();

		// Obtain the function object
		FunctionObject functionObject = function.getFunctionObject(objectName);

		// Link to object
		SectionDependencyObjectNode dependency = this.getDependencyObject(typeQualifier, objectTypeName, designer,
				context, sectionObjects, sectionManagedObjectTypes, null);
		designer.link(functionObject, dependency);
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

		// Obtain the class name
		String sectionClassName = this.getSectionClassName(context);
		if ((sectionClassName == null) || (sectionClassName.trim().length() == 0)) {
			designer.addIssue("Must specify section class name within the location");
			return; // not able to load if no section class specified
		}

		// Obtain the class
		Class<?> sectionClass = context.loadClass(sectionClassName);

		// Load the object for the section
		SectionManagedObjectSource sectionManagedObjectSource = designer
				.addSectionManagedObjectSource(CLASS_OBJECT_NAME, ClassManagedObjectSource.class.getName());
		sectionManagedObjectSource.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, sectionClassName);
		SectionManagedObject sectionObject = sectionManagedObjectSource.addSectionManagedObject(CLASS_OBJECT_NAME,
				ManagedObjectScope.THREAD);

		// Load type for the section object
		PropertyList properties = context.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(sectionClassName);
		ManagedObjectType<?> sectionObjectType = context.loadManagedObjectType(CLASS_OBJECT_NAME,
				ClassManagedObjectSource.class.getName(), properties);

		// Registry of the section objects
		Map<String, SectionDependencyObjectNode> sectionObjects = new HashMap<>();
		Map<String, ManagedObjectType<?>> sectionManagedObjectTypes = new HashMap<>();

		// Load in the dependencies
		for (ManagedObjectDependencyType<?> dependencyType : sectionObjectType.getDependencyTypes()) {

			// Obtain the required dependency
			SectionManagedObjectDependency requiredDependency = sectionObject
					.getSectionManagedObjectDependency(dependencyType.getDependencyName());

			// Obtain the dependent object
			String dependencyTypeQualifier = dependencyType.getTypeQualifier();
			String dependencyTypeName = dependencyType.getDependencyType().getName();
			SectionDependencyObjectNode dependencyObject = this.getDependencyObject(dependencyTypeQualifier,
					dependencyTypeName, designer, context, sectionObjects, sectionManagedObjectTypes, dependencyType);

			// Link dependency
			designer.link(requiredDependency, dependencyObject);
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
			designer.addIssue("Must have at least one public method on section class " + sectionClassName);
		}

		// Load the namespace type for the class
		String namespaceName = "NAMESPACE";
		PropertyList functionProperties = context.createPropertyList();
		functionProperties.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(sectionClass.getName());
		FunctionNamespaceType namespaceType = context.loadManagedFunctionType(namespaceName,
				SectionClassManagedFunctionSource.class.getName(), functionProperties);

		// Add the namespace
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(namespaceName,
				SectionClassManagedFunctionSource.class.getName());
		namespace.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, sectionClass.getName());

		// Create registries for linking functions
		Map<String, SectionFunction> functionsByName = new HashMap<>();
		Map<String, ManagedFunctionType<?, ?>> functionTypesByName = new HashMap<>();
		Map<String, SubSection> subSectionsByName = new HashMap<>();
		Map<String, SectionFunction> functionsByParameterType = new HashMap<>();
		Map<String, SectionInput> sectionInputs = new HashMap<>();
		Map<String, SectionOutput> sectionOutputs = new HashMap<>();
		Map<String, Integer> parameterIndexes = new HashMap<>();
		Set<String> includedFunctionTypeNames = new HashSet<>();

		// Load functions
		NEXT_FUNCTION: for (ManagedFunctionType<?, ?> functionType : namespaceType.getManagedFunctionTypes()) {

			// Obtain the function type name
			String functionTypeName = functionType.getFunctionName();

//			// Determine if include function
//			if (!this.isIncludeManagedFunctionType(functionType)) {
//				continue NEXT_FUNCTION;
//			}
			includedFunctionTypeNames.add(functionTypeName);

			// Obtain the function name
			String functionName = this.getFunctionName(functionType);

			// Add function (both by name and type name for internal linking)
			SectionFunction function = namespace.addSectionFunction(functionName, functionTypeName);
			functionsByName.put(functionName, function);
			functionTypesByName.put(functionName, functionType);

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
			this.enrichFunction(function, functionType, parameterType, designer, sectionInputs);
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
			SectionFunction function = functionsByName.get(functionTypeName);

			// Link the next (if available)
			NextAnnotation nextFunctionAnnotation = functionType.getAnnotation(NextAnnotation.class);
			if (nextFunctionAnnotation != null) {
				this.linkNextFunction(function, functionType, nextFunctionAnnotation, designer, functionsByName,
						sectionOutputs);
			}

			// Obtain the flow meta-data for the function
			FlowAnnotation[] flowAnnotations = functionType.getAnnotations(FlowAnnotation.class);
			if (flowAnnotations != null) {

				// Sort the flows by index
				List<FlowAnnotation> flowList = Arrays.asList(flowAnnotations);
				Collections.sort(flowList, (a, b) -> a.getFlowIndex() - b.getFlowIndex());

				// Link flows for the function
				for (FlowAnnotation flow : flowList) {

					// Obtain the function flow
					FunctionFlow functionFlow = function.getFunctionFlow(flow.getFlowName());

					// Link the function flow
					this.linkFunctionFlow(functionFlow, functionType, flow, designer, functionsByName, sectionOutputs);
				}
			}

			// Obtain the flow meta-data for the function
			SectionInterfaceAnnotation[] sectionAnnotations = functionType
					.getAnnotations(SectionInterfaceAnnotation.class);
			if (sectionAnnotations != null) {

				// Sort the flows by index
				List<SectionInterfaceAnnotation> flowList = Arrays.asList(sectionAnnotations);
				Collections.sort(flowList, (a, b) -> a.getFlowIndex() - b.getFlowIndex());

				// Link flows for the function
				for (SectionInterfaceAnnotation flow : flowList) {

					// Obtain the function flow
					FunctionFlow functionFlow = function.getFunctionFlow(flow.getFlowName());

					// Link the function flow
					this.linkFunctionFlow(functionFlow, functionType, flow, designer, context, subSectionsByName,
							functionsByName, sectionObjects, sectionManagedObjectTypes);
				}
			}

			// Link escalations for the function
			for (ManagedFunctionEscalationType escalationType : functionType.getEscalationTypes()) {

				// Obtain function handling escalation (if available)
				String escalationTypeName = escalationType.getEscalationType().getName();
				SectionFunction escalationHandler = functionsByParameterType.get(escalationTypeName);

				// Link escalation
				this.linkFunctionEscalation(function, functionType, escalationType, escalationHandler, designer,
						sectionOutputs);
			}

			// Obtain the object index for the parameter
			Integer parameterIndex = parameterIndexes.get(functionTypeName);

			// Obtain the object types
			ManagedFunctionObjectType<?>[] objectTypes = functionType.getObjectTypes();

			// Determine first object is the section object
			int objectIndex = 0;
			if (sectionObject != null) {
				ManagedFunctionObjectType<?> sectionClassObject = objectTypes[objectIndex++];
				FunctionObject objectSection = function.getFunctionObject(sectionClassObject.getObjectName());
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
				this.linkFunctionObject(function, functionType, objectType, designer, context, sectionObjects,
						sectionManagedObjectTypes);
			}
		}

		// Link managed object dependencies
		for (String moName : sectionManagedObjectTypes.keySet()) {

			// Obtain the section managed object and its type
			SectionManagedObject mo = (SectionManagedObject) sectionObjects.get(moName);
			ManagedObjectType<?> moType = sectionManagedObjectTypes.get(moName);

			// Link the dependencies
			for (ManagedObjectDependencyType<?> moDependencyType : moType.getDependencyTypes()) {

				// Obtain the dependency
				SectionManagedObjectDependency moDependency = mo
						.getSectionManagedObjectDependency(moDependencyType.getDependencyName());

				// Link to its implementing dependency
				SectionDependencyObjectNode dependency = this.getDependencyObject(moDependencyType.getTypeQualifier(),
						moDependencyType.getDependencyType().getName(), designer, context, sectionObjects,
						sectionManagedObjectTypes, null);
				designer.link(moDependency, dependency);
			}
		}
	}

}