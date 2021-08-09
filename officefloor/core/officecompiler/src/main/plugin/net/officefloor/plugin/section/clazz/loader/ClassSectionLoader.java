/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.plugin.section.clazz.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
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
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowContext;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturer;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerContext;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.flow.ClassSectionSubSectionOutputLink;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectContext;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturer;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerContext;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.object.ClassSectionTypeQualifier;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogation;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogator;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorServiceFactory;
import net.officefloor.plugin.section.clazz.spawn.ClassSectionFlowSpawnInterrogator;
import net.officefloor.plugin.section.clazz.spawn.ClassSectionFlowSpawnInterrogatorContext;
import net.officefloor.plugin.section.clazz.spawn.ClassSectionFlowSpawnInterrogatorServiceFactory;

/**
 * {@link Class} loader for {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionLoader implements ClassSectionLoaderContext {

	/**
	 * {@link SectionDesigner}.
	 */
	private final SectionDesigner designer;

	/**
	 * {@link SectionSourceContext}.
	 */
	private final SectionSourceContext sectionContext;

	/**
	 * {@link ClassSectionObjectManufacturerContext}.
	 */
	private final ClassSectionObjectManufacturerContextImpl objectContext;

	/**
	 * {@link ClassSectionFlowManufacturerContext}.
	 */
	private final ClassSectionFlowManufacturerContextImpl flowContext;

	/**
	 * Instantiate.
	 * 
	 * @param designer       {@link SectionDesigner}.
	 * @param sectionContext {@link SectionSourceContext}.
	 */
	public ClassSectionLoader(SectionDesigner designer, SectionSourceContext sectionContext) {
		this.designer = designer;
		this.sectionContext = sectionContext;

		// Create the dependency/flow contexts
		this.objectContext = new ClassSectionObjectManufacturerContextImpl(designer, sectionContext);
		this.flowContext = new ClassSectionFlowManufacturerContextImpl(designer, sectionContext);
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
	 * Indicates if have qualifier.
	 * 
	 * @param qualifier Qualifier.
	 * @return <code>true</code> if have qualifier.
	 */
	protected boolean isQualifier(String qualifier) {
		return !CompileUtil.isBlank(qualifier);
	}

	/**
	 * Loads the {@link SectionFunction} instances.
	 * 
	 * @param namespaceName                  Name of
	 *                                       {@link SectionFunctionNamespace}.
	 * @param managedFunctionSourceClassName {@link ManagedFunctionSource}
	 *                                       {@link Class} name.
	 * @param properties                     {@link PropertyList} for the
	 *                                       {@link ManagedFunctionSource}.
	 * @param functionDecoration             {@link FunctionDecoration}. May be
	 *                                       <code>null</code>.
	 * @return {@link ClassSectionFunctionNamespace}.
	 * @throws Exception If fails to load {@link SectionFunction} instances.
	 */
	public ClassSectionFunctionNamespace addManagedFunctions(String namespaceName,
			String managedFunctionSourceClassName, PropertyList properties, FunctionDecoration functionDecoration)
			throws Exception {
		return this.flowContext.addFunctionNamespace(namespaceName, managedFunctionSourceClassName, null, properties,
				functionDecoration);
	}

	/**
	 * Loads the {@link SectionFunction} instances.
	 * 
	 * @param namespaceName         Name of {@link SectionFunctionNamespace}.
	 * @param managedFunctionSource {@link ManagedFunctionSource}.
	 * @param properties            {@link PropertyList} for the
	 *                              {@link ManagedFunctionSource}.
	 * @param functionDecoration    {@link FunctionDecoration}. May be
	 *                              <code>null</code>.
	 * @return {@link ClassSectionFunctionNamespace}.
	 * @throws Exception If fails to load {@link SectionFunction} instances.
	 */
	public ClassSectionFunctionNamespace addManagedFunctions(String namespaceName,
			ManagedFunctionSource managedFunctionSource, PropertyList properties, FunctionDecoration functionDecoration)
			throws Exception {
		return this.flowContext.addFunctionNamespace(namespaceName, managedFunctionSource.getClass().getName(),
				managedFunctionSource, properties, functionDecoration);
	}

	/**
	 * Loads the {@link SectionManagedObject}.
	 * 
	 * @param objectName                   Name of {@link SectionManagedObject}.
	 * @param managedObjectSourceClassName {@link ManagedObjectSource} {@link Class}
	 *                                     name.
	 * @param properties                   {@link PropertyList} for the
	 *                                     {@link ManagedObjectSource}.
	 * @param objectDecoration             {@link ObjectDecoration} May be
	 *                                     <code>null</code>.
	 * @return {@link ClassSectionManagedObject}.
	 * @throws Exception If fails to load the {@link SectionManagedObject}.
	 */
	public ClassSectionManagedObject addManagedObject(String objectName, String managedObjectSourceClassName,
			PropertyList properties, ObjectDecoration objectDecoration) throws Exception {
		return this.objectContext.addManagedObject(objectName, managedObjectSourceClassName, null, properties,
				objectDecoration);
	}

	/**
	 * Loads the {@link SectionManagedObject}.
	 * 
	 * @param objectName          Name of {@link SectionManagedObject}.
	 * @param managedObjectSource {@link ManagedObjectSource}.
	 * @param properties          {@link PropertyList} for the
	 *                            {@link ManagedObjectSource}.
	 * @param objectDecoration    {@link ObjectDecoration} May be <code>null</code>.
	 * @return {@link ClassSectionManagedObject}.
	 * @throws Exception If fails to load the {@link SectionManagedObject}.
	 */
	public ClassSectionManagedObject addManagedObject(String objectName, ManagedObjectSource<?, ?> managedObjectSource,
			PropertyList properties, ObjectDecoration objectDecoration) throws Exception {
		return this.objectContext.addManagedObject(objectName, managedObjectSource.getClass().getName(),
				managedObjectSource, properties, objectDecoration);
	}

	/**
	 * Obtains the {@link SectionDependencyObjectNode}.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param typeName  Type name of dependency.
	 * @return {@link SectionDependencyObjectNode}.
	 * @throws Exception If fails to obtain {@link SectionDependencyObjectNode}.
	 */
	public SectionDependencyObjectNode getDependency(String qualifier, String typeName) throws Exception {
		return this.objectContext.getDependency(qualifier, typeName, null);
	}

	/**
	 * Obtains the {@link ClassSectionFlow}.
	 * 
	 * @param flowName         Name of the {@link SectionFlowSinkNode}.
	 * @param argumentTypeName Argument type name. May be <code>null</code> for no
	 *                         argument.
	 * @return {@link ClassSectionFlow}.
	 */
	public ClassSectionFlow getFlow(String flowName, String argumentTypeName) {
		return this.flowContext.getFlow(flowName, argumentTypeName);
	}

	/**
	 * Obtains the {@link ClassSectionManagedFunction}.
	 * 
	 * @param functionName Name of the {@link SectionFunction}.
	 * @return {@link ClassSectionManagedFunction} or <code>null</code> if no
	 *         {@link SectionFunction} by the name.
	 */
	public ClassSectionManagedFunction getFunction(String functionName) {
		return this.flowContext.getFunction(functionName);
	}

	/**
	 * Obtains the {@link SectionFlowSinkNode} for the {@link Escalation}.
	 * 
	 * @param escalationType {@link Escalation} type.
	 * @return {@link SectionFlowSinkNode}.
	 * @throws Exception If fails to obtain {@link SectionFlowSinkNode}.
	 */
	public SectionFlowSinkNode getEscalation(Class<?> escalationType) throws Exception {
		return this.flowContext.getEscalationSink(null, escalationType);
	}

	/**
	 * Links non-configured items.
	 * 
	 * @throws Exception If fails to configure.
	 */
	public void load() throws Exception {

		// Keep configuring until complete
		FurtherConfiguration furtherConfiguration = new FurtherConfiguration();
		do {

			// Link managed object dependencies
			// (must be before functions to allow functions to re-use plugin dependencies)
			NEXT_OBJECT: for (SectionClassManagedObject sectionMo : new ArrayList<>(
					this.objectContext.sectionManagedObjects.values())) {

				// Determine if already configured
				if (sectionMo.isAlreadyConfigured()) {
					continue NEXT_OBJECT;
				}

				// Obtain the object details
				SectionManagedObject managedObject = sectionMo.managedObject;
				ManagedObjectType<?> managedObjectType = sectionMo.managedObjectType;
				ObjectDecoration objectDecoration = sectionMo.objectDecoration;

				// Possibly decorate the managed object
				ObjectClassSectionLoaderContextImpl objectContext = new ObjectClassSectionLoaderContextImpl(
						managedObject, managedObjectType);
				if (objectDecoration != null) {
					objectDecoration.decorateObject(objectContext);
				}

				// Link the dependencies
				ManagedObjectDependencyType<?>[] moDependencyTypes = managedObjectType.getDependencyTypes();
				NEXT_OBJECT_DEPENDENCY: for (int i = 0; i < moDependencyTypes.length; i++) {
					ManagedObjectDependencyType<?> moDependencyType = moDependencyTypes[i];

					// Determine if already linked
					if (objectContext.linkedDependencyIndexes.contains(i)) {
						continue NEXT_OBJECT_DEPENDENCY;
					}

					// Obtain the dependency
					SectionManagedObjectDependency moDependency = managedObject
							.getSectionManagedObjectDependency(moDependencyType.getDependencyName());

					// Link to its implementing dependency
					String dependencyQualifier = moDependencyType.getTypeQualifier();
					String dependencyTypeName = moDependencyType.getDependencyType().getName();
					SectionDependencyObjectNode dependency = this.objectContext.getDependency(dependencyQualifier,
							dependencyTypeName, moDependencyType);
					this.designer.link(moDependency, dependency);
				}
			}

			// Link functions
			NEXT_FUNCTION: for (SectionClassFunction sectionFunction : new ArrayList<>(
					this.flowContext.sectionFunctions.values())) {

				// Determine if already configured
				if (sectionFunction.isAlreadyConfigured()) {
					continue NEXT_FUNCTION;
				}

				// Obtain the function details
				SectionFunction function = sectionFunction.managedFunction;
				ManagedFunctionType<?, ?> functionType = sectionFunction.managedFunctionType;
				Class<?> parameterType = sectionFunction.parameterType;
				int parameterIndex = sectionFunction.parameterIndex;
				FunctionDecoration functionDecoration = sectionFunction.functionDecoration;

				// Possibly decorate the section function
				FunctionClassSectionLoaderContextImpl functionContext = new FunctionClassSectionLoaderContextImpl(
						function, functionType, parameterType);
				if (functionDecoration != null) {
					functionDecoration.decorateSectionFunction(functionContext);
				}

				// Link the next (if available)
				if (functionContext.isLinkNext) {
					ClassSectionFlow nextSink = this.flowContext.getOptionalFlowSink(functionType);
					if (nextSink != null) {
						designer.link(function, nextSink.getFlowSink());
					}
				}

				// Link the function flows
				ManagedFunctionFlowType<?>[] functionFlowTypes = functionType.getFlowTypes();
				NEXT_FUNCTION_FLOW: for (int i = 0; i < functionFlowTypes.length; i++) {
					ManagedFunctionFlowType<?> functionFlowType = functionFlowTypes[i];

					// Determine if already linked
					if (functionContext.linkedFlowIndexes.contains(i)) {
						continue NEXT_FUNCTION_FLOW;
					}

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
							return ClassSectionLoader.this.sectionContext;
						}
					};
					CHECK_SPAWN: for (ClassSectionFlowSpawnInterrogator interrogator : this.sectionContext
							.loadOptionalServices(ClassSectionFlowSpawnInterrogatorServiceFactory.class)) {
						try {
							if (interrogator.isSpawnFlow(spawnContext)) {
								isSpawn = true;
								break CHECK_SPAWN;
							}
						} catch (Exception ex) {
							throw this.designer.addIssue("Failed to determine if spawn flow", ex);
						}
					}

					// Obtain the flow sink
					Class<?> flowArgumentType = functionFlowType.getArgumentType();
					ClassSectionFlow flowSink = this.flowContext.getFlowSink(flowName,
							flowArgumentType != null ? flowArgumentType.getName() : null, functionFlowType);
					this.designer.link(functionFlow, flowSink.getFlowSink(), isSpawn);
				}

				// Link escalations for the function
				for (ManagedFunctionEscalationType functionEscalationType : functionType.getEscalationTypes()) {

					// Obtain the function escalation
					FunctionFlow functionEscalation = function
							.getFunctionEscalation(functionEscalationType.getEscalationName());

					// Link escalation
					Class<?> escalationType = functionEscalationType.getEscalationType();
					SectionFlowSinkNode escalationHandler = this.flowContext.getEscalationSink(function,
							escalationType);
					this.designer.link(functionEscalation, escalationHandler, false);
				}

				// Link the function dependencies
				ManagedFunctionObjectType<?>[] functionObjectTypes = functionType.getObjectTypes();
				NEXT_FUNCTION_OBJECT: for (int i = 0; i < functionObjectTypes.length; i++) {
					ManagedFunctionObjectType<?> functionObjectType = functionObjectTypes[i];

					// Determine if already linked
					if (functionContext.linkedObjectIndexes.contains(i)) {
						continue NEXT_FUNCTION_OBJECT;
					}

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
					SectionDependencyObjectNode objectDependency = this.objectContext.getDependency(objectQualifier,
							objectTypeName, functionObjectType);

					// Link dependency (if available)
					if (objectDependency != null) {
						this.designer.link(functionObject, objectDependency);
					}
				}
			}

			// Link sub sections
			NEXT_SUB_SECTION: for (SectionClassSubSection subSectionStruct : new ArrayList<>(
					this.flowContext.subSections.values())) {

				// Determine if already configured
				if (subSectionStruct.isAlreadyConfigured()) {
					continue NEXT_SUB_SECTION;
				}

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
						Class<?> escalationType = this.sectionContext.loadClass(escalationTypeName);
						SectionFlowSinkNode handler = this.flowContext.getEscalationSink(null, escalationType);

						// Link escalation handling
						this.designer.link(escalation, handler);
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
					ClassSectionFlow flowSink = this.flowContext.getFlowSink(linkName, argumentTypeName, outputType);

					// Link
					this.designer.link(output, flowSink.getFlowSink());
				}

				// Ensure no additional link configuration
				if (outputsToLinks.size() > 0) {
					throw this.designer.addIssue(SectionOutput.class.getSimpleName() + " instances do not exist on "
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
					SectionDependencyObjectNode dependency = this.objectContext.getDependency(objectTypeQualifier,
							objectTypeName, subSectionObjectType);

					// Link to dependency
					this.designer.link(subSectionObject, dependency);
				}
			}

			// Check if further configuration required
			furtherConfiguration.isRequireFurtherConfiguration = false;
			for (SectionClassFunction sectionFunction : this.flowContext.sectionFunctions.values()) {
				sectionFunction.loadRequireConfigure(furtherConfiguration);
			}
			for (SectionClassSubSection subSectionStruct : this.flowContext.subSections.values()) {
				subSectionStruct.loadRequireConfigure(furtherConfiguration);
			}
			for (SectionClassManagedObject sectionMo : this.objectContext.sectionManagedObjects.values()) {
				sectionMo.loadRequireConfigure(furtherConfiguration);
			}

		} while (furtherConfiguration.isRequireFurtherConfiguration);
	}

	/*
	 * ========================= ClassSectionLoaderContext =========================
	 */

	@Override
	public ClassSectionObjectContext getSectionObjectContext() {
		return this.objectContext;
	}

	@Override
	public ClassSectionFlowContext getSectionFlowContext() {
		return this.flowContext;
	}

	@Override
	public SectionDesigner getSectionDesigner() {
		return this.designer;
	}

	@Override
	public SectionSourceContext getSectionSourceContext() {
		return this.sectionContext;
	}

	/**
	 * {@link FunctionClassSectionLoaderContext} implementation.
	 */
	private class FunctionClassSectionLoaderContextImpl implements FunctionClassSectionLoaderContext {

		/**
		 * {@link SectionFunction}.
		 */
		private final SectionFunction function;

		/**
		 * {@link ManagedFunctionType}.
		 */
		private final ManagedFunctionType<?, ?> functionType;

		/**
		 * Parameter {@link Class}.
		 */
		private final Class<?> parameterType;

		/**
		 * Indicates if link next.
		 */
		private boolean isLinkNext = true;

		/**
		 * Linked {@link FunctionObject} indexes.
		 */
		private final Set<Integer> linkedObjectIndexes = new HashSet<>();

		/**
		 * Linked {@link FunctionFlow} indexes.
		 */
		private final Set<Integer> linkedFlowIndexes = new HashSet<>();

		/**
		 * Instantiate.
		 * 
		 * @param function      {@link SectionFunction}.
		 * @param functionType  {@link ManagedFunctionType}.
		 * @param parameterType Parameter {@link Class}.
		 */
		private FunctionClassSectionLoaderContextImpl(SectionFunction function, ManagedFunctionType<?, ?> functionType,
				Class<?> parameterType) {
			this.function = function;
			this.functionType = functionType;
			this.parameterType = parameterType;
		}

		/*
		 * ==================== FunctionClassSectionLoaderContext ====================
		 */

		@Override
		public SectionFunction getSectionFunction() {
			return this.function;
		}

		@Override
		public ManagedFunctionType<?, ?> getManagedFunctionType() {
			return this.functionType;
		}

		@Override
		public Class<?> getParameterType() {
			return this.parameterType;
		}

		@Override
		public void flagNextLinked() {
			this.isLinkNext = false;
		}

		@Override
		public void flagFunctionObjectLinked(int objectIndex) {
			this.linkedObjectIndexes.add(objectIndex);
		}

		@Override
		public void flagFunctionFlowLinked(int flowIndex) {
			this.linkedFlowIndexes.add(flowIndex);
		}

		@Override
		public ClassSectionObjectContext getSectionObjectContext() {
			return ClassSectionLoader.this.objectContext;
		}

		@Override
		public ClassSectionFlowContext getSectionFlowContext() {
			return ClassSectionLoader.this.flowContext;
		}

		@Override
		public SectionDesigner getSectionDesigner() {
			return ClassSectionLoader.this.designer;
		}

		@Override
		public SectionSourceContext getSectionSourceContext() {
			return ClassSectionLoader.this.sectionContext;
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
		 * {@link SectionClassFunctionNamespace} instances by their {@link SourceKey}.
		 */
		private final Map<SourceKey, SectionClassFunctionNamespace> sectionFunctionNamespaces = new HashMap<>();

		/**
		 * {@link SectionFunctionNamespace} instances by their name.
		 */
		private final Set<String> namespaceNames = new HashSet<>();

		/**
		 * {@link SectionClassFunction} instances by their {@link SectionFunction} name.
		 */
		private final Map<String, SectionClassFunction> sectionFunctions = new HashMap<>();

		/**
		 * {@link SectionClassSubSection} instances by their {@link SourceKey}.
		 */
		private final Map<SourceKey, SectionClassSubSection> subSections = new HashMap<>();

		/**
		 * {@link SubSection} instances by their name.
		 */
		private final Set<String> sectionNames = new HashSet<>();

		/**
		 * {@link SectionClassOutput} instances by name.
		 */
		private final Map<String, SectionClassOutput> sectionOutputs = new HashMap<>();

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
		}

		/**
		 * Obtains the optional {@link ClassSectionFlow}.
		 * 
		 * @param annotatedType {@link AnnotatedType}.
		 * @return {@link ClassSectionFlow} or <code>null</code>.
		 * @throws Exception If fails to obtain.
		 */
		private ClassSectionFlow getOptionalFlowSink(AnnotatedType annotatedType) throws Exception {

			// Attempt to obtain flow sink
			this.annotatedType = annotatedType;
			for (ClassSectionFlowManufacturer manufacturer : this.context
					.loadOptionalServices(ClassSectionFlowManufacturerServiceFactory.class)) {

				// Obtain the flow sink
				ClassSectionFlow flow = manufacturer.createFlow(this);
				if (flow != null) {
					return flow; // have flow sink
				}
			}

			// As here, no plugin flow sink
			return null;
		}

		/**
		 * Obtain the {@link ClassSectionFlow}.
		 * 
		 * @param flowName      Name of {@link Flow}.
		 * @param argumentType  Type of argument. May be <code>null</code> for no
		 *                      argument.
		 * @param annotatedType {@link AnnotatedType}. May be <code>null</code>.
		 * @return {@link ClassSectionFlow}.
		 * @throws Exception If fails to obtain.
		 */
		private ClassSectionFlow getFlowSink(String flowName, String argumentType, AnnotatedType annotatedType)
				throws Exception {

			// Determine if function by name
			SectionClassFunction function = this.sectionFunctions.get(flowName);
			if (function != null) {
				return new ClassSectionManagedFunction(function.managedFunction, function.managedFunctionType,
						function.parameterType); // sink to function
			}

			// Determine if section output
			SectionClassOutput output = this.sectionOutputs.get(flowName);
			if (output != null) {
				return new ClassSectionFlow(output.output, output.parameterType); // sink to output
			}

			// Obtain via plugin
			if (annotatedType != null) {
				ClassSectionFlow flow = this.getOptionalFlowSink(annotatedType);
				if (flow != null) {
					return flow;
				}
			}

			// Link to created section output
			Class<?> parameterType = (argumentType == null) ? null : this.context.loadClass(argumentType);
			SectionOutput sectionOutput = this.designer.addSectionOutput(flowName, argumentType, false);
			this.sectionOutputs.put(flowName, new SectionClassOutput(sectionOutput, parameterType));
			return new ClassSectionFlow(sectionOutput, parameterType);
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

				// Ensure can handle escalation
				if (!handlingType.isAssignableFrom(escalationType)) {
					continue NEXT_HANDLER; // not able to handle escalation
				}

				// Section Output must match exactly
				if ((checkHandler instanceof SectionOutput) && (!handlingType.equals(escalationType))) {
					// Section output must match exactly
					continue NEXT_HANDLER;
				}

				// Find the distance to escalation
				int distance = 0;
				Class<?> parentEscalationType = escalationType;
				while ((parentEscalationType != null) && (!parentEscalationType.equals(handlingType))) {
					parentEscalationType = parentEscalationType.getSuperclass();
					distance++;
				}

				// Replace handler if closer distance
				if (distance < minDistance) {
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

		/**
		 * Obtains the item name.
		 * 
		 * @param hintName      Hint name.
		 * @param existingNames Existing names.
		 * @return Returns the item name.
		 */
		private <I> String getItemName(String hintName, Set<String> existingNames) {

			// Determine name
			int index = 0;
			String name;
			do {
				name = hintName + (index == 0 ? "" : "-" + index);
				index++;
			} while (existingNames.contains(name));

			// Register the name
			existingNames.add(name);

			// Return the name
			return name;
		}

		/**
		 * Adds the {@link SectionFunctionNamespace}.
		 * 
		 * @param namespaceName                  Name of
		 *                                       {@link SectionFunctionNamespace}.
		 * @param managedFunctionSourceClassName {@link ManagedFunctionSource}
		 *                                       {@link Class} name.
		 * @param managedFunctionSource          {@link ManagedFunctionSource}. May be
		 *                                       <code>null</code>.
		 * @param properties                     {@link PropertyList}.
		 * @param functionDecoration             {@link FunctionDecoration}.
		 * @return {@link ClassSectionFunctionNamespace}.
		 */
		private ClassSectionFunctionNamespace addFunctionNamespace(String namespaceName,
				String managedFunctionSourceClassName, ManagedFunctionSource managedFunctionSource,
				PropertyList properties, FunctionDecoration functionDecoration) {

			// Ensure have function decoration
			if (functionDecoration == null) {
				functionDecoration = new FunctionDecoration();
			}

			// Determine if already registered
			SourceKey sourceKey = new SourceKey(managedFunctionSourceClassName, properties);
			SectionClassFunctionNamespace registeredNamespace = this.sectionFunctionNamespaces.get(sourceKey);
			if (registeredNamespace != null) {
				// Already registered
				return new ClassSectionFunctionNamespace(registeredNamespace.namespace,
						registeredNamespace.namespaceType);
			}

			// Obtain the namespace name
			String functionNamespaceName = this.getItemName(namespaceName, this.namespaceNames);

			// Load the namespace type for the class
			FunctionNamespaceType namespaceType = managedFunctionSource != null
					? this.context.loadManagedFunctionType(functionNamespaceName, managedFunctionSource, properties)
					: this.context.loadManagedFunctionType(functionNamespaceName, managedFunctionSourceClassName,
							properties);

			// Add the namespace
			SectionFunctionNamespace namespace = managedFunctionSource != null
					? this.designer.addSectionFunctionNamespace(namespaceName, managedFunctionSource)
					: this.designer.addSectionFunctionNamespace(namespaceName, managedFunctionSourceClassName);
			properties.configureProperties(namespace);

			// Register the namespace
			this.sectionFunctionNamespaces.put(sourceKey, new SectionClassFunctionNamespace(namespace, namespaceType));

			// Load functions
			NEXT_FUNCTION: for (ManagedFunctionType<?, ?> functionType : namespaceType.getManagedFunctionTypes()) {

				// Determine if include function
				if (!functionDecoration.isIncludeFunction(functionType, ClassSectionLoader.this)) {
					continue NEXT_FUNCTION;
				}

				// Obtain the function name
				String functionName = functionDecoration.getFunctionName(functionType, ClassSectionLoader.this);

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
							isParameter = ClassSectionParameterInterrogation.isParameter(functionObjectType,
									this.context, interrogator);
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
				this.sectionFunctions.put(functionName, new SectionClassFunction(functionType, function, parameterType,
						parameterIndex, functionDecoration));

				// Register as potential escalation handler
				if ((parameterType != null) && (Throwable.class.isAssignableFrom(parameterType))) {
					this.escalationHandlers.put(parameterType, function);
				}
			}

			// Return the namespace
			return new ClassSectionFunctionNamespace(namespace, namespaceType);
		}

		/**
		 * Gets or creates the {@link SubSection}.
		 * 
		 * @param sectionName            Hint to use as name of {@link SubSection}. May
		 *                               alter to keep name unique.
		 * @param sectionSourceClassName Name of {@link SectionSource} {@link Class}.
		 * @param sectionSource          {@link SectionSource}. May be
		 *                               <code>null</code>.
		 * @param sectionLocation        Location of the {@link SubSection}.
		 * @param properties             {@link PropertyList} for the
		 *                               {@link SubSection}.
		 * @param configuredLinks        {@link ClassSectionSubSectionOutputLink}
		 *                               instances.
		 * @return {@link ClassSectionSubSection}.
		 */
		private ClassSectionSubSection getOrCreateSubSection(String sectionName, String sectionSourceClassName,
				SectionSource sectionSource, String sectionLocation, PropertyList properties,
				ClassSectionSubSectionOutputLink... configuredLinks) {

			// Determine if already registered
			SourceKey sourceKey = new SourceKey(sectionSourceClassName, sectionLocation, properties);
			SectionClassSubSection subSectionStruct = this.subSections.get(sourceKey);
			if (subSectionStruct != null) {
				// Already registered
				return new ClassSectionSubSection(subSectionStruct.subSection, subSectionStruct.sectionType);
			}

			// Obtain the section name
			String subSectionName = this.getItemName(sectionName, this.sectionNames);

			// Load the section type
			SectionType sectionType = sectionSource != null
					? this.context.loadSectionType(subSectionName, sectionSource, sectionLocation, properties)
					: this.context.loadSectionType(subSectionName, sectionSourceClassName, sectionLocation, properties);

			// Add the sub section
			SubSection subSection = sectionSource != null
					? this.designer.addSubSection(subSectionName, sectionSource, sectionLocation)
					: this.designer.addSubSection(subSectionName, sectionSourceClassName, sectionLocation);
			properties.configureProperties(subSection);

			// Capture the output links
			Map<String, String> outputsToLinks = new HashMap<>();
			for (ClassSectionSubSectionOutputLink link : configuredLinks) {
				outputsToLinks.put(link.getSubSectionOutputName(), link.getLinkName());
			}

			// Register the sub section
			this.subSections.put(sourceKey, new SectionClassSubSection(sectionType, subSection, outputsToLinks));

			// Return the sub section
			return new ClassSectionSubSection(subSection, sectionType);
		}

		/*
		 * ==================== ClassSectionFlowManufacturerContext ====================
		 */

		@Override
		public AnnotatedType getAnnotatedType() {
			return this.annotatedType;
		}

		@Override
		public ClassSectionFunctionNamespace addFunctionNamespace(String namespaceName,
				String managedFunctionSourceClassName, PropertyList properties) {
			return this.addFunctionNamespace(namespaceName, managedFunctionSourceClassName, null, properties, null);
		}

		@Override
		public ClassSectionFunctionNamespace addFunctionNamespace(String namespaceName,
				ManagedFunctionSource managedFunctionSource, PropertyList properties) {
			return this.addFunctionNamespace(namespaceName, managedFunctionSource.getClass().getName(),
					managedFunctionSource, properties, null);
		}

		@Override
		public ClassSectionManagedFunction getFunction(String functionName) {
			SectionClassFunction function = this.sectionFunctions.get(functionName);
			return function != null
					? new ClassSectionManagedFunction(function.managedFunction, function.managedFunctionType,
							function.parameterType)
					: null;
		}

		@Override
		public ClassSectionSubSectionOutputLink createSubSectionOutputLink(String subSectionOutputName,
				String linkName) {
			return new ClassSectionSubSectionOutputLinkImpl(subSectionOutputName, linkName);
		}

		@Override
		public ClassSectionSubSection getOrCreateSubSection(String sectionName, String sectionSourceClassName,
				String sectionLocation, PropertyList properties, ClassSectionSubSectionOutputLink... configuredLinks) {
			return this.getOrCreateSubSection(sectionName, sectionSourceClassName, null, sectionLocation, properties,
					configuredLinks);
		}

		@Override
		public ClassSectionSubSection getOrCreateSubSection(String sectionName, SectionSource sectionSource,
				String sectionLocation, PropertyList properties, ClassSectionSubSectionOutputLink... configuredLinks) {
			return this.getOrCreateSubSection(sectionName, sectionSource.getClass().getName(), sectionSource,
					sectionLocation, properties, configuredLinks);
		}

		@Override
		public SectionSourceContext getSourceContext() {
			return this.context;
		}

		@Override
		public ClassSectionFlow getFlow(String flowName, String argumentType) {
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
	 * Associates the {@link FunctionNamespaceType},
	 * {@link SectionFunctionNamespace}.
	 */
	private static class SectionClassFunctionNamespace {

		/**
		 * {@link SectionFunctionNamespace}.
		 */
		private final SectionFunctionNamespace namespace;

		/**
		 * {@link FunctionNamespaceType}.
		 */
		private final FunctionNamespaceType namespaceType;

		/**
		 * Instantiate.
		 * 
		 * @param namespace     {@link SectionFunctionNamespace}.
		 * @param namespaceType {@link FunctionNamespaceType}.
		 */
		private SectionClassFunctionNamespace(SectionFunctionNamespace namespace, FunctionNamespaceType namespaceType) {
			this.namespace = namespace;
			this.namespaceType = namespaceType;
		}
	}

	/**
	 * Associates the {@link ManagedFunctionType}, {@link SectionFunction} and its
	 * parameter.
	 */
	private static class SectionClassFunction extends AbstractConfigurationItem {

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
		 * {@link FunctionDecoration}.
		 */
		private final FunctionDecoration functionDecoration;

		/**
		 * Instantiate.
		 * 
		 * @param managedFunctionType {@link ManagedFunctionType}.
		 * @param managedFunction     {@link SectionFunction}.
		 * @param parameterType       Parameter type. May be <code>null</code>.
		 * @param parameterIndex      Index of the parameter.
		 * @param functionDecoration  {@link FunctionDecoration}.
		 */
		private SectionClassFunction(ManagedFunctionType<?, ?> managedFunctionType, SectionFunction managedFunction,
				Class<?> parameterType, int parameterIndex, FunctionDecoration functionDecoration) {
			this.managedFunctionType = managedFunctionType;
			this.managedFunction = managedFunction;
			this.parameterType = parameterType;
			this.parameterIndex = parameterIndex;
			this.functionDecoration = functionDecoration;
		}
	}

	/**
	 * Associates the {@link SectionOutput} within its parameter.
	 */
	private static class SectionClassOutput {

		/**
		 * {@link SectionOutput}.
		 */
		private final SectionOutput output;

		/**
		 * Parameter type. May be <code>null</code>.
		 */
		private final Class<?> parameterType;

		/**
		 * Instantiate.
		 * 
		 * @param output        {@link SectionOutput}.
		 * @param parameterType Parameter type. May be <code>null</code>.
		 */
		private SectionClassOutput(SectionOutput output, Class<?> parameterType) {
			this.output = output;
			this.parameterType = parameterType;
		}
	}

	/**
	 * Associates the {@link SectionType} and {@link SubSection}.
	 */
	private static class SectionClassSubSection extends AbstractConfigurationItem {

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
	 * {@link ObjectClassSectionLoaderContext} implementation.
	 */
	private class ObjectClassSectionLoaderContextImpl implements ObjectClassSectionLoaderContext {

		/**
		 * {@link SectionManagedObject}.
		 */
		private final SectionManagedObject sectionManagedObject;

		/**
		 * {@link ManagedObjectType}.
		 */
		private final ManagedObjectType<?> managedObjectType;

		/**
		 * Linked dependency indexes.
		 */
		private final Set<Integer> linkedDependencyIndexes = new HashSet<>();

		/**
		 * Instantiate.
		 * 
		 * @param sectionManagedObject {@link SectionManagedObject}.
		 * @param managedObjectType    {@link ManagedObjectType}.
		 */
		private ObjectClassSectionLoaderContextImpl(SectionManagedObject sectionManagedObject,
				ManagedObjectType<?> managedObjectType) {
			this.sectionManagedObject = sectionManagedObject;
			this.managedObjectType = managedObjectType;
		}

		/*
		 * ================== ObjectClassSectionLoaderContext ================
		 */

		@Override
		public SectionManagedObject getSectionManagedObject() {
			return this.sectionManagedObject;
		}

		@Override
		public ManagedObjectType<?> getManagedObjectType() {
			return this.managedObjectType;
		}

		@Override
		public void flagObjectDependencyLinked(int dependencyIndex) {
			this.linkedDependencyIndexes.add(dependencyIndex);
		}

		@Override
		public ClassSectionObjectContext getSectionObjectContext() {
			return ClassSectionLoader.this.objectContext;
		}

		@Override
		public ClassSectionFlowContext getSectionFlowContext() {
			return ClassSectionLoader.this.flowContext;
		}

		@Override
		public SectionDesigner getSectionDesigner() {
			return ClassSectionLoader.this.designer;
		}

		@Override
		public SectionSourceContext getSectionSourceContext() {
			return ClassSectionLoader.this.sectionContext;
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
		 * Adds the {@link SectionManagedObject}.
		 * 
		 * @param objectName                   Name of the {@link SectionManagedObject}.
		 * @param managedObjectSourceClassName {@link ManagedObjectSource} {@link Class}
		 *                                     name.
		 * @param managedObjectSource          {@link ManagedObjectSource}. May be
		 *                                     <code>null</code>.
		 * @param properties                   {@link PropertyList} for the
		 *                                     {@link ManagedObjectSource}.
		 * @param objectDecoration             {@link ObjectDecoration}. May be
		 *                                     <code>null</code>.
		 * @param typeQualifiers               {@link ClassSectionTypeQualifier}
		 *                                     instances.
		 * @return {@link ClassSectionManagedObject}.
		 */
		private ClassSectionManagedObject addManagedObject(String objectName, String managedObjectSourceClassName,
				ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties,
				ObjectDecoration objectDecoration, ClassSectionTypeQualifier... typeQualifiers) {

			// Ensure have decoration
			if (objectDecoration == null) {
				objectDecoration = new ObjectDecoration();
			}

			// Not existing, so load the managed object type
			String moTypeName = CompileUtil.isBlank(objectName) ? "MO" : objectName;
			ManagedObjectType<?> moType = managedObjectSource != null
					? this.context.loadManagedObjectType(moTypeName, managedObjectSource, properties)
					: this.context.loadManagedObjectType(moTypeName, managedObjectSourceClassName, properties);

			// Obtain the dependency information
			Class<?> objectType = moType.getObjectType();

			// Derive the object name
			ClassSectionTypeQualifier namingTypeQualifier = typeQualifiers.length > 0 ? typeQualifiers[0]
					: this.createTypeQualifier(null, objectType);
			String derivedName = ClassSectionLoader.this.getSectionObjectName(namingTypeQualifier.getQualifier(),
					namingTypeQualifier.getType().getName());

			// Ensure have object name
			if (CompileUtil.isBlank(objectName)) {
				objectName = derivedName;
			}

			// Add the managed object
			SectionManagedObjectSource mos = managedObjectSource != null
					? this.designer.addSectionManagedObjectSource(objectName, managedObjectSource)
					: this.designer.addSectionManagedObjectSource(objectName, managedObjectSourceClassName);
			properties.configureProperties(mos);
			SectionManagedObject mo = mos.addSectionManagedObject(objectName, ManagedObjectScope.THREAD);
			for (ClassSectionTypeQualifier typeQualifier : typeQualifiers) {
				mo.addTypeQualification(typeQualifier.getQualifier(), typeQualifier.getType().getName());
			}

			// Register the managed object to link dependencies
			SourceKey sourceKey = new SourceKey(managedObjectSourceClassName, properties);
			this.sectionManagedObjects.put(sourceKey, new SectionClassManagedObject(moType, mo, objectDecoration));

			// Register the managed object for dependencies
			if (typeQualifiers.length == 0) {
				// Just register under name
				this.sectionObjects.put(derivedName, mo);

			} else {
				// Register under all type qualifications
				for (ClassSectionTypeQualifier typeQualifier : typeQualifiers) {
					String qualifiedName = ClassSectionLoader.this.getSectionObjectName(typeQualifier.getQualifier(),
							typeQualifier.getType().getName());
					this.sectionObjects.put(qualifiedName, mo);
				}
			}

			// Return the managed object
			return new ClassSectionManagedObject(mo, moType);
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
			String objectName = ClassSectionLoader.this.getSectionObjectName(qualifier, typeName);
			SectionDependencyObjectNode sectionObjectNode = this.sectionObjects.get(objectName);
			if (sectionObjectNode != null) {
				return sectionObjectNode; // have existing dependency
			}

			// Attempt to load the section dependency via plugin
			if (annotatedType != null) {
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
			}

			// No plugin dependency, so fall back to section object
			SectionObject sectionObject = this.designer.addSectionObject(objectName, typeName);
			if (ClassSectionLoader.this.isQualifier(qualifier)) {
				sectionObject.setTypeQualifier(qualifier);
			}
			this.sectionObjects.put(objectName, sectionObject);

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
		public ClassSectionManagedObject getOrCreateManagedObject(String managedObjectSourceClassName,
				PropertyList properties, ClassSectionTypeQualifier... typeQualifiers) {

			// Determine if already have managed object
			SourceKey sourceKey = new SourceKey(managedObjectSourceClassName, properties);
			SectionClassManagedObject existing = this.sectionManagedObjects.get(sourceKey);
			if (existing != null) {
				return new ClassSectionManagedObject(existing.managedObject, existing.managedObjectType);
			}

			// Return the added managed object
			return this.addManagedObject(null, managedObjectSourceClassName, null, properties, null, typeQualifiers);
		}

		@Override
		public ClassSectionManagedObject getOrCreateManagedObject(ManagedObjectSource<?, ?> managedObjectSource,
				PropertyList properties, ClassSectionTypeQualifier... typeQualifiers) {

			// Obtain managed object source class name
			String managedObjectSourceClassName = managedObjectSource.getClass().getName();

			// Determine if already have managed object
			SourceKey sourceKey = new SourceKey(managedObjectSourceClassName, properties);
			SectionClassManagedObject existing = this.sectionManagedObjects.get(sourceKey);
			if (existing != null) {
				return new ClassSectionManagedObject(existing.managedObject, existing.managedObjectType);
			}

			// Return the added managed object
			return this.addManagedObject(null, managedObjectSourceClassName, managedObjectSource, properties, null,
					typeQualifiers);
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
	private static class SectionClassManagedObject extends AbstractConfigurationItem {

		/**
		 * {@link ManagedObjectType}.
		 */
		private final ManagedObjectType<?> managedObjectType;

		/**
		 * {@link SectionManagedObject}.
		 */
		private final SectionManagedObject managedObject;

		/**
		 * {@link ObjectDecoration}.
		 */
		private final ObjectDecoration objectDecoration;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectType {@link ManagedObjectType}.
		 * @param managedObject     {@link SectionManagedObject}.
		 * @param objectDecoration  {@link ObjectDecoration}.
		 */
		private SectionClassManagedObject(ManagedObjectType<?> managedObjectType, SectionManagedObject managedObject,
				ObjectDecoration objectDecoration) {
			this.managedObjectType = managedObjectType;
			this.managedObject = managedObject;
			this.objectDecoration = objectDecoration;
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
	 * Captures whether further configuration is necessary.
	 */
	private static class FurtherConfiguration {

		/**
		 * Indicates if further configuration is required.
		 */
		boolean isRequireFurtherConfiguration = false;
	}

	/**
	 * Handles whether configured.
	 */
	private static class AbstractConfigurationItem {

		/**
		 * Indicates if configured.
		 */
		private boolean isConfigured = false;

		/**
		 * Indicates if configured and subsequently assumes to be configured.
		 * 
		 * @return <code>true</code> if already configured.
		 */
		protected boolean isAlreadyConfigured() {
			boolean isConfigured = this.isConfigured;
			this.isConfigured = true;
			return isConfigured;
		}

		/**
		 * Loads to {@link FurtherConfiguration} whether configuration still required.
		 * 
		 * @param furtherConfiguration {@link FurtherConfiguration}.
		 */
		protected void loadRequireConfigure(FurtherConfiguration furtherConfiguration) {
			if (!this.isConfigured) {
				furtherConfiguration.isRequireFurtherConfiguration = true;
			}
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
