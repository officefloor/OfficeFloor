/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectNodeImpl implements ManagedObjectNode {

	/**
	 * Name of this {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link ManagedObjectScope} of this {@link ManagedObject}.
	 */
	private final ManagedObjectScope managedObjectScope;

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
	 * Containing {@link SectionNode}. <code>null</code> if contained in the
	 * {@link Office} or {@link OfficeFloor}.
	 */
	private final SectionNode containingSectionNode;

	/**
	 * Containing {@link OfficeNode}. <code>null</code> if contained in the
	 * {@link OfficeFloor}.
	 */
	private final OfficeNode containingOfficeNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of this {@link ManagedObject}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} of the {@link ManagedObjectNode}.
	 * @param locationType
	 *            {@link LocationType} of the location containing this
	 *            {@link ManagedObject}.
	 * @param location
	 *            Location containing this {@link ManagedObject}.
	 * @param managedObjectSourcNode
	 *            {@link ManagedObjectSourceNode} for the
	 *            {@link ManagedObjectSource} to source this
	 *            {@link ManagedObject}.
	 * @param containingSectionNode
	 *            Containing {@link SectionNode}. <code>null</code> if contained
	 *            in the {@link Office} or {@link OfficeFloor}.
	 * @param containingOfficeNode
	 *            Containing {@link OfficeNode}. <code>null</code> if contained
	 *            in the {@link OfficeFloor}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectNodeImpl(String managedObjectName,
			ManagedObjectScope managedObjectScope, LocationType locationType,
			String location, ManagedObjectSourceNode managedObjectSourcNode,
			SectionNode containingSectionNode, OfficeNode containingOfficeNode,
			NodeContext context) {
		this.managedObjectName = managedObjectName;
		this.managedObjectScope = managedObjectScope;
		this.locationType = locationType;
		this.location = location;
		this.managedObjectSourceNode = managedObjectSourcNode;
		this.containingSectionNode = containingSectionNode;
		this.containingOfficeNode = containingOfficeNode;
		this.context = context;
	}

	/*
	 * ===================== BoundManagedObjectNode ===========================
	 */

	@Override
	public String getBoundManagedObjectName() {
		// Obtain the name based on location
		switch (this.locationType) {
		case OFFICE_FLOOR:
			// Use name unqualified
			return this.managedObjectName;

		case OFFICE:
			// Use name qualified with office name
			return this.containingOfficeNode.getDeployedOfficeName() + "."
					+ this.managedObjectName;

		case SECTION:
			// Use name qualified with both office and section
			return this.containingOfficeNode.getDeployedOfficeName()
					+ "."
					+ this.containingSectionNode
							.getSectionQualifiedName(this.managedObjectName);

		default:
			throw new IllegalStateException("Unknown location type");
		}
	}

	/**
	 * {@link OfficeNode} instances that this {@link ManagedObject} has already
	 * been built into.
	 */
	private final Set<OfficeNode> builtOffices = new HashSet<OfficeNode>();

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildOfficeManagedObject(OfficeNode office,
			OfficeBuilder officeBuilder) {

		// Ensure not already built into the office
		if (this.builtOffices.contains(office)) {
			return; // already built into the office
		}
		this.builtOffices.add(office);

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = this.managedObjectSourceNode
				.getManagedObjectType();
		if (managedObjectType == null) {
			return; // must have managed object type
		}

		// Obtain the managed object name
		String managedObjectName = this.getBoundManagedObjectName();

		// Register to the office
		officeBuilder.registerManagedObjectSource(managedObjectName,
				this.managedObjectSourceNode.getManagedObjectSourceName());

		// Add the managed object to the office
		DependencyMappingBuilder mapper;
		switch (this.managedObjectScope) {
		case PROCESS:
			mapper = officeBuilder.addProcessManagedObject(managedObjectName,
					managedObjectName);
			break;
		case THREAD:
			mapper = officeBuilder.addThreadManagedObject(managedObjectName,
					managedObjectName);
			break;
		case WORK:
			// work not built here
			return;
		default:
			throw new IllegalStateException("Unknown managed object scope "
					+ this.managedObjectScope);
		}

		// Load the dependencies for the managed object
		for (ManagedObjectDependencyType<?> dependencyType : managedObjectType
				.getDependencyTypes()) {

			// Obtain the dependency type details
			String dependencyName = dependencyType.getDependencyName();
			Enum dependencyKey = dependencyType.getKey();
			int dependencyIndex = dependencyType.getIndex();

			// Obtain the dependency
			ManagedObjectDependencyNode dependencyNode = this.depedencies
					.get(dependencyName);
			BoundManagedObjectNode dependency = LinkUtil.retrieveTarget(
					dependencyNode, BoundManagedObjectNode.class, "Dependency "
							+ dependencyName, this.locationType, this.location,
					AssetType.MANAGED_OBJECT, this.managedObjectName,
					this.context.getCompilerIssues());
			if (dependency == null) {
				continue; // must have dependency
			}

			// Ensure the dependent managed object is built into the office
			dependency.buildOfficeManagedObject(office, officeBuilder);

			// Link the dependency
			String dependentManagedObjectName = dependency
					.getBoundManagedObjectName();
			if (dependencyKey != null) {
				mapper.mapDependency(dependencyKey, dependentManagedObjectName);
			} else {
				mapper.mapDependency(dependencyIndex,
						dependentManagedObjectName);
			}
		}
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