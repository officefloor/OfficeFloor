/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectNode} implementation.
 * 
 * @author Daniel
 */
public class ManagedObjectNodeImpl implements ManagedObjectNode {

	/**
	 * Name of this {@link SectionManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link ManagedObjectDependencyNode} instances by their
	 * {@link ManagedObjectDependency} names.
	 */
	private final Map<String, ManagedObjectDependencyNode> depedencies = new HashMap<String, ManagedObjectDependencyNode>();

	/**
	 * {@link LocationType} of the location containing this
	 * {@link ManagedObject}.
	 */
	private final LocationType locationType;

	/**
	 * Location containing this {@link ManagedObject}.
	 */
	private final String location;

	/**
	 * {@link ManagedObjectSourceNode} for the {@link ManagedObjectSource} to
	 * source this {@link ManagedObject}.
	 */
	private final ManagedObjectSourceNode managedObjectSourceNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of this {@link SectionManagedObject}.
	 * @param locationType
	 *            {@link LocationType} of the location containing this
	 *            {@link ManagedObject}.
	 * @param location
	 *            Location containing this {@link ManagedObject}.
	 * @param managedObjectSourcNode
	 *            {@link ManagedObjectSourceNode} for the
	 *            {@link ManagedObjectSource} to source this
	 *            {@link ManagedObject}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectNodeImpl(String managedObjectName,
			LocationType locationType, String location,
			ManagedObjectSourceNode managedObjectSourcNode, NodeContext context) {
		this.managedObjectName = managedObjectName;
		this.locationType = locationType;
		this.location = location;
		this.managedObjectSourceNode = managedObjectSourcNode;
		this.context = context;
	}

	/*
	 * ===================== ManagedObjectNode ================================
	 */

	@Override
	public void registerToOffice(OfficeNode office, OfficeObject object,
			OfficeBuilder officeBuilder) {

		// Register to the office
		officeBuilder.registerManagedObjectSource(object.getOfficeObjectName(),
				this.managedObjectName);
	}

	@Override
	public void buildOfficeManagedObject(OfficeNode office,
			OfficeBuilder officeBuilder) {

		// Add the managed object to the office
		// DependencyMappingBuilder mapper = officeBuilder
		// .addProcessManagedObject(officeManagedObjectName,
		// officeManagedObjectName);

		// TODO provide the dependencies for the managed object
	}

	/*
	 * ========================== (Common) ===============================
	 */

	@Override
	public ManagedObjectDependency getManagedObjectDependency(
			String managedObjectDependencyName) {
		// Obtain and return the dependency for the name
		ManagedObjectDependencyNode dependency = this.depedencies
				.get(managedObjectDependencyName);
		if (dependency == null) {
			// Create the managed object dependency
			dependency = new ManagedObjectDependencyNodeImpl(
					managedObjectDependencyName, this.locationType,
					this.location, this.context);

			// Add the managed object dependency
			this.depedencies.put(managedObjectDependencyName, dependency);
		}
		return dependency;
	}

	/*
	 * ==================== SectionManagedObject ===============================
	 */

	@Override
	public String getSectionManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * ================ OfficeSectionManagedObject ============================
	 */

	@Override
	public String getOfficeSectionManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public Class<?>[] getSupportedExtensionInterfaces() {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = this.managedObjectSourceNode
				.getManagedObjectType();

		// Return the supported extension interfaces
		if (managedObjectType == null) {
			// Issue in loading type, no extension interfaces
			return new Class[0];
		} else {
			// Return the extension interfaces supported by the managed object
			return managedObjectType.getExtensionInterfaces();
		}
	}

	/*
	 * ======================= OfficeManagedObject ============================
	 */

	@Override
	public String getOfficeManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * ==================== OfficeFloorManagedObject ===========================
	 */

	@Override
	public String getOfficeFloorManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * =================== DependentManagedObject ==============================
	 */

	@Override
	public String getDependentManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * =================== AdministerableManagedObject =========================
	 */

	@Override
	public String getAdministerableManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * =================== LinkObjectNode ======================================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectName;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		// Link
		this.linkedObjectName = node;
		return true;
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectName;
	}

}