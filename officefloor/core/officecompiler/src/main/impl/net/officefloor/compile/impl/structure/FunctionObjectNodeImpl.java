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
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.impl.object.ObjectDependencyTypeImpl;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.DependentObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.FunctionObjectNode;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.type.TypeContext;
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
	public ObjectDependencyType loadObjectDependencyType(TypeContext typeContext) {

		// Obtain the function type
		ManagedFunctionType<?, ?> functionType = this.functionNode.loadManagedFunctionType(typeContext);
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
		String dependencyType = object.getObjectType().getName();
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
			dependentObjectType = dependentObjectNode.loadDependentObjectType(typeContext);
			if (dependentObjectType == null) {
				return null;
			}
		}

		// Create and return the type
		return new ObjectDependencyTypeImpl(this.objectName, dependencyType, typeQualifier, this.isParameter,
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