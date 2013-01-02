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

import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.GovernerableManagedObject;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Implementation of the {@link GovernanceNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceNodeImpl implements GovernanceNode {

	/**
	 * Name of this {@link OfficeGovernance}.
	 */
	private final String governanceName;

	/**
	 * Class name of the {@link GovernanceSource}.
	 */
	private final String governanceSourceClassName;

	/**
	 * {@link GovernanceSource} instance to use. Should this be specified it
	 * overrides the {@link Class}.
	 */
	@SuppressWarnings("unused")
	private final GovernanceSource<?, ?> governanceSource;

	/**
	 * {@link PropertyList} to source the {@link Governance}.
	 */
	private final PropertyList properties = new PropertyListImpl();

	/**
	 * Location of the {@link Office} containing this {@link OfficeGovernance}.
	 */
	private final String officeLocation;

	/**
	 * {@link OfficeNode} of the {@link Office} containing this
	 * {@link Governance}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link GovernanceType} for this {@link GovernanceNode}.
	 */
	private GovernanceType<?, ?> governanceType = null;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of this {@link OfficeGovernance}.
	 * @param governanceSourceClassName
	 *            Class name of the {@link GovernanceSource}.
	 * @param officeLocation
	 *            Location of the {@link Office} containing this
	 *            {@link OfficeGovernance}.
	 * @param officeNode
	 *            {@link OfficeNode} of the {@link Office} containing this
	 *            {@link Governance}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public GovernanceNodeImpl(String governanceName,
			String governanceSourceClassName, String officeLocation,
			OfficeNode officeNode, NodeContext context) {
		this.governanceName = governanceName;
		this.governanceSourceClassName = governanceSourceClassName;
		this.governanceSource = null;
		this.officeLocation = officeLocation;
		this.officeNode = officeNode;
		this.context = context;
	}

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of this {@link OfficeGovernance}.
	 * @param governanceSourceClassName
	 *            {@link GovernanceSource} instance to use.
	 * @param officeLocation
	 *            Location of the {@link Office} containing this
	 *            {@link OfficeGovernance}.
	 * @param officeNode
	 *            {@link OfficeNode} of the {@link Office} containing this
	 *            {@link Governance}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public GovernanceNodeImpl(String governanceName,
			GovernanceSource<?, ?> governanceSource, String officeLocation,
			OfficeNode officeNode, NodeContext context) {
		this.governanceName = governanceName;
		this.governanceSourceClassName = null;
		this.governanceSource = governanceSource;
		this.officeLocation = officeLocation;
		this.officeNode = officeNode;
		this.context = context;
	}

	/*
	 * ======================== GovernanceNode ======================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public GovernanceType<?, ?> getGovernanceType() {

		// Lazy load the governance type
		if (this.governanceType != null) {
			return this.governanceType; // already loaded
		}

		// Obtain the governance source class
		Class governanceSourceClass = this.context.getGovernanceSourceClass(
				this.governanceSourceClassName, this.officeLocation,
				this.governanceName);
		if (governanceSourceClass == null) {
			return null; // must obtain source class
		}

		// Load the governance type
		GovernanceLoader loader = this.context.getGovernanceLoader(
				this.officeLocation, this.governanceName);
		this.governanceType = loader.loadGovernanceType(governanceSourceClass,
				this.properties);

		// Return the governance type
		return this.governanceType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildGovernance(OfficeBuilder officeBuilder) {

		// Obtain the governance type
		GovernanceType govType = this.getGovernanceType();
		if (govType == null) {
			return; // must obtain governance type
		}

		// Build the governance
		GovernanceBuilder govBuilder = officeBuilder.addGovernance(
				this.governanceName, govType.getGovernanceFactory(),
				govType.getExtensionInterface());

		// Obtain the office team responsible for this governance
		OfficeTeam officeTeam = LinkUtil.retrieveTarget(this, OfficeTeam.class,
				"Governance " + this.governanceName, LocationType.OFFICE,
				this.officeLocation, AssetType.GOVERNANCE, this.governanceName,
				this.context.getCompilerIssues());
		if (officeTeam != null) {
			// Build the team responsible for the governance
			govBuilder.setTeam(officeTeam.getOfficeTeamName());
		}
	}

	/*
	 * ======================== OfficeGovernance ======================
	 */

	@Override
	public String getOfficeGovernanceName() {
		return this.governanceName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public void governManagedObject(GovernerableManagedObject managedObject) {

		// Register governance with the managed object
		if (managedObject instanceof ManagedObjectNode) {
			// Register governance with the managed object node
			ManagedObjectNode managedObjectNode = (ManagedObjectNode) managedObject;
			managedObjectNode.addGovernance(this, this.officeNode);

		} else if (managedObject instanceof OfficeObjectNode) {
			// Register governance with the office object node
			OfficeObjectNode officeObjectNode = (OfficeObjectNode) managedObject;
			officeObjectNode.addGovernance(this);

		} else {
			// Unknown governerable managed object node
			this.context.getCompilerIssues().addIssue(
					LocationType.OFFICE,
					this.officeLocation,
					AssetType.GOVERNANCE,
					this.governanceName,
					"Unknown "
							+ GovernerableManagedObject.class.getSimpleName()
							+ " node");
		}
	}

	/*
	 * ========================== LinkTeamNode ========================
	 */

	/**
	 * Linked {@link LinkTeamNode}.
	 */
	private LinkTeamNode linkedTeamNode = null;

	@Override
	public boolean linkTeamNode(LinkTeamNode node) {

		// Ensure not already linked
		if (this.linkedTeamNode != null) {
			// Team already linked
			this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
					this.officeLocation, AssetType.GOVERNANCE,
					this.governanceName, "Team already assigned");
			return false; // already linked
		}

		// Link
		this.linkedTeamNode = node;
		return true;
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}