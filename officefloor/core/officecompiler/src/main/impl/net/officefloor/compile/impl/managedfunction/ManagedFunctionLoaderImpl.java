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

package net.officefloor.compile.impl.managedfunction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceProperty;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.IssueTarget;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ManagedFunctionLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionLoaderImpl implements ManagedFunctionLoader, IssueTarget {

	/**
	 * {@link Node} requiring the {@link ManagedFunction} instances.
	 */
	private final Node node;

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Indicates using to load type.
	 */
	private final boolean isLoadingType;

	/**
	 * Initiate for building.
	 * 
	 * @param node          {@link Node} requiring the {@link ManagedFunction}
	 *                      instances.
	 * @param officeNode    {@link OfficeNode}. May be <code>null</code> if not
	 *                      loading within {@link OfficeNode}.
	 * @param nodeContext   {@link NodeContext}.
	 * @param isLoadingType Indicates using to load type.
	 */
	public ManagedFunctionLoaderImpl(Node node, OfficeNode officeNode, NodeContext nodeContext, boolean isLoadingType) {
		this.node = node;
		this.officeNode = officeNode;
		this.nodeContext = nodeContext;
		this.isLoadingType = isLoadingType;
	}

	/*
	 * ====================== ManagedFunctionLoader ======================
	 */

	@Override
	public <S extends ManagedFunctionSource> PropertyList loadSpecification(Class<S> managedFunctionSourceClass) {

		// Instantiate the managed function source
		ManagedFunctionSource managedFunctionSource = CompileUtil.newInstance(managedFunctionSourceClass,
				ManagedFunctionSource.class, this.node, this.nodeContext.getCompilerIssues());
		if (managedFunctionSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the specification
		return this.loadSpecification(managedFunctionSource);
	}

	@Override
	public PropertyList loadSpecification(ManagedFunctionSource managedFunctionSource) {

		// Obtain the specification
		ManagedFunctionSourceSpecification specification;
		try {
			specification = managedFunctionSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + ManagedFunctionSourceSpecification.class.getSimpleName() + " from "
					+ managedFunctionSource.getClass().getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + ManagedFunctionSourceSpecification.class.getSimpleName() + " returned from "
					+ managedFunctionSource.getClass().getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		ManagedFunctionSourceProperty[] managedFunctionProperties;
		try {
			managedFunctionProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + ManagedFunctionSourceProperty.class.getSimpleName() + " instances from "
					+ ManagedFunctionSourceSpecification.class.getSimpleName() + " for "
					+ managedFunctionSource.getClass().getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (managedFunctionProperties != null) {
			for (int i = 0; i < managedFunctionProperties.length; i++) {
				ManagedFunctionSourceProperty namespaceProperty = managedFunctionProperties[i];

				// Ensure have the managed function property
				if (namespaceProperty == null) {
					this.addIssue(ManagedFunctionSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ ManagedFunctionSourceSpecification.class.getSimpleName() + " for "
							+ managedFunctionSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = namespaceProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + ManagedFunctionSourceProperty.class.getSimpleName() + " "
							+ i + " from " + ManagedFunctionSourceSpecification.class.getSimpleName() + " for "
							+ managedFunctionSource.getClass().getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(ManagedFunctionSourceProperty.class.getSimpleName() + " " + i
							+ " provided blank name from " + ManagedFunctionSourceSpecification.class.getSimpleName()
							+ " for " + managedFunctionSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = namespaceProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + ManagedFunctionSourceProperty.class.getSimpleName() + " "
							+ i + " (" + name + ") from " + ManagedFunctionSourceSpecification.class.getSimpleName()
							+ " for " + managedFunctionSource.getClass().getName(), ex);
					return null; // must have complete property details
				}

				// Add to the properties
				propertyList.addProperty(name, label);
			}
		}

		// Return the property list
		return propertyList;
	}

	@Override
	public <S extends ManagedFunctionSource> FunctionNamespaceType loadManagedFunctionType(
			Class<S> managedFunctionSourceClass, PropertyList propertyList) {

		// Instantiate the managed function source
		ManagedFunctionSource managedFunctionSource = CompileUtil.newInstance(managedFunctionSourceClass,
				ManagedFunctionSource.class, this.node, this.nodeContext.getCompilerIssues());
		if (managedFunctionSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the type
		return this.loadManagedFunctionType(managedFunctionSource, propertyList);
	}

	@Override
	public FunctionNamespaceType loadManagedFunctionType(ManagedFunctionSource managedFunctionSource,
			PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName();

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName,
				this.officeNode, propertyList);

		// Create the managed function source context
		String[] additionalProfiles = this.nodeContext.additionalProfiles(this.officeNode);
		ManagedFunctionSourceContext context = new ManagedFunctionSourceContextImpl(qualifiedName, this.isLoadingType,
				additionalProfiles, overriddenProperties, this.nodeContext);

		// Create the namespace type builder
		FunctionNamespaceTypeImpl namespaceType = new FunctionNamespaceTypeImpl();

		try {
			// Source the managed function type
			managedFunctionSource.sourceManagedFunctions(namespaceType, context);

		} catch (AbstractSourceError ex) {
			ex.addIssue(this);
			return null; // must have property

		} catch (Throwable ex) {
			this.addIssue("Failed to source " + FunctionNamespaceType.class.getSimpleName() + " definition from "
					+ ManagedFunctionSource.class.getSimpleName() + " " + managedFunctionSource.getClass().getName(),
					ex);
			return null; // must be successful
		}

		// Ensure has at least one function
		ManagedFunctionType<?, ?>[] functionTypes = namespaceType.getManagedFunctionTypes();
		if (functionTypes.length == 0) {
			this.addIssue("No " + ManagedFunctionType.class.getSimpleName() + " definitions provided by "
					+ ManagedFunctionSource.class.getSimpleName() + " " + managedFunctionSource.getClass().getName());
			return null; // must have complete type
		}

		// Determine if duplicate function names
		if (this.isDuplicateNaming(functionTypes, (functionType) -> functionType.getFunctionName(), "Two or more "
				+ ManagedFunctionType.class.getSimpleName() + " definitions with the same name (${NAME}) provided by "
				+ ManagedFunctionSource.class.getSimpleName() + " " + managedFunctionSource.getClass().getName())) {
			return null; // must have valid type
		}

		// Ensure the function definitions are valid
		for (int i = 0; i < functionTypes.length; i++) {
			if (!this.isValidFunctionType(functionTypes[i], i, managedFunctionSource.getClass())) {
				return null; // must be completely valid type
			}
		}

		// Return the namespace type
		return namespaceType;
	}

	/**
	 * Determines if the input {@link ManagedFunctionType} is valid.
	 * 
	 * @param functionType               {@link ManagedFunctionType} to validate.
	 * @param functionIndex              Index of the {@link ManagedFunctionType} on
	 *                                   the {@link FunctionNamespaceType}.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} providing the
	 *                                   {@link FunctionNamespaceType}.
	 * @return <code>true</code> if {@link ManagedFunctionType} is valid.
	 */
	private <M extends Enum<M>, F extends Enum<F>> boolean isValidFunctionType(ManagedFunctionType<M, F> functionType,
			int functionIndex, Class<?> managedFunctionSourceClass) {

		// Ensure has name
		String functionName = functionType.getFunctionName();
		if (CompileUtil.isBlank(functionName)) {
			this.addFunctionIssue("No function name provided for", functionIndex, functionName,
					managedFunctionSourceClass);
			return false; // must have complete type
		}

		// Ensure has function factory
		if (functionType.getManagedFunctionFactory() == null) {
			this.addFunctionIssue("No " + ManagedFunctionFactory.class.getSimpleName() + " provided for", functionIndex,
					functionName, managedFunctionSourceClass);
			return false; // must have complete type
		}

		// Obtain the object keys class (taking into account Indexed)
		Class<M> objectKeysClass = functionType.getObjectKeyClass();
		if ((objectKeysClass != null) && (!objectKeysClass.equals(Indexed.class))) {
			// Is valid by keys
			if (!this.isValidObjects(functionType, objectKeysClass, functionIndex, functionName,
					managedFunctionSourceClass)) {
				return false; // must be valid
			}
		} else {
			// Is valid by indexes
			if (!this.isValidObjects(functionType, functionIndex, functionName, managedFunctionSourceClass)) {
				return false; // must be valid
			}
		}

		// Validate common details for objects
		ManagedFunctionObjectType<M>[] objectTypes = functionType.getObjectTypes();
		for (ManagedFunctionObjectType<M> objectType : objectTypes) {

			// Must have names for objects
			String objectName = objectType.getObjectName();
			if (CompileUtil.isBlank(objectName)) {
				this.addFunctionIssue("No object name on", functionIndex, functionName, managedFunctionSourceClass);
				return false; // not valid
			}

			// Must have object types
			if (objectType.getObjectType() == null) {
				this.addFunctionIssue("No object type provided for object " + objectName + " on", functionIndex,
						functionName, managedFunctionSourceClass);
				return false; // not valid
			}
		}

		// Validate no duplicate object names
		if (this.isDuplicateNaming(objectTypes, (item) -> item.getObjectName(),
				this.getFunctionIssueDescription(
						"Two or more " + ManagedFunctionObjectType.class.getSimpleName()
								+ " definitions with the same name (${NAME}) for",
						functionIndex, functionName, managedFunctionSourceClass))) {
			return false; // must have valid type
		}

		// Obtain the flow keys class (taking into account Indexed)
		Class<F> flowKeysClass = functionType.getFlowKeyClass();
		if ((flowKeysClass != null) && (!flowKeysClass.equals(Indexed.class))) {
			// Is valid by keys
			if (!this.isValidFlows(functionType, flowKeysClass, functionIndex, functionName,
					managedFunctionSourceClass)) {
				return false; // must be valid
			}
		} else {
			// Is valid by indexes
			if (!this.isValidFlows(functionType, functionIndex, functionName, managedFunctionSourceClass)) {
				return false; // must be valid
			}
		}

		// Validate common details for flows
		ManagedFunctionFlowType<F>[] flowTypes = functionType.getFlowTypes();
		for (ManagedFunctionFlowType<F> flowType : flowTypes) {

			// Must have names for flows
			if (CompileUtil.isBlank(flowType.getFlowName())) {
				this.addFunctionIssue("No flow name on", functionIndex, functionName, managedFunctionSourceClass);
				return false; // not valid
			}
		}

		// Validate no duplicate flow names
		if (this.isDuplicateNaming(flowTypes, new NameExtractor<ManagedFunctionFlowType<F>>() {
			@Override
			public String extractName(ManagedFunctionFlowType<F> item) {
				return item.getFlowName();
			}
		}, this.getFunctionIssueDescription(
				"Two or more " + ManagedFunctionFlowType.class.getSimpleName()
						+ " definitions with the same name (${NAME}) for",
				functionIndex, functionName, managedFunctionSourceClass))) {
			return false; // must have valid type
		}

		// Validate the escalations
		ManagedFunctionEscalationType[] escalationTypes = functionType.getEscalationTypes();
		for (ManagedFunctionEscalationType escalationType : escalationTypes) {

			// Must have escalation type
			if (escalationType.getEscalationType() == null) {
				this.addFunctionIssue("No escalation type on", functionIndex, functionName, managedFunctionSourceClass);
				return false; // not valid
			}

			// Must have names for escalations
			if (CompileUtil.isBlank(escalationType.getEscalationName())) {
				this.addFunctionIssue("No escalation name on", functionIndex, functionName, managedFunctionSourceClass);
				return false; // not valid
			}
		}

		// Validate no duplicate escalation names
		if (this.isDuplicateNaming(escalationTypes, new NameExtractor<ManagedFunctionEscalationType>() {
			@Override
			public String extractName(ManagedFunctionEscalationType item) {
				return item.getEscalationName();
			}
		}, this.getFunctionIssueDescription(
				"Two or more " + ManagedFunctionEscalationType.class.getSimpleName()
						+ " definitions with the same name (${NAME}) for",
				functionIndex, functionName, managedFunctionSourceClass))) {
			return false; // must have valid type
		}

		// If here then valid
		return true;
	}

	/**
	 * Determines that the {@link ManagedFunctionObjectType} instances are valid
	 * given an {@link Enum} providing the dependent {@link Object} keys.
	 * 
	 * @param functionType               {@link ManagedFunctionType}.
	 * @param objectKeysClass            {@link Enum} providing the keys.
	 * @param functionIndex              Index of the {@link ManagedFunctionType} on
	 *                                   the {@link FunctionNamespaceType}.
	 * @param functionName               Name of the {@link ManagedFunctionType}.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} class.
	 * @return <code>true</code> if {@link ManagedFunctionObjectType} instances are
	 *         valid.
	 */
	private <M extends Enum<M>> boolean isValidObjects(ManagedFunctionType<M, ?> functionType, Class<M> objectKeysClass,
			int functionIndex, String functionName, Class<?> managedFunctionSourceClass) {

		// Obtain the keys are sort by ordinal just to be sure
		M[] keys = objectKeysClass.getEnumConstants();
		Arrays.sort(keys, new Comparator<M>() {
			@Override
			public int compare(M a, M b) {
				return a.ordinal() - b.ordinal();
			}
		});

		// Validate the function object types
		ManagedFunctionObjectType<M>[] objectTypes = functionType.getObjectTypes();
		for (int i = 0; i < keys.length; i++) {
			M key = keys[i];
			ManagedFunctionObjectType<M> objectType = (objectTypes.length > i ? objectTypes[i] : null);

			// Ensure object type for the key
			if (objectType == null) {
				this.addFunctionIssue(
						"No " + ManagedFunctionObjectType.class.getSimpleName() + " provided for key " + key + " on",
						functionIndex, functionName, managedFunctionSourceClass);
				return false; // not valid
			}

			// Ensure have key identifying the object
			M typeKey = objectType.getKey();
			if (typeKey == null) {
				this.addFunctionIssue("No key provided for an object on", functionIndex, functionName,
						managedFunctionSourceClass);
				return false; // not valid
			}

			// Ensure the key is of correct type
			if (!objectKeysClass.isInstance(typeKey)) {
				this.addFunctionIssue(
						"Incorrect key type (" + typeKey.getClass().getName() + ") provided for an object on",
						functionIndex, functionName, managedFunctionSourceClass);
				return false; // not valid
			}

			// Ensure is the correct expected key
			if (key != typeKey) {
				this.addIssue("Incorrect object key (" + typeKey + ") as was expecting " + key + " on");
				return false; // not valid
			}

			// Ensure the index matches the ordinal for the key
			int index = objectType.getIndex();
			if (index != key.ordinal()) {
				this.addIssue("Index (" + index + ") of object does match ordinal of key (" + key + ") on");
				return false; // not valid
			}
		}

		// Ensure there are no addition objects than keys
		if (objectTypes.length > keys.length) {
			this.addFunctionIssue("More objects than keys on", functionIndex, functionName, managedFunctionSourceClass);
			return false; // not valid
		}

		// If here then valid
		return true;
	}

	/**
	 * Determines that the {@link ManagedFunctionObjectType} instances are valid
	 * given they are {@link Indexed}.
	 * 
	 * @param functionType               {@link ManagedFunctionType}.
	 * @param functionIndex              Index of the {@link ManagedFunctionType} on
	 *                                   the {@link FunctionNamespaceType}.
	 * @param functionName               Name of the {@link ManagedFunctionType}.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} class.
	 * @return <code>true</code> if {@link ManagedFunctionObjectType} instances are
	 *         valid.
	 */
	private boolean isValidObjects(ManagedFunctionType<?, ?> functionType, int functionIndex, String functionName,
			Class<?> managedFunctionSourceClass) {

		// Validate the function object types
		ManagedFunctionObjectType<?>[] objectTypes = functionType.getObjectTypes();
		for (int i = 0; i < objectTypes.length; i++) {
			ManagedFunctionObjectType<?> objectType = objectTypes[i];

			// Ensure no key on object
			Enum<?> key = objectType.getKey();
			if (key != null) {
				this.addFunctionIssue("Objects are not keyed but object has key on", functionIndex, functionName,
						managedFunctionSourceClass);
				return false; // not valid
			}

			// Ensure the index is correct
			if (objectType.getIndex() != i) {
				this.addFunctionIssue("Object indexes are out of order on", functionIndex, functionName,
						managedFunctionSourceClass);
				return false; // not valid
			}
		}

		// If here then valid
		return true;
	}

	/**
	 * Determines that the {@link ManagedFunctionFlowType} instances are valid given
	 * an {@link Enum} providing the instigated {@link Flow} keys.
	 * 
	 * @param functionType               {@link ManagedFunctionType}.
	 * @param objectKeysClass            {@link Enum} providing the keys.
	 * @param functionIndex              Index of the {@link ManagedFunctionType} on
	 *                                   the {@link FunctionNamespaceType}.
	 * @param functionName               Name of the {@link ManagedFunctionType}.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} class.
	 * @return <code>true</code> if {@link ManagedFunctionFlowType} instances are
	 *         valid.
	 */
	private <F extends Enum<F>> boolean isValidFlows(ManagedFunctionType<?, F> functionType, Class<F> flowKeysClass,
			int functionIndex, String functionName, Class<?> managedFunctionSourceClass) {

		// Obtain the keys are sort by ordinal just to be sure
		F[] keys = flowKeysClass.getEnumConstants();
		Arrays.sort(keys, new Comparator<F>() {
			@Override
			public int compare(F a, F b) {
				return a.ordinal() - b.ordinal();
			}
		});

		// Validate the function flow types
		ManagedFunctionFlowType<F>[] flowTypes = functionType.getFlowTypes();
		for (int i = 0; i < keys.length; i++) {
			F key = keys[i];
			ManagedFunctionFlowType<F> flowType = (flowTypes.length > i ? flowTypes[i] : null);

			// Ensure flow type for the key
			if (flowType == null) {
				this.addFunctionIssue(
						"No " + ManagedFunctionFlowType.class.getSimpleName() + " provided for key " + key + " on",
						functionIndex, functionName, managedFunctionSourceClass);
				return false; // not valid
			}

			// Ensure have key identifying the object
			F typeKey = flowType.getKey();
			if (typeKey == null) {
				this.addFunctionIssue("No key provided for a flow on", functionIndex, functionName,
						managedFunctionSourceClass);
				return false; // not valid
			}

			// Ensure the key is of correct type
			if (!flowKeysClass.isInstance(typeKey)) {
				this.addFunctionIssue(
						"Incorrect key type (" + typeKey.getClass().getName() + ") provided for a flow on",
						functionIndex, functionName, managedFunctionSourceClass);
				return false; // not valid
			}

			// Ensure is the correct expected key
			if (key != typeKey) {
				this.addIssue("Incorrect flow key (" + typeKey + ") as was expecting " + key + " on");
				return false; // not valid
			}

			// Ensure the index matches the ordinal for the key
			int index = flowType.getIndex();
			if (index != key.ordinal()) {
				this.addIssue("Index (" + index + ") of flow does match ordinal of key (" + key + ") on");
				return false; // not valid
			}
		}

		// Ensure there are no addition flows than keys
		if (flowTypes.length > keys.length) {
			this.addFunctionIssue("More flows than keys on", functionIndex, functionName, managedFunctionSourceClass);
			return false; // not valid
		}

		// If here then valid
		return true;
	}

	/**
	 * Determines that the {@link ManagedFunctionFlowType} instances are valid given
	 * they are {@link Indexed}.
	 * 
	 * @param functionType               {@link ManagedFunctionType}.
	 * @param functionIndex              Index of the {@link ManagedFunctionType} on
	 *                                   the {@link FunctionNamespaceType}.
	 * @param functionName               Name of the {@link ManagedFunctionType}.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} class.
	 * @return <code>true</code> if {@link ManagedFunctionFlowType} instances are
	 *         valid.
	 */
	private boolean isValidFlows(ManagedFunctionType<?, ?> functionType, int functionIndex, String functionName,
			Class<?> managedFunctionSourceClass) {

		// Validate the function flow types
		ManagedFunctionFlowType<?>[] flowTypes = functionType.getFlowTypes();
		for (int i = 0; i < flowTypes.length; i++) {
			ManagedFunctionFlowType<?> flowType = flowTypes[i];

			// Ensure no key on flow
			Enum<?> key = flowType.getKey();
			if (key != null) {
				this.addFunctionIssue("Flows are not keyed but flow has key on", functionIndex, functionName,
						managedFunctionSourceClass);
				return false; // not valid
			}

			// Ensure the index is correct
			if (flowType.getIndex() != i) {
				this.addFunctionIssue("Flow indexes are out of order on", functionIndex, functionName,
						managedFunctionSourceClass);
				return false; // not valid
			}
		}

		// If here then valid
		return true;
	}

	/**
	 * <p>
	 * Determines if there are duplicate item names.
	 * <p>
	 * Duplicate names are reported as issues.
	 * 
	 * @param items            Items to be checked for unique naming.
	 * @param extractor        {@link NameExtractor}.
	 * @param issueDescription {@link CompilerIssues} description if duplicate name
	 * @return <code>true</code> if there are duplicate item names.
	 */
	private <N> boolean isDuplicateNaming(N[] items, NameExtractor<N> extractor, String issueDescription) {

		// Determine if duplicate name
		boolean isDuplicateName = false;
		Set<String> checkedNames = new HashSet<String>();
		for (N item : items) {
			String name = extractor.extractName(item);
			if (name != null) {

				// Ignore if name already checked (stops repetitive issues)
				if (checkedNames.contains(name)) {
					continue; // already checked
				}

				// Determine if name occurs more than once
				int nameCount = 0;
				for (N check : items) {
					String checkName = extractor.extractName(check);
					if (name.equals(checkName)) {
						nameCount++;
					}
				}
				if (nameCount > 1) {

					// More than one item with name so duplicate name
					isDuplicateName = true;

					// Duplicate name so prepare message
					String reportMessage = issueDescription.replace("${NAME}", name);

					// Report the issue
					this.addIssue(reportMessage);
				}

				// Name checked
				checkedNames.add(name);
			}
		}

		// Return whether duplicate names
		return isDuplicateName;
	}

	/**
	 * Extracts the name from the object.
	 */
	private static interface NameExtractor<N> {

		/**
		 * Extracts the particular name from the item.
		 * 
		 * @param item Item to have name extracted.
		 * @return
		 */
		String extractName(N item);
	}

	/**
	 * Obtains the {@link ManagedFunction} issue description.
	 * 
	 * @param issueDescription           Description of the issue.
	 * @param functionIndex              Index of the {@link ManagedFunction}.
	 * @param functionName               Name of the {@link ManagedFunction}.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} class.
	 * @return {@link ManagedFunction} issue description.
	 */
	private String getFunctionIssueDescription(String issueDescription, int functionIndex, String functionName,
			Class<?> managedFunctionSourceClass) {
		return issueDescription + " " + ManagedFunctionType.class.getSimpleName() + " definition " + functionIndex
				+ (functionName == null ? "" : " (" + functionName + ")") + " by "
				+ ManagedFunctionSource.class.getSimpleName() + " " + managedFunctionSourceClass.getName();
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription           Description of the issue.
	 * @param functionIndex              Index of the {@link ManagedFunction}.
	 * @param functionName               Name of the {@link ManagedFunction}.
	 * @param managedFunctionSourceClass {@link ManagedFunctionSource} class.
	 */
	private void addFunctionIssue(String issueDescription, int functionIndex, String functionName,
			Class<?> managedFunctionSourceClass) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, this.getFunctionIssueDescription(issueDescription,
				functionIndex, functionName, managedFunctionSourceClass));
	}

	/*
	 * ================== IssueTarget ========================
	 */

	@Override
	public void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

}
