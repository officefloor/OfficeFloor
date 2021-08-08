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
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.DependentObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFunctionDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;

/**
 * {@link ManagedObjectFunctionDependencyNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFunctionDependencyNodeImpl implements ManagedObjectFunctionDependencyNode {

	/**
	 * Name of this {@link ManagedObjectDependency}.
	 */
	private final String dependencyName;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link ManagedObjectSourceNode} for this {@link ManagedObjectDependencyNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSource;

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
	 * Initiate.
	 * 
	 * @param dependencyName      Name of this {@link ManagedObjectDependency}.
	 * @param managedObjectSource Parent {@link ManagedObjectSourceNode}.
	 * @param context             {@link NodeContext}.
	 */
	public ManagedObjectFunctionDependencyNodeImpl(String dependencyName, ManagedObjectSourceNode managedObjectSource,
			NodeContext context) {
		this.dependencyName = dependencyName;
		this.managedObjectSource = managedObjectSource;
		this.context = context;
	}

	/*
	 * ==================== Node ============================
	 */

	@Override
	public String getNodeName() {
		return this.dependencyName;
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
		return this.managedObjectSource;
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
	 * ==================== ManagedObjectDependency ============================
	 */

	@Override
	public String getManagedObjectDependencyName() {
		return this.dependencyName;
	}

	@Override
	public void setOverrideQualifier(String qualifier) {
		// TODO implement setOverrideQualifier
		throw new UnsupportedOperationException("TODO implement setOverrideQualifier");
	}

	@Override
	public void setSpecificType(String type) {
		// TODO implement setSpecificType
		throw new UnsupportedOperationException("TODO implement setSpecificType");
	}

	/**
	 * ===================== ObjectDependencyNode ==============================
	 */

	@Override
	public ObjectDependencyType loadObjectDependencyType(CompileContext compileContext) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext.getOrLoadManagedObjectType(this.managedObjectSource);
		if (managedObjectType == null) {
			return null; // must have type
		}

		// Obtain the function dependency type
		ManagedObjectFunctionDependencyType functionDependency = null;
		for (ManagedObjectFunctionDependencyType moDependency : managedObjectType.getFunctionDependencyTypes()) {
			if (this.dependencyName.equals(moDependency.getFunctionObjectName())) {
				functionDependency = moDependency;
			}
		}
		if (functionDependency == null) {
			this.context.getCompilerIssues().addIssue(this,
					ManagedObjectSourceNode.TYPE + " does not have function dependency " + this.dependencyName);
			return null;
		}

		// Obtain the type information
		Class<?> dependencyType = functionDependency.getFunctionObjectType();
		String typeQualifier = null;

		// Obtain dependent object
		DependentObjectNode dependentObjectNode = LinkUtil.retrieveFurtherestTarget(this, DependentObjectNode.class,
				this.context.getCompilerIssues());
		if (dependentObjectNode == null) {
			return null;
		}

		// Obtain the dependent object type
		DependentObjectType dependentObjectType = dependentObjectNode.loadDependentObjectType(compileContext);

		// Create and return the dependency type
		return new ObjectDependencyTypeImpl(this.dependencyName, dependencyType.getName(), typeQualifier, false,
				dependentObjectType);
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
