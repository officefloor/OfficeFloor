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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.method.AbstractFunctionManagedFunctionSource;
import net.officefloor.plugin.clazz.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturer;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerContext;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.flow.ClassSectionSubSectionOutputLink;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturer;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerContext;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.object.ClassSectionTypeQualifier;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogator;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorContext;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorServiceFactory;
import net.officefloor.plugin.section.clazz.spawn.ClassSectionFlowSpawnInterrogator;
import net.officefloor.plugin.section.clazz.spawn.ClassSectionFlowSpawnInterrogatorContext;
import net.officefloor.plugin.section.clazz.spawn.ClassSectionFlowSpawnInterrogatorServiceFactory;

/**
 * Abstract {@link Class} {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractClassSectionSource extends AbstractSectionSource {

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
	 * Indicates if include {@link ManagedFunctionType}.
	 * 
	 * @param functionType {@link ManagedFunctionType}.
	 * @return <code>true</code> to include the {@link ManagedFunctionType}.
	 */
	protected boolean isIncludeFunction(ManagedFunctionType<?, ?> functionType) {
		return true;
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
	 * Link the {@link SectionInput} for the {@link SectionFunction}.
	 * 
	 * @param function      {@link SectionFunction}.
	 * @param functionType  {@link ManagedFunctionType} for the
	 *                      {@link SectionFunction}.
	 * @param parameterType Parameter type for the {@link SectionFunction}. May be
	 *                      <code>null</code> if no parameter.
	 * @param designer      {@link SectionDesigner}.
	 */
	protected void linkInput(SectionFunction function, ManagedFunctionType<?, ?> functionType, Class<?> parameterType,
			SectionDesigner designer) {

		// Obtain the input name
		String inputName = this.getFunctionName(functionType);

		// Obtain the parameter type name
		String parameterTypeName = (parameterType == null ? null : parameterType.getName());

		// Add input for function
		SectionInput sectionInput = designer.addSectionInput(inputName, parameterTypeName);
		designer.link(sectionInput, function);
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

		// Create the dependency/flow contexts
		ClassSectionFlowManufacturerContextImpl flowContext = new ClassSectionFlowManufacturerContextImpl(designer,
				context);
		ClassSectionObjectManufacturerContextImpl objectContext = new ClassSectionObjectManufacturerContextImpl(
				designer, context);

		// Load in the section object dependencies
		for (ManagedObjectDependencyType<?> dependencyType : sectionObjectType.getDependencyTypes()) {

			// Obtain the required dependency
			SectionManagedObjectDependency requiredDependency = sectionObject
					.getSectionManagedObjectDependency(dependencyType.getDependencyName());

			// Obtain the dependent object
			String dependencyTypeQualifier = dependencyType.getTypeQualifier();
			String dependencyTypeName = dependencyType.getDependencyType().getName();
			SectionDependencyObjectNode dependencyObject = objectContext.getDependency(dependencyTypeQualifier,
					dependencyTypeName, dependencyType);

			// Link dependency (if available)
			if (dependencyObject != null) {
				designer.link(requiredDependency, dependencyObject);
			}
		}

		// Load the functions
		PropertyList functionProperties = context.createPropertyList();
		functionProperties.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(sectionClass.getName());
		flowContext.addFunctionNamespace(SectionClassManagedFunctionSource.class.getName(), functionProperties);

		// Link functions
		for (SectionClassFunction sectionFunction : flowContext.sectionFunctions.values()) {

			// Obtain the function details
			SectionFunction function = sectionFunction.managedFunction;
			ManagedFunctionType<?, ?> functionType = sectionFunction.managedFunctionType;
			Class<?> parameterType = sectionFunction.parameterType;
			int parameterIndex = sectionFunction.parameterIndex;

			// Provide section input to function
			this.linkInput(function, functionType, parameterType, designer);

			// Link the next (if available)
			SectionFlowSinkNode nextSink = flowContext.getOptionalFlowSink(functionType);
			if (nextSink != null) {
				designer.link(function, nextSink);
			}

			// Link the function flows
			for (ManagedFunctionFlowType<?> functionFlowType : functionType.getFlowTypes()) {

				// Obtain the function flow
				String flowName = functionFlowType.getFlowName();
				FunctionFlow functionFlow = function.getFunctionFlow(flowName);

				// Determine if spawn flow
				boolean isSpawn = false;
				ClassSectionFlowSpawnInterrogatorContext spawnContext = new ClassSectionFlowSpawnInterrogatorContext() {

					@Override
					public ManagedFunctionFlowType<?> getManagedFunctionFlowType() {
						return functionFlowType;
					}

					@Override
					public SectionSourceContext getSourceContext() {
						return context;
					}
				};
				CHECK_SPAWN: for (ClassSectionFlowSpawnInterrogator interrogator : context
						.loadOptionalServices(ClassSectionFlowSpawnInterrogatorServiceFactory.class)) {
					try {
						if (interrogator.isSpawnFlow(spawnContext)) {
							isSpawn = true;
							break CHECK_SPAWN;
						}
					} catch (Exception ex) {
						throw designer.addIssue("Failed to determine if spawn flow", ex);
					}

				}

				// Obtain the flow sink
				Class<?> flowArgumentType = functionFlowType.getArgumentType();
				SectionFlowSinkNode flowSink = flowContext.getFlowSink(flowName,
						flowArgumentType != null ? flowArgumentType.getName() : null, functionFlowType);
				designer.link(functionFlow, flowSink, isSpawn);
			}

			// Link escalations for the function
			for (ManagedFunctionEscalationType functionEscalationType : functionType.getEscalationTypes()) {

				// Obtain the function escalation
				FunctionFlow functionEscalation = function
						.getFunctionEscalation(functionEscalationType.getEscalationName());

				// Link escalation
				Class<?> escalationType = functionEscalationType.getEscalationType();
				SectionFlowSinkNode escalationHandler = flowContext.getEscalationSink(function, escalationType);
				designer.link(functionEscalation, escalationHandler, false);
			}

			// Determine first object is the section object
			ManagedFunctionObjectType<?>[] functionObjectTypes = functionType.getObjectTypes();
			int objectIndex = 0;
			if (sectionObject != null) {
				ManagedFunctionObjectType<?> sectionClassObject = functionObjectTypes[objectIndex++];
				FunctionObject objectSection = function.getFunctionObject(sectionClassObject.getObjectName());
				designer.link(objectSection, sectionObject);
			}

			// Link the function dependencies
			NEXT_FUNCTION_OBJECT: for (int i = objectIndex; i < functionObjectTypes.length; i++) {
				ManagedFunctionObjectType<?> functionObjectType = functionObjectTypes[i];

				// Obtain the function object
				FunctionObject functionObject = function.getFunctionObject(functionObjectType.getObjectName());

				// Ignore parameter
				if (parameterIndex == i) {
					functionObject.flagAsParameter();
					continue NEXT_FUNCTION_OBJECT;
				}

				// Obtain the dependency
				String objectQualifier = functionObjectType.getTypeQualifier();
				String objectTypeName = functionObjectType.getObjectType().getName();
				SectionDependencyObjectNode objectDependency = objectContext.getDependency(objectQualifier,
						objectTypeName, functionObjectType);

				// Link dependency (if available)
				if (objectDependency != null) {
					designer.link(functionObject, objectDependency);
				}
			}
		}

		// Link sub section outputs and objects
		for (SectionClassSubSection subSectionStruct : flowContext.subSections.values()) {

			// Obtain the sub section details
			SubSection subSection = subSectionStruct.subSection;
			SectionType sectionType = subSectionStruct.sectionType;
			Map<String, String> outputsToLinks = subSectionStruct.outputsToLinks;

			// Link outputs of sub section
			NEXT_OUTPUT: for (SectionOutputType outputType : sectionType.getSectionOutputTypes()) {

				// Obtain the name of output
				String outputName = outputType.getSectionOutputName();

				// Determine if escalation only
				if (outputType.isEscalationOnly()) {

					// Obtain the escalation output
					SubSectionOutput escalation = subSection.getSubSectionOutput(outputName);

					// Obtain the escalation handler
					String escalationTypeName = outputType.getArgumentType();
					Class<?> escalationType = context.loadClass(escalationTypeName);
					SectionFlowSinkNode handler = flowContext.getEscalationSink(null, escalationType);

					// Link escalation handling
					designer.link(escalation, handler);
					continue NEXT_OUTPUT;
				}

				// Obtain the link name
				String linkName = outputsToLinks.get(outputName);
				if (linkName == null) {
					// Not configured, so use output name
					linkName = outputName;
				} else {
					// Remove so can determine extra configuration
					outputsToLinks.remove(outputName);
				}

				// Obtain the section output
				SubSectionOutput output = subSection.getSubSectionOutput(outputName);

				// Obtain the flow sink
				String argumentTypeName = outputType.getArgumentType();
				SectionFlowSinkNode flowSink = flowContext.getFlowSink(linkName, argumentTypeName, outputType);

				// Link
				designer.link(output, flowSink);
			}

			// Ensure no additional link configuration
			if (outputsToLinks.size() > 0) {
				throw designer.addIssue(SectionOutput.class.getSimpleName() + " instances do not exist on "
						+ SubSection.class.getSimpleName() + ": " + outputsToLinks);
			}

			// Link objects of sub section
			for (SectionObjectType subSectionObjectType : sectionType.getSectionObjectTypes()) {

				// Obtain the sub section object
				String objectName = subSectionObjectType.getSectionObjectName();
				SubSectionObject subSectionObject = subSection.getSubSectionObject(objectName);

				// Obtain the dependency
				String objectTypeQualifier = subSectionObjectType.getTypeQualifier();
				String objectTypeName = subSectionObjectType.getObjectType();
				SectionDependencyObjectNode dependency = objectContext.getDependency(objectTypeQualifier,
						objectTypeName, subSectionObjectType);

				// Link to dependency
				designer.link(subSectionObject, dependency);
			}
		}

		// Link managed object dependencies
		for (SectionClassManagedObject sectionMo : objectContext.sectionManagedObjects.values()) {

			// Link the dependencies
			for (ManagedObjectDependencyType<?> moDependencyType : sectionMo.managedObjectType.getDependencyTypes()) {

				// Obtain the dependency
				SectionManagedObjectDependency moDependency = sectionMo.managedObject
						.getSectionManagedObjectDependency(moDependencyType.getDependencyName());

				// Link to its implementing dependency
				String dependencyQualifier = moDependencyType.getTypeQualifier();
				String dependencyTypeName = moDependencyType.getDependencyType().getName();
				SectionDependencyObjectNode dependency = objectContext.getDependency(dependencyQualifier,
						dependencyTypeName, moDependencyType);
				designer.link(moDependency, dependency);
			}
		}
	}

	/**
	 * {@link ManagedFunctionSource} implementation to provide the
	 * {@link ManagedFunction} instances for the {@link ClassSectionSource}.
	 */
	@PrivateSource
	public static class SectionClassManagedFunctionSource extends AbstractFunctionManagedFunctionSource {

		@Override
		protected ManagedFunctionTypeBuilder<Indexed, Indexed> buildMethod(Class<?> clazz, Method method,
				MethodManagedFunctionBuilder managedFunctionBuilder) throws Exception {

			// Build the method (using section object)
			ManagedFunctionTypeBuilder<Indexed, Indexed> function = managedFunctionBuilder.buildMethod(method,
					(context) -> {

						// Create the class dependency factory for section object
						ClassDependencyFactory dependencyFactory = context.getClassDependencies()
								.createClassDependencyFactory(ClassSectionSource.CLASS_OBJECT_NAME, clazz, null);

						// Create factory to return section object
						return (managedFunctionContext) -> dependencyFactory.createDependency(managedFunctionContext);
					});

			// Return the function
			return function;
		}
	}

	/**
	 * {@link ClassSectionFlowManufacturerContext} implementation.
	 */
	private class ClassSectionFlowManufacturerContextImpl implements ClassSectionFlowManufacturerContext {

		/**
		 * {@link SectionDesigner}.
		 */
		private final SectionDesigner designer;

		/**
		 * {@link SectionSourceContext}.
		 */
		private final SectionSourceContext context;

		/**
		 * {@link ClassSectionParameterInterrogator}.
		 */
		private final ClassSectionParameterInterrogatorContextImpl parameterContext;

		/**
		 * {@link SectionFunctionNamespace} instances by their {@link SourceKey}.
		 */
		private final Map<SourceKey, SectionFunctionNamespace> sectionFunctionNamespaces = new HashMap<>();

		/**
		 * {@link SectionClassSubSection} instances by their {@link SourceKey}.
		 */
		private final Map<SourceKey, SectionClassSubSection> subSections = new HashMap<>();

		/**
		 * {@link SectionClassFunction} instances by their {@link SectionFunction} name.
		 */
		private final Map<String, SectionClassFunction> sectionFunctions = new HashMap<>();

		/**
		 * {@link SectionOutput} instances by name.
		 */
		private final Map<String, SectionOutput> sectionOutputs = new HashMap<>();

		/**
		 * {@link Escalation} handling {@link SectionFlowSinkNode} instances.
		 */
		private final Map<Class<?>, SectionFlowSinkNode> escalationHandlers = new HashMap<>();

		/**
		 * {@link AnnotatedType}.
		 */
		private AnnotatedType annotatedType = null;

		/**
		 * Instantiate.
		 * 
		 * @param designer {@link SectionDesigner}.
		 * @param context  {@link SectionSourceContext}.
		 */
		private ClassSectionFlowManufacturerContextImpl(SectionDesigner designer, SectionSourceContext context) {
			this.designer = designer;
			this.context = context;
			this.parameterContext = new ClassSectionParameterInterrogatorContextImpl(context);
		}

		/**
		 * Obtains the optional {@link SectionFlowSinkNode}.
		 * 
		 * @param annotatedType {@link AnnotatedType}.
		 * @return {@link SectionFlowSinkNode} or <code>null</code>.
		 * @throws Exception If fails to obtain.
		 */
		private SectionFlowSinkNode getOptionalFlowSink(AnnotatedType annotatedType) throws Exception {

			// Attempt to obtain flow sink
			this.annotatedType = annotatedType;
			for (ClassSectionFlowManufacturer manufacturer : this.context
					.loadOptionalServices(ClassSectionFlowManufacturerServiceFactory.class)) {

				// Obtain the flow sink
				SectionFlowSinkNode flowSink = manufacturer.createFlowSink(this);
				if (flowSink != null) {
					return flowSink; // have flow sink
				}
			}

			// As here, no plugin flow sink
			return null;
		}

		/**
		 * Obtain the {@link SectionFlowSinkNode}.
		 * 
		 * @param flowName      Name of {@link Flow}.
		 * @param argumentType  Type of argument. May be <code>null</code> for no
		 *                      argument.
		 * @param annotatedType {@link AnnotatedType}.
		 * @return {@link SectionFlowSinkNode}.
		 * @throws Exception If fails to obtain.
		 */
		private SectionFlowSinkNode getFlowSink(String flowName, String argumentType, AnnotatedType annotatedType)
				throws Exception {

			// Determine if function by name
			SectionClassFunction function = this.sectionFunctions.get(flowName);
			if (function != null) {
				return function.managedFunction; // sink to function
			}

			// Determine if section output
			SectionOutput sectionOutput = this.sectionOutputs.get(flowName);
			if (sectionOutput != null) {
				return sectionOutput; // sink to output
			}

			// Obtain via plugin
			if (annotatedType != null) {
				SectionFlowSinkNode flowSink = this.getOptionalFlowSink(annotatedType);
				if (flowSink != null) {
					return flowSink;
				}
			}

			// Link to created section output
			sectionOutput = this.designer.addSectionOutput(flowName, argumentType, false);
			this.sectionOutputs.put(flowName, sectionOutput);
			return sectionOutput;
		}

		/**
		 * Obtains the {@link SectionFlowSinkNode} for the {@link Escalation}.
		 * 
		 * @param escalatingFunction {@link SectionFunction} potentially escalating the
		 *                           {@link Escalation}.
		 * @param escalationType     {@link Escalation} type.
		 * @return {@link SectionFlowSinkNode} for the {@link Escalation}.
		 */
		private SectionFlowSinkNode getEscalationSink(SectionFunction escalatingFunction, Class<?> escalationType) {

			// Try to find closest match
			SectionFlowSinkNode handler = null;
			int minDistance = Integer.MAX_VALUE;
			NEXT_HANDLER: for (Class<?> handlingType : this.escalationHandlers.keySet()) {

				// Obtain the handler
				SectionFlowSinkNode checkHandler = this.escalationHandlers.get(handlingType);
				if (checkHandler == escalatingFunction) {
					continue NEXT_HANDLER; // can not handle own exception (potential infinite loop)
				}

				// Find the distance to escalation
				Class<?> parentHandlingType = handlingType.getSuperclass();
				int distance = 1;
				while ((parentHandlingType != null) && (!parentHandlingType.isAssignableFrom(escalationType))) {
					parentHandlingType = parentHandlingType.getSuperclass();
					distance++;
				}

				// Replace handler if closer distance
				if ((parentHandlingType != null) && (distance < minDistance)) {
					handler = this.escalationHandlers.get(handlingType);
					minDistance = distance;
				}
			}
			if (handler != null) {
				return handler; // found generic handler
			}

			// No handler available, so create section escalation
			handler = this.designer.addSectionOutput(escalationType.getName(), escalationType.getName(), true);
			this.escalationHandlers.put(escalationType, handler);
			return handler;
		}

		/*
		 * ==================== ClassSectionFlowManufacturerContext ====================
		 */

		@Override
		public AnnotatedType getAnnotatedType() {
			return this.annotatedType;
		}

		@Override
		public void addFunctionNamespace(String managedFunctionSourceClassName, PropertyList properties) {

			// Determine if already registered
			SourceKey sourceKey = new SourceKey(managedFunctionSourceClassName, properties);
			if (this.sectionFunctionNamespaces.containsKey(sourceKey)) {
				return; // already registered function namespaces
			}

			// Load the namespace type for the class
			int namespaceIndex = this.sectionFunctionNamespaces.size();
			String namespaceName = "NAMESPACE" + (namespaceIndex == 0 ? "" : "-" + String.valueOf(namespaceIndex));
			FunctionNamespaceType namespaceType = context.loadManagedFunctionType(namespaceName,
					SectionClassManagedFunctionSource.class.getName(), properties);

			// Add the namespace
			SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(namespaceName,
					SectionClassManagedFunctionSource.class.getName());
			properties.configureProperties(namespace);

			// Register the namespace
			this.sectionFunctionNamespaces.put(sourceKey, namespace);

			// Load functions
			NEXT_FUNCTION: for (ManagedFunctionType<?, ?> functionType : namespaceType.getManagedFunctionTypes()) {

				// Determine if include function
				if (!AbstractClassSectionSource.this.isIncludeFunction(functionType)) {
					continue NEXT_FUNCTION;
				}

				// Obtain the function name
				String functionName = AbstractClassSectionSource.this.getFunctionName(functionType);

				// Add function
				String functionTypeName = functionType.getFunctionName();
				SectionFunction function = namespace.addSectionFunction(functionName, functionTypeName);

				// Interrogate for the parameter
				Class<?> parameterType = null;
				int parameterIndex = -1;
				ManagedFunctionObjectType<?>[] functionObjectTypes = functionType.getObjectTypes();
				for (ClassSectionParameterInterrogator interrogator : this.context
						.loadOptionalServices(ClassSectionParameterInterrogatorServiceFactory.class)) {
					for (int i = 0; i < functionObjectTypes.length; i++) {
						ManagedFunctionObjectType<?> functionObjectType = functionObjectTypes[i];

						// Determine if parameter
						boolean isParameter;
						try {
							isParameter = this.parameterContext.isParameter(functionObjectType, interrogator);
						} catch (Exception ex) {
							throw this.designer.addIssue("Failed to determine if parameter", ex);
						}
						if (isParameter) {

							// Ensure parameter not already exists for function
							if (parameterType != null) {
								throw this.designer
										.addIssue("Function " + functionTypeName + " has more than one parameter");
							}

							// Found the parameter
							parameterIndex = i;
							parameterType = functionObjectType.getObjectType();
						}
					}
				}

				// Register the section function
				// (note: under type name to avoid name changes not obvious in code)
				this.sectionFunctions.put(functionTypeName,
						new SectionClassFunction(functionType, function, parameterType, parameterIndex));

				// Register as potential escalation handler
				if ((parameterType != null) && (Throwable.class.isAssignableFrom(parameterType))) {
					this.escalationHandlers.put(parameterType, function);
				}
			}
		}

		@Override
		public SectionFunction getFunction(String functionName) {
			SectionClassFunction function = this.sectionFunctions.get(functionName);
			return function != null ? function.managedFunction : null;
		}

		@Override
		public ClassSectionSubSectionOutputLink createSubSectionOutputLink(String subSectionOutputName,
				String linkName) {
			return new ClassSectionSubSectionOutputLinkImpl(subSectionOutputName, linkName);
		}

		@Override
		public SubSection getOrCreateSubSection(String sectionSourceClassName, String sectionLocation,
				PropertyList properties, ClassSectionSubSectionOutputLink... configuredLinks) {

			// Determine if already registered
			SourceKey sourceKey = new SourceKey(sectionSourceClassName, sectionLocation, properties);
			SectionClassSubSection subSectionStruct = this.subSections.get(sourceKey);
			if (subSectionStruct != null) {
				return subSectionStruct.subSection; // already registered
			}

			// Load the section type
			int nameIndex = this.subSections.size();
			String subSectionName = "SUB-SECTION-" + nameIndex;
			SectionType sectionType = context.loadSectionType(subSectionName, sectionSourceClassName, sectionLocation,
					properties);

			// Add the sub section
			SubSection subSection = this.designer.addSubSection(subSectionName, sectionSourceClassName,
					sectionLocation);
			properties.configureProperties(subSection);

			// Capture the output links
			Map<String, String> outputsToLinks = new HashMap<>();
			for (ClassSectionSubSectionOutputLink link : configuredLinks) {
				outputsToLinks.put(link.getSubSectionOutputName(), link.getLinkName());
			}

			// Register the sub section
			this.subSections.put(sourceKey, new SectionClassSubSection(sectionType, subSection, outputsToLinks));

			// Return the sub section
			return subSection;
		}

		@Override
		public SectionSourceContext getSourceContext() {
			return this.context;
		}

		@Override
		public SectionFlowSinkNode getFlowSink(String flowName, String argumentType) {
			try {
				return this.getFlowSink(flowName, argumentType, null);
			} catch (Exception ex) {
				throw this.designer.addIssue(
						"Failed to obtain " + SectionFlowSinkNode.class.getSimpleName() + " for flow " + flowName, ex);
			}
		}
	}

	/**
	 * {@link ClassSectionSubSectionOutputLink} implementation.
	 */
	private static class ClassSectionSubSectionOutputLinkImpl implements ClassSectionSubSectionOutputLink {

		/**
		 * {@link SectionOutput} name.
		 */
		private final String subSectionOutputName;

		/**
		 * Name of {@link SectionFlowSinkNode}.
		 */
		private final String linkName;

		/**
		 * Instantiate.
		 * 
		 * @param subSectionOutputName {@link SectionOutput} name.
		 * @param linkName             Name of {@link SectionFlowSinkNode}.
		 */
		private ClassSectionSubSectionOutputLinkImpl(String subSectionOutputName, String linkName) {
			this.subSectionOutputName = subSectionOutputName;
			this.linkName = linkName;
		}

		/*
		 * ==================== ClassSectionSubSectionOutputLink ===================
		 */

		@Override
		public String getSubSectionOutputName() {
			return this.subSectionOutputName;
		}

		@Override
		public String getLinkName() {
			return this.linkName;
		}
	}

	/**
	 * Associates the {@link ManagedFunctionType}, {@link SectionFunction} and its
	 * parameter.
	 */
	private static class SectionClassFunction {

		/**
		 * {@link ManagedFunctionType}.
		 */
		private final ManagedFunctionType<?, ?> managedFunctionType;

		/**
		 * {@link SectionFunction}.
		 */
		private final SectionFunction managedFunction;

		/**
		 * Parameter type. May be <code>null</code>.
		 */
		private final Class<?> parameterType;

		/**
		 * Index of the parameter.
		 */
		private final int parameterIndex;

		/**
		 * Instantiate.
		 * 
		 * @param managedFunctionType {@link ManagedFunctionType}.
		 * @param managedFunction     {@link SectionFunction}.
		 * @param parameterType       Parameter type. May be <code>null</code>.
		 * @param parameterIndex      Index of the parameter.
		 */
		private SectionClassFunction(ManagedFunctionType<?, ?> managedFunctionType, SectionFunction managedFunction,
				Class<?> parameterType, int parameterIndex) {
			this.managedFunctionType = managedFunctionType;
			this.managedFunction = managedFunction;
			this.parameterType = parameterType;
			this.parameterIndex = parameterIndex;
		}
	}

	/**
	 * Associates the {@link SectionType} and {@link SubSection}.
	 */
	private static class SectionClassSubSection {

		/**
		 * {@link SectionType}.
		 */
		private final SectionType sectionType;

		/**
		 * {@link SubSection}.
		 */
		private final SubSection subSection;

		/**
		 * {@link SectionOutput} name to {@link SectionFlowSinkNode} name.
		 */
		private final Map<String, String> outputsToLinks;

		/**
		 * Instantiate.
		 * 
		 * @param sectionType    {@link SectionType}.
		 * @param subSection     {@link SubSection}.
		 * @param outputsToLinks {@link SectionOutput} name to
		 *                       {@link SectionFlowSinkNode} name.
		 */
		private SectionClassSubSection(SectionType sectionType, SubSection subSection,
				Map<String, String> outputsToLinks) {
			this.sectionType = sectionType;
			this.subSection = subSection;
			this.outputsToLinks = outputsToLinks;
		}
	}

	/**
	 * {@link ClassSectionParameterInterrogatorContext} implementation.
	 */
	private class ClassSectionParameterInterrogatorContextImpl implements ClassSectionParameterInterrogatorContext {

		/**
		 * {@link SectionSourceContext}.
		 */
		private final SectionSourceContext context;

		/**
		 * {@link ManagedFunctionObjectType}.
		 */
		private ManagedFunctionObjectType<?> functionObject = null;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link SectionSourceContext}.
		 */
		private ClassSectionParameterInterrogatorContextImpl(SectionSourceContext context) {
			this.context = context;
		}

		/**
		 * Indicates if parameter.
		 * 
		 * @param functionObject {@link ManagedFunctionObjectType}.
		 * @param interrogator   {@link ClassSectionParameterInterrogator}.
		 * @return <code>true</code> if parameter.
		 * @throws Exception If fails to determine if parameter.
		 */
		private boolean isParameter(ManagedFunctionObjectType<?> functionObject,
				ClassSectionParameterInterrogator interrogator) throws Exception {
			this.functionObject = functionObject;
			return interrogator.isParameter(this);
		}

		/*
		 * ================ ClassSectionParameterInterrogatorContext ================
		 */

		@Override
		public ManagedFunctionObjectType<?> getManagedFunctionObjectType() {
			return this.functionObject;
		}

		@Override
		public SectionSourceContext getSourceContext() {
			return this.context;
		}
	}

	/**
	 * {@link ClassSectionObjectManufacturerContext} implementation.
	 */
	private class ClassSectionObjectManufacturerContextImpl implements ClassSectionObjectManufacturerContext {

		/**
		 * {@link SectionDesigner}.
		 */
		private final SectionDesigner designer;

		/**
		 * {@link SectionSourceContext}.
		 */
		private final SectionSourceContext context;

		/**
		 * {@link SectionDependencyObjectNode} instances by their name.
		 */
		private final Map<String, SectionDependencyObjectNode> sectionObjects = new HashMap<>();

		/**
		 * {@link SectionClassManagedObject} instances by their {@link SourceKey}.
		 */
		private final Map<SourceKey, SectionClassManagedObject> sectionManagedObjects = new HashMap<>();

		/**
		 * {@link AnnotatedType}.
		 */
		private AnnotatedType annotatedType = null;

		/**
		 * Indicates if augmented.
		 */
		private boolean isAugmented = false;

		/**
		 * Instantiate.
		 * 
		 * @param designer {@link SectionDesigner}.
		 * @param context  {@link SectionSourceContext}.
		 */
		private ClassSectionObjectManufacturerContextImpl(SectionDesigner designer, SectionSourceContext context) {
			this.designer = designer;
			this.context = context;
		}

		/**
		 * Obtains the dependency for the {@link AnnotatedType}.
		 * 
		 * 
		 * @param qualifier     {@link Qualifier} for the
		 *                      {@link SectionDependencyObjectNode}. If not
		 *                      {@link Qualifier} should be the same as the type name.
		 * @param typeName      Fully qualified type name of the
		 *                      {@link SectionDependencyObjectNode}.
		 * @param annotatedType {@link AnnotatedType} requiring a dependency.
		 * @return {@link SectionDependencyObjectNode} for the {@link AnnotatedType}.
		 * @throws Exception If fails to get {@link SectionDependencyObjectNode}.
		 */
		private SectionDependencyObjectNode getDependency(String qualifier, String typeName,
				AnnotatedType annotatedType) throws Exception {

			// Determine if existing object for dependency
			String objectName = AbstractClassSectionSource.this.getSectionObjectName(qualifier, typeName);
			SectionDependencyObjectNode sectionObjectNode = sectionObjects.get(objectName);
			if (sectionObjectNode != null) {
				return sectionObjectNode; // have existing dependency
			}

			// Attempt to load the section dependency via plugin
			this.annotatedType = annotatedType;
			this.isAugmented = false;
			for (ClassSectionObjectManufacturer manufacturer : this.context
					.loadOptionalServices(ClassSectionObjectManufacturerServiceFactory.class)) {

				// Attempt to manufacture dependency
				SectionDependencyObjectNode dependency = manufacturer.createObject(this);
				if (this.isAugmented) {
					return null; // will be augmented
				}
				if (dependency != null) {
					return dependency; // found via plugin
				}
			}

			// No plugin dependency, so fall back to section object
			SectionObject sectionObject = designer.addSectionObject(objectName, typeName);
			if (AbstractClassSectionSource.this.isQualifier(qualifier)) {
				sectionObject.setTypeQualifier(qualifier);
			}
			sectionObjects.put(objectName, sectionObject);

			// Return the section object
			return sectionObject;
		}

		/*
		 * ====================== ClassSectionObjectContext ===========================
		 */

		@Override
		public void flagAugmented() {
			this.isAugmented = true;
		}

		@Override
		public ClassSectionTypeQualifier createTypeQualifier(String qualifier, Class<?> type) {
			return new ClassSectionTypeQualifierImpl(qualifier, type);
		}

		@Override
		public SectionManagedObject getOrCreateManagedObject(String managedObjectSourceClassName,
				PropertyList properties, ClassSectionTypeQualifier... typeQualifiers) {

			// Determine if already have managed object
			SourceKey sourceKey = new SourceKey(managedObjectSourceClassName, properties);
			SectionClassManagedObject existing = this.sectionManagedObjects.get(sourceKey);
			if (existing != null) {
				return existing.managedObject;
			}

			// Not existing, so load the managed object type
			ManagedObjectType<?> moType = this.context.loadManagedObjectType("MO", managedObjectSourceClassName,
					properties);

			// Obtain the dependency information
			Class<?> objectType = moType.getObjectType();

			// Derive the object name
			ClassSectionTypeQualifier namingTypeQualifier = typeQualifiers.length > 0 ? typeQualifiers[0]
					: this.createTypeQualifier(null, objectType);
			String moName = AbstractClassSectionSource.this.getSectionObjectName(namingTypeQualifier.getQualifier(),
					namingTypeQualifier.getType().getName());

			// Add the managed object
			SectionManagedObjectSource mos = this.designer.addSectionManagedObjectSource(moName,
					managedObjectSourceClassName);
			properties.configureProperties(mos);
			SectionManagedObject mo = mos.addSectionManagedObject(moName, ManagedObjectScope.PROCESS);
			for (ClassSectionTypeQualifier typeQualifier : typeQualifiers) {
				mo.addTypeQualification(typeQualifier.getQualifier(), typeQualifier.getType().getName());
			}

			// Register the managed object to link dependencies
			this.sectionManagedObjects.put(sourceKey, new SectionClassManagedObject(moType, mo));

			// Register the managed object for dependencies
			if (typeQualifiers.length == 0) {
				// Just register under name
				this.sectionObjects.put(moName, mo);

			} else {
				// Register under all type qualifications
				for (ClassSectionTypeQualifier typeQualifier : typeQualifiers) {
					String qualifiedName = AbstractClassSectionSource.this
							.getSectionObjectName(typeQualifier.getQualifier(), typeQualifier.getType().getName());
					this.sectionObjects.put(qualifiedName, mo);
				}
			}

			// Return the managed object
			return mo;
		}

		@Override
		public AnnotatedType getAnnotatedType() {
			return this.annotatedType;
		}

		@Override
		public SectionSourceContext getSourceContext() {
			return this.context;
		}
	}

	/**
	 * Associates the {@link ManagedObjectType} and {@link SectionManagedObject}.
	 */
	private static class SectionClassManagedObject {

		/**
		 * {@link ManagedObjectType}.
		 */
		private final ManagedObjectType<?> managedObjectType;

		/**
		 * {@link SectionManagedObject}.
		 */
		private final SectionManagedObject managedObject;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectType {@link ManagedObjectType}.
		 * @param managedObject     {@link SectionManagedObject}.
		 */
		private SectionClassManagedObject(ManagedObjectType<?> managedObjectType, SectionManagedObject managedObject) {
			this.managedObjectType = managedObjectType;
			this.managedObject = managedObject;
		}
	}

	/**
	 * {@link ClassSectionTypeQualifier} implementation.
	 */
	private static class ClassSectionTypeQualifierImpl implements ClassSectionTypeQualifier {

		/**
		 * Qualifier. May be <code>null</code>.
		 */
		private final String qualifier;

		/**
		 * Type.
		 */
		private final Class<?> type;

		/**
		 * Instantiate.
		 * 
		 * @param qualifier Qualifier. May be <code>null</code>.
		 * @param type      Type.
		 */
		private ClassSectionTypeQualifierImpl(String qualifier, Class<?> type) {
			this.qualifier = qualifier;
			this.type = type;
		}

		/*
		 * ===================== ClassSectionTypeQualifier =====================
		 */

		@Override
		public String getQualifier() {
			return this.qualifier;
		}

		@Override
		public Class<?> getType() {
			return this.type;
		}
	}

	/**
	 * {@link Map} key to find source.
	 */
	private static class SourceKey {

		/**
		 * Source {@link Class} name.
		 */
		private final String sourceClassName;

		/**
		 * Optional location.
		 */
		private final String location;

		/**
		 * {@link PropertyList} name/value pairs.
		 */
		private final Map<String, String> properties = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param sourceClassName Source {@link Class} name.
		 * @param location        Optional location.
		 * @param properties      {@link PropertyList}.
		 */
		private SourceKey(String sourceClassName, String location, PropertyList properties) {
			this.sourceClassName = sourceClassName;
			this.location = location != null ? location : "";
			if (properties != null) {
				for (Property property : properties) {
					this.properties.put(property.getName(), property.getValue());
				}
			}
		}

		/**
		 * Instantiate.
		 * 
		 * @param sourceClassName Source {@link Class} name.
		 * @param properties      {@link PropertyList}.
		 */
		private SourceKey(String sourceClassName, PropertyList properties) {
			this(sourceClassName, null, properties);
		}

		/*
		 * ========================== Object ==========================
		 */

		@Override
		public int hashCode() {
			return Objects.hash(this.sourceClassName, this.location, this.properties);
		}

		@Override
		public boolean equals(Object obj) {

			// Ensure same type
			if (!(obj instanceof SourceKey)) {
				return false;
			}
			SourceKey that = (SourceKey) obj;

			// Match if values equal
			return Objects.equals(this.sourceClassName, that.sourceClassName)
					&& Objects.equals(this.location, that.location) && Objects.equals(this.properties, that.properties);
		}
	}

}