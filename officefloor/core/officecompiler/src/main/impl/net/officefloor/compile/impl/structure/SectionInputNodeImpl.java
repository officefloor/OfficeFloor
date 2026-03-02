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

package net.officefloor.compile.impl.structure;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.section.OfficeSectionInputTypeImpl;
import net.officefloor.compile.impl.section.SectionInputTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.ExternalServiceInputNode;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.spi.office.ExecutionExplorer;
import net.officefloor.compile.spi.office.ExecutionExplorerContext;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.ExternalServiceCleanupEscalationHandler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.internal.structure.ExternalServiceInputFactory;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link SectionInputNode} node.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionInputNodeImpl implements SectionInputNode {

	/**
	 * Name of the {@link SectionInputType}.
	 */
	private final String inputName;

	/**
	 * {@link SectionNode} containing this {@link SectionInputNode}.
	 */
	private final SectionNode section;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link InitialisedState}.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

		/**
		 * Parameter type.
		 */
		private String parameterType;

		/**
		 * Instantiate.
		 * 
		 * @param parameterType Parameter type.
		 */
		public InitialisedState(String parameterType) {
			this.parameterType = parameterType;
		}
	}

	/**
	 * {@link ExternalServiceProxyFunctionManager}.
	 */
	private ExternalServiceProxyFunctionManager externalFunctionManager = null;

	/**
	 * {@link ExecutionExplorer} instances.
	 */
	private final List<ExecutionExplorer> executionExplorers = new LinkedList<>();

	/**
	 * Annotations.
	 */
	private final List<Object> annotations = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param inputName Name of the {@link SubSectionInput} (which is the name of
	 *                  the {@link SectionInputType}).
	 * @param section   {@link SectionNode} containing this
	 *                  {@link SectionInputNode}.
	 * @param context   {@link NodeContext}.
	 */
	public SectionInputNodeImpl(String inputName, SectionNode section, NodeContext context) {
		this.inputName = inputName;
		this.section = section;
		this.context = context;
	}

	/*
	 * ================== Node ========================
	 */

	@Override
	public String getNodeName() {
		return this.inputName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return this.section;
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes();
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String parameterType) {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState(parameterType));
	}

	/*
	 * ===================== SectionInputNode ===========================
	 */

	@Override
	public void loadExternalServicing(Office office) throws UnknownFunctionException {

		// Determine if externally triggered
		if (this.externalFunctionManager == null) {
			return; // not externally triggered
		}

		// Obtain the function node servicing this section input
		ManagedFunctionNode functionNode = LinkUtil.retrieveTarget(this, ManagedFunctionNode.class,
				this.context.getCompilerIssues());
		if (functionNode == null) {
			// Compiled, so this should not occur
			return; // must have function
		}

		// Obtain the corresponding function manager
		String functionName = functionNode.getQualifiedFunctionName();
		FunctionManager functionManager = office.getFunctionManager(functionName);

		// Load the function to the proxy
		this.externalFunctionManager.delegate = functionManager;
	}

	@Override
	public SectionInputType loadSectionInputType(CompileContext compileContext) {

		// Ensure have input name
		if (CompileUtil.isBlank(this.inputName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have names for inputs
		}

		// Create and return type
		return new SectionInputTypeImpl(this.inputName, this.state.parameterType,
				this.annotations.toArray(new Object[this.annotations.size()]));
	}

	@Override
	public OfficeSectionInputType loadOfficeSectionInputType(CompileContext compileContext) {
		return new OfficeSectionInputTypeImpl(this.inputName, this.state.parameterType,
				this.annotations.toArray(new Object[this.annotations.size()]));
	}

	@Override
	public boolean runExecutionExplorers(Map<String, ManagedFunctionNode> managedFunctions,
			CompileContext compileContext) {

		// Run the execution explorer
		ExecutionManagedFunction initialFunction = null;
		for (ExecutionExplorer explorer : this.executionExplorers) {

			// Lazy obtain function servicing this section input
			if (initialFunction == null) {
				ManagedFunctionNode functionNode = LinkUtil.retrieveTarget(this, ManagedFunctionNode.class,
						this.context.getCompilerIssues());
				if (functionNode != null) {
					initialFunction = functionNode.createExecutionManagedFunction(compileContext);
				}
			}

			// Explore from this input
			try {
				final ExecutionManagedFunction finalInitialFunction = initialFunction;
				explorer.explore(new ExecutionExplorerContext() {

					@Override
					public ExecutionManagedFunction getManagedFunction(String functionName) {

						// Obtain the managed function node
						ManagedFunctionNode function = managedFunctions.get(functionName);
						if (function == null) {
							return null;
						}

						// Create and return the execution managed function
						return function.createExecutionManagedFunction(compileContext);
					}

					@Override
					public ExecutionManagedFunction getInitialManagedFunction() {
						return finalInitialFunction;
					}
				});
			} catch (Throwable ex) {
				this.context.getCompilerIssues().addIssue(this, "Failure in exploring execution", ex);
			}
		}

		// As here, successfully explored
		return true;
	}

	/*
	 * ================= SectionInput =========================
	 */

	@Override
	public String getSectionInputName() {
		return this.inputName;
	}

	@Override
	public void addAnnotation(Object annotation) {
		this.annotations.add(annotation);
	}

	/*
	 * =================== SubSectionInput ========================
	 */

	@Override
	public String getSubSectionInputName() {
		return this.inputName;
	}

	/*
	 * ===================== OfficeSectionInput =====================
	 */

	@Override
	public OfficeSection getOfficeSection() {
		return this.section;
	}

	@Override
	public String getOfficeSectionInputName() {
		return this.inputName;
	}

	@Override
	public void addExecutionExplorer(ExecutionExplorer executionExplorer) {
		this.executionExplorers.add(executionExplorer);
	}

	/*
	 * ====================== DeployedOfficeInput ====================
	 */

	@Override
	public String getDeployedOfficeInputName() {
		return this.inputName;
	}

	@Override
	public DeployedOffice getDeployedOffice() {
		return this.section.getOfficeNode();
	}

	@Override
	public FunctionManager getFunctionManager() {

		// Lazy create the function manager
		if (this.externalFunctionManager == null) {
			this.externalFunctionManager = new ExternalServiceProxyFunctionManager();
		}

		// Return the external function manager
		return this.externalFunctionManager;
	}

	@Override
	public <O, M extends ManagedObject> ExternalServiceInput<O, M> addExternalServiceInput(Class<O> objectType,
			Class<M> managedObjectType,
			ExternalServiceCleanupEscalationHandler<? super M> cleanupEscalationHandler) {
		return this.addExternalServiceInput(objectType, null, managedObjectType, cleanupEscalationHandler);
	}

	@Override
	public <O, M extends ManagedObject> ExternalServiceInput<O, M> addExternalServiceInput(Class<O> objectType,
			String typeQualifier, Class<M> managedObjectType,
			ExternalServiceCleanupEscalationHandler<? super M> cleanupEscalationHandler) {

        // Add to OfficeFloor (to make available for auto-wiring)
        OfficeNode office = this.section.getOfficeNode();
        OfficeFloorNode officeFloor = office.getOfficeFloorNode();

        // Obtain the factory to create external service input
        ExternalServiceInputFactory<O, M> factory = officeFloor.addExternalServiceInputFactory(objectType, typeQualifier, managedObjectType, office, cleanupEscalationHandler);

        // Create the external service input
        ExternalServiceInputNode<O, M> input = factory.createExternalServiceInput(this);

        // Link flow for handling
        LinkUtil.linkFlow(input.getOfficeFloorManagedObjectFlow(), this,
                this.context.getCompilerIssues(), this);

        // Return the external service input
		return input.getExternalServiceInput();
	}

	/*
	 * =================== LinkFlowNode ============================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode = null;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {
		return LinkUtil.linkFlowNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedFlowNode = link);
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

	/**
	 * Proxy {@link FunctionManager} for external service handling.
	 */
	private static class ExternalServiceProxyFunctionManager implements FunctionManager {

		/**
		 * Delegate {@link FunctionManager}.
		 */
		private FunctionManager delegate = null;

		@Override
		public Object[] getAnnotations() {
			return this.delegate.getAnnotations();
		}

		@Override
		public Class<?> getParameterType() {
			return this.delegate.getParameterType();
		}

		@Override
		public ProcessManager invokeProcess(Object parameter, FlowCallback callback)
				throws InvalidParameterTypeException {
			return this.delegate.invokeProcess(parameter, callback);
		}
	}

}
