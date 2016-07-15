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

import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
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
	public ManagedObjectDependencyNodeImpl(String dependencyName,
			LocationType locationType, String location, NodeContext context) {
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