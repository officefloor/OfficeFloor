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
	public ObjectDependencyType loadObjectDependencyType(
			ManagedObjectType<?> managedObjectType) {

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

		// Obtain the dependent object
		DependentObjectType dependentObjectType = null;
		if (this.linkedObjectNode != null) {

			// TODO implement
			throw new UnsupportedOperationException("TODO implement "
					+ this.getClass().getSimpleName() + " dependent object");

		}

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

		// Ensure not already linked
		if (this.linkedObjectNode != null) {
			this.context.getCompilerIssues().addIssue(
					this,
					"Managed object dependency " + this.dependencyName
							+ " linked more than once");
			return false; // already linked
		}

		// Link
		this.linkedObjectNode = node;
		return true;
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}