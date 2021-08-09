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

import net.officefloor.compile.impl.object.ObjectDependencyTypeImpl;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.DependentObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.FunctionObjectNode;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link FunctionObjectNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionObjectNodeImpl implements FunctionObjectNode {

	/**
	 * Name of this {@link FunctionObject}.
	 */
	private final String objectName;

	/**
	 * {@link ManagedFunctionNode} containing this {@link FunctionObjectNode}.
	 */
	private final ManagedFunctionNode functionNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {
	}

	/**
	 * Flag indicating if this {@link FunctionObject} is a parameter to the
	 * {@link ManagedFunction}.
	 */
	private boolean isParameter = false;

	/**
	 * Initiate.
	 * 
	 * @param objectName
	 *            Name of this {@link FunctionObject}.
	 * @param functionNode
	 *            {@link ManagedFunctionNode} containing this
	 *            {@link FunctionObjectNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public FunctionObjectNodeImpl(String objectName, ManagedFunctionNode functionNode, NodeContext context) {
		this.objectName = objectName;
		this.functionNode = functionNode;
		this.context = context;
	}

	/*
	 * ==================== Node ============================
	 */

	@Override
	public String getNodeName() {
		return this.objectName;
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
		return this.functionNode;
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
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState());
	}

	/*
	 * ==================== FunctionObjectNode ============================
	 */

	@Override
	public boolean isParameter() {
		return this.isParameter;
	}

	/*
	 * ================= ObjectDependencyNode =========================
	 */

	@Override
	public ObjectDependencyType loadObjectDependencyType(CompileContext compileContext) {

		// Obtain the function type
		ManagedFunctionType<?, ?> functionType = this.functionNode.loadManagedFunctionType(compileContext);
		if (functionType == null) {
			return null;
		}

		// Obtain the function object type
		ManagedFunctionObjectType<?> object = null;
		for (ManagedFunctionObjectType<?> objectType : functionType.getObjectTypes()) {
			if (this.objectName.equals(objectType.getObjectName())) {
				object = objectType;
			}
		}
		if (object == null) {
			this.context.getCompilerIssues().addIssue(this,
					FunctionObjectNode.TYPE + " does not have object " + this.objectName);
			return null;
		}

		// Obtain the type information
		Class<?> dependencyType = object.getObjectType();
		String typeQualifier = object.getTypeQualifier();

		// Obtain the dependent object type
		DependentObjectType dependentObjectType = null;
		if (!this.isParameter) {

			// Not parameter, so must obtain dependent object
			DependentObjectNode dependentObjectNode = LinkUtil.retrieveFurtherestTarget(this, DependentObjectNode.class,
					this.context.getCompilerIssues());
			if (dependentObjectNode == null) {
				return null;
			}

			// Obtain the dependent object type
			dependentObjectType = dependentObjectNode.loadDependentObjectType(compileContext);
			if (dependentObjectType == null) {
				return null;
			}
		}

		// Create and return the type
		return new ObjectDependencyTypeImpl(this.objectName, dependencyType.getName(), typeQualifier, this.isParameter,
				dependentObjectType);
	}

	/*
	 * ===================== FunctionObject ===============================
	 */

	@Override
	public String getFunctionObjectName() {
		return this.objectName;
	}

	@Override
	public void flagAsParameter() {
		this.isParameter = true;
	}

	/*
	 * ================= AugmentedFunctionObject ===========================
	 */

	@Override
	public boolean isLinked() {
		return (this.linkedObjectNode != null);
	}

	/*
	 * ===================== LinkObjectNode ===========================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectNode;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		return LinkUtil.linkObjectNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedObjectNode = link);
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}
