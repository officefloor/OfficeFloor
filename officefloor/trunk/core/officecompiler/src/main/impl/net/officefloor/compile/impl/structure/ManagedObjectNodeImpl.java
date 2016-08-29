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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.office.TypeQualification;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.governance.Governance;
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
	private final Map<String, ManagedObjectDependencyNode> dependencies = new HashMap<String, ManagedObjectDependencyNode>();

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
	 * Containing {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode containingOfficeFloorNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link TypeQualification} instances for this {@link ManagedObjectNode}.
	 */
	private final List<TypeQualification> typeQualifications = new LinkedList<TypeQualification>();

	/**
	 * {@link GovernanceNode} instances to provide {@link Governance} over this
	 * {@link ManagedObjectNode} within the specified {@link OfficeNode}.
	 */
	private final Map<OfficeNode, List<GovernanceNode>> governancesPerOffice = new HashMap<OfficeNode, List<GovernanceNode>>();

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of this {@link ManagedObject}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} of the {@link ManagedObjectNode}.
	 * @param managedObjectSourcNode
	 *            {@link ManagedObjectSourceNode} for the
	 *            {@link ManagedObjectSource} to source this
	 *            {@link ManagedObject}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectNodeImpl(String managedObjectName,
			ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourcNode, NodeContext context) {
		this.managedObjectName = managedObjectName;
		this.managedObjectScope = managedObjectScope;
		this.managedObjectSourceNode = managedObjectSourcNode;
		this.containingSectionNode = this.managedObjectSourceNode
				.getSectionNode();
		this.containingOfficeNode = (this.containingSectionNode != null ? this.containingSectionNode
				.getOfficeNode() : this.managedObjectSourceNode.getOfficeNode());
		this.containingOfficeFloorNode = (this.containingOfficeNode != null ? this.containingOfficeNode
				.getOfficeFloorNode() : this.managedObjectSourceNode
				.getOfficeFloorNode());
		this.context = context;
	}

	/*
	 * ===================== Node ===========================
	 */

	@Override
	public String getNodeName() {
		// TODO implement Node.getNodeName
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeName");

	}

	@Override
	public String getNodeType() {
		// TODO implement Node.getNodeType
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeType");

	}

	@Override
	public String getLocation() {
		// TODO implement Node.getLocation
		throw new UnsupportedOperationException(
				"TODO implement Node.getLocation");

	}

	@Override
	public Node getParentNode() {
		// TODO implement Node.getParentNode
		throw new UnsupportedOperationException(
				"TODO implement Node.getParentNode");

	}

	/*
	 * ===================== BoundManagedObjectNode ===========================
	 */

	@Override
	public String getBoundManagedObjectName() {
		// Obtain the name based on location
		if (this.containingSectionNode != null) {
			// Use name qualified with both office and section
			return this.containingOfficeNode.getDeployedOfficeName()
					+ "."
					+ this.containingSectionNode
							.getSectionQualifiedName(this.managedObjectName);

		} else if (this.containingOfficeNode != null) {
			// Use name qualified with office name
			return this.containingOfficeNode.getDeployedOfficeName() + "."
					+ this.managedObjectName;

		} else {
			// Use name unqualified
			return this.managedObjectName;
		}
	}

	@Override
	public void addGovernance(GovernanceNode governance, OfficeNode office) {

		// Obtain the listing of governances for the office
		List<GovernanceNode> governances = this.governancesPerOffice
				.get(office);
		if (governances == null) {
			// Create and register listing to add the governance
			governances = new LinkedList<GovernanceNode>();
			this.governancesPerOffice.put(office, governances);
		}

		// Add the governance for the specified office
		governances.add(governance);
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
			ManagedObjectDependencyNode dependencyNode = this.dependencies
					.get(dependencyName);
			BoundManagedObjectNode dependency = LinkUtil.retrieveTarget(
					dependencyNode, BoundManagedObjectNode.class,
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

		// Obtain the governances for the office
		List<GovernanceNode> governances = this.governancesPerOffice
				.get(office);
		if (governances != null) {
			// Load the governance for the managed object
			for (GovernanceNode governance : governances) {
				mapper.mapGovernance(governance.getOfficeGovernanceName());
			}
		}
	}

	/*
	 * ========================== (Common) ===============================
	 */

	@Override
	public void addTypeQualification(String qualifier, String type) {
		this.typeQualifications.add(new TypeQualificationImpl(qualifier, type));
	}

	@Override
	public ManagedObjectDependency getManagedObjectDependency(
			String managedObjectDependencyName) {
		// Obtain and return the dependency for the name
		ManagedObjectDependencyNode dependency = this.dependencies
				.get(managedObjectDependencyName);
		if (dependency == null) {
			// Create the managed object dependency
			dependency = this.context.createManagedObjectDependencyNode(
					managedObjectDependencyName, this);

			// Add the managed object dependency
			this.dependencies.put(managedObjectDependencyName, dependency);
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
	 * =================== GovernerableManagedObject =========================
	 */

	@Override
	public String getGovernerableManagedObjectName() {
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