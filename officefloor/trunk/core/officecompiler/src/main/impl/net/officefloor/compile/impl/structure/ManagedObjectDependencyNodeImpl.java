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

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.UnknownType;
import net.officefloor.compile.spi.section.ManagedObjectDependency;

/**
 * {@link ManagedObjectDependencyNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyNodeImpl implements
		ManagedObjectDependencyNode {

	/**
	 * {@link ManagedObjectSourceNode} for the {@link ManagedObjectNode}
	 * containing this {@link ManagedObjectDependency}.
	 */
	private final ManagedObjectSourceNode managedObjectSourceNode;

	/**
	 * Name of this {@link ManagedObjectDependency}.
	 */
	private final String dependencyName;

	/**
	 * {@link LocationType} of the location containing this
	 * {@link ManagedObjectDependencyNode}.
	 */
	private final LocationType locationType;

	/**
	 * Location containing this {@link ManagedObjectDependencyNode}.
	 */
	private final String location;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode} for the
	 *            {@link ManagedObjectNode} containing this
	 *            {@link ManagedObjectDependency}.
	 * @param dependencyName
	 *            Name of this {@link ManagedObjectDependency}.
	 * @param locationType
	 *            {@link LocationType} of the location containing this
	 *            {@link ManagedObjectDependencyNode}.
	 * @param location
	 *            Location containing this {@link ManagedObjectDependencyNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectDependencyNodeImpl(
			ManagedObjectSourceNode managedObjectSourceNode,
			String dependencyName, LocationType locationType, String location,
			NodeContext context) {
		this.managedObjectSourceNode = managedObjectSourceNode;
		this.dependencyName = dependencyName;
		this.locationType = locationType;
		this.location = location;
		this.context = context;
	}

	/*
	 * ==================== ManagedObjectDependency ============================
	 */

	@Override
	public String getManagedObjectDependencyName() {
		return this.dependencyName;
	}

	/*
	 * ========================= ObjectDependency =============================
	 */

	@Override
	public String getObjectDependencyName() {
		return this.dependencyName;
	}

	@Override
	public Class<?> getObjectDependencyType() {

		// Obtain the managed object dependency type
		ManagedObjectDependencyType<?> dependencyType = this
				.getManagedObjectDependencyType();
		if (dependencyType == null) {
			// Failed to obtain dependency type, so type unknown
			return UnknownType.class;
		}

		// Return the dependency type
		return dependencyType.getDependencyType();
	}

	@Override
	public String getObjectDependencyTypeQualifier() {

		// Obtain the managed object dependency type
		ManagedObjectDependencyType<?> dependencyType = this
				.getManagedObjectDependencyType();
		if (dependencyType == null) {
			// Failed to obtain dependency type, so no type qualifier
			return null;
		}

		// Return the dependency type qualifier
		return dependencyType.getTypeQualifier();
	}

	/**
	 * Obtains the {@link ManagedObjectDependencyType} for this
	 * {@link ManagedObjectDependencyNode}.
	 * 
	 * @return {@link ManagedObjectDependencyType} for this
	 *         {@link ManagedObjectDependencyNode}. May be <code>null</code> if
	 *         not able to obtain {@link ManagedObjectDependencyType}.
	 */
	private ManagedObjectDependencyType<?> getManagedObjectDependencyType() {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = this.managedObjectSourceNode
				.getManagedObjectType();
		if (managedObjectType == null) {
			// Failed to obtain managed object type, so no dependency type
			return null;
		}

		// Find the corresponding dependency type
		for (ManagedObjectDependencyType<?> dependencyType : managedObjectType
				.getDependencyTypes()) {
			if (this.dependencyName.equals(dependencyType.getDependencyName())) {
				// Return the dependency type
				return dependencyType;
			}
		}

		// As here, not dependency type
		return null;
	}

	@Override
	public DependentManagedObject getDependentManagedObject() {

		// Return the retrieved dependent managed object
		return LinkUtil.retrieveTarget(this, DependentManagedObject.class,
				"ManagedObjectDependency " + this.dependencyName,
				this.locationType, this.location, null, null,
				this.context.getCompilerIssues());
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
					this.locationType,
					this.location,
					null,
					null,
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