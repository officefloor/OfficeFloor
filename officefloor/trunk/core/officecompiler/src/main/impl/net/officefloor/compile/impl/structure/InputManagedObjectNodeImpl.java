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

import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link InputManagedObjectNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class InputManagedObjectNodeImpl implements InputManagedObjectNode {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Name of this {@link InputManagedObjectNode}.
	 */
	private final String inputManagedObjectName;

	/**
	 * Bound {@link ManagedObjectSourceNode}.
	 */
	private ManagedObjectSourceNode boundManagedObjectSource = null;

	/**
	 * Listing of {@link GovernanceNode} instances for the particular
	 * {@link OfficeNode}.
	 */
	private final Map<OfficeNode, List<GovernanceNode>> governancesPerOffice = new HashMap<OfficeNode, List<GovernanceNode>>();

	/**
	 * {@link OfficeNode} instances that this {@link InputManagedObjectNode} has
	 * been built into.
	 */
	private final Set<OfficeNode> builtOffices = new HashSet<OfficeNode>();

	/**
	 * Initiate.
	 * 
	 * @param inputManagedObjectName
	 *            Name of this {@link InputManagedObjectNode}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public InputManagedObjectNodeImpl(String inputManagedObjectName,
			String officeFloorLocation, NodeContext context) {
		this.inputManagedObjectName = inputManagedObjectName;
		this.officeFloorLocation = officeFloorLocation;
		this.context = context;
	}

	/*
	 * ======================= InputManagedObjectNode =========================
	 */

	@Override
	public ManagedObjectSourceNode getBoundManagedObjectSourceNode() {
		return this.boundManagedObjectSource;
	}

	/*
	 * ======================= BoundManagedObjectNode =========================
	 */

	@Override
	public String getBoundManagedObjectName() {
		return this.inputManagedObjectName;
	}

	@Override
	public void addGovernance(GovernanceNode governance, OfficeNode office) {

		// Obtain the listing of governances for the office
		List<GovernanceNode> governances = this.governancesPerOffice
				.get(office);
		if (governances == null) {
			governances = new LinkedList<GovernanceNode>();
			this.governancesPerOffice.put(office, governances);
		}

		// Add the governance
		governances.add(governance);
	}

	@Override
	public void buildOfficeManagedObject(OfficeNode office,
			OfficeBuilder officeBuilder) {

		// Determine if already built into the office
		if (this.builtOffices.contains(office)) {
			return; // already built into the office
		}

		// Provide binding to managed object source if specified
		if (this.boundManagedObjectSource != null) {
			officeBuilder.setBoundInputManagedObject(
					this.getBoundManagedObjectName(),
					this.boundManagedObjectSource.getManagedObjectSourceName());
		}

		// Flag that built into the office
		this.builtOffices.add(office);
	}

	/*
	 * ================== OfficeFloorInputManagedObject =======================
	 */

	@Override
	public String getOfficeFloorInputManagedObjectName() {
		return this.inputManagedObjectName;
	}

	@Override
	public void setBoundOfficeFloorManagedObjectSource(
			OfficeFloorManagedObjectSource managedObjectSource) {

		// Ensure is a Managed Object Source Node
		if (!(managedObjectSource instanceof ManagedObjectSourceNode)) {
			this.context.getCompilerIssues().addIssue(
					LocationType.OFFICE_FLOOR,
					this.officeFloorLocation,
					null,
					null,
					"Invalid managed object source node: "
							+ managedObjectSource
							+ " ["
							+ (managedObjectSource == null ? null
									: managedObjectSource.getClass().getName())
							+ ", required "
							+ ManagedObjectSourceNode.class.getName() + "]");
			return; // can not bind
		}

		// Ensure not already bound
		if (this.boundManagedObjectSource != null) {
			this.context.getCompilerIssues().addIssue(
					LocationType.OFFICE_FLOOR,
					this.officeFloorLocation,
					null,
					null,
					"Managed Object Source already bound for Input Managed Object '"
							+ this.inputManagedObjectName + "'");
			return; // already bound
		}

		// Bind the managed object source
		this.boundManagedObjectSource = (ManagedObjectSourceNode) managedObjectSource;
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

	@Override
	public GovernanceNode[] getGovernances(OfficeNode managingOffice) {

		// Obtain the governances
		List<GovernanceNode> governances = this.governancesPerOffice
				.get(managingOffice);

		// Return the governances
		return (governances == null ? new GovernanceNode[0] : governances
				.toArray(new GovernanceNode[governances.size()]));
	}

}