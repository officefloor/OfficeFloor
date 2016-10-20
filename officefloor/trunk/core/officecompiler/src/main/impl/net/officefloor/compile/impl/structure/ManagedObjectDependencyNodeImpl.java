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
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.type.TypeContext;

/**
 * {@link ManagedObjectDependencyNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyNodeImpl implements
		ManagedObjectDependencyNode {

	/**
	 * Name of this {@link ManagedObjectDependency}.
	 */
	private final String dependencyName;

	/**
	 * Parent {@link ManagedObjectNode} or {@link ManagedObjectSourceNode}.
	 */
	private final Node parent;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link ManagedObjectSourceNode} for this
	 * {@link ManagedObjectDependencyNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSource;

	/**
	 * Initiate.
	 * 
	 * @param dependencyName
	 *            Name of this {@link ManagedObjectDependency}.
	 * @param managedObject
	 *            Parent {@link ManagedObjectNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectDependencyNodeImpl(String dependencyName,
			ManagedObjectNode managedObject, NodeContext context) {
		this.dependencyName = dependencyName;
		this.parent = managedObject;
		this.context = context;

		// Store the managed object source
		this.managedObjectSource = managedObject.getManagedObjectSourceNode();
	}

	/**
	 * Initiate.
	 * 
	 * @param dependencyName
	 *            Name of this {@link ManagedObjectDependency}.
	 * @param managedObjectSource
	 *            Parent {@link ManagedObjectSourceNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectDependencyNodeImpl(String dependencyName,
			ManagedObjectSourceNode managedObjectSource, NodeContext context) {
		this.dependencyName = dependencyName;
		this.parent = managedObjectSource;
		this.context = context;

		// Keep track of the managed object source
		this.managedObjectSource = managedObjectSource;
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
		return this.parent;
	}

	@Override
	public boolean isInitialised() {
		// TODO implement Node.isInitialised
		throw new UnsupportedOperationException(
				"TODO implement Node.isInitialised");

	}

	@Override
	public void initialise() {
		// TODO implement ManagedObjectDependencyNode.initialise
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectDependencyNode.initialise");

	}

	/*
	 * ==================== ManagedObjectDependency ============================
	 */

	@Override
	public String getManagedObjectDependencyName() {
		return this.dependencyName;
	}

	/**
	 * ===================== ObjectDependencyNode ==============================
	 */

	@Override
	public ObjectDependencyType loadObjectDependencyType(TypeContext typeContext) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = typeContext
				.getOrLoadManagedObjectType(this.managedObjectSource);
		if (managedObjectType == null) {
			return null; // must have type
		}

		// Obtain the dependency type
		ManagedObjectDependencyType<?> dependency = null;
		for (ManagedObjectDependencyType<?> moDependency : managedObjectType
				.getDependencyTypes()) {
			if (this.dependencyName.equals(moDependency.getDependencyName())) {
				dependency = moDependency;
			}
		}
		if (dependency == null) {
			this.context.getCompilerIssues().addIssue(
					this,
					ManagedObjectSourceNode.TYPE + " does not have dependency "
							+ this.dependencyName);
			return null;
		}

		// Obtain the type information
		String dependencyType = dependency.getDependencyType().getName();
		String typeQualifier = dependency.getTypeQualifier();

		// Obtain dependent object
		DependentObjectNode dependentObjectNode = LinkUtil
				.retrieveFurtherestTarget(this, DependentObjectNode.class,
						this.context.getCompilerIssues());
		if (dependentObjectNode == null) {
			this.context.getCompilerIssues().addIssue(
					this,
					ManagedObjectDependencyNode.TYPE + " "
							+ this.dependencyName + " is not linked to a "
							+ DependentObjectNode.TYPE);
			return null;
		}

		// Obtain the dependent object type
		DependentObjectType dependentObjectType = dependentObjectNode
				.loadDependentObjectType(typeContext);

		// Create and return the dependency type
		return new ObjectDependencyTypeImpl(this.dependencyName,
				dependencyType, typeQualifier, false, dependentObjectType);
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
		return LinkUtil.linkObjectNode(this, node,
				this.context.getCompilerIssues(),
				(link) -> this.linkedObjectNode = link);
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}