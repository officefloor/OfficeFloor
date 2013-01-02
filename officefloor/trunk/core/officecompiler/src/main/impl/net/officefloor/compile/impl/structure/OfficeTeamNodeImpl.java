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

import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeTeamNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeTeamNodeImpl implements OfficeTeamNode {

	/**
	 * Name of this {@link OfficeTeam}.
	 */
	private final String teamName;

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Flag indicating if in {@link OfficeFloor} context.
	 */
	private boolean isInOfficeFloorContext = false;

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private String officeFloorLocation;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of this {@link OfficeTeam}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeTeamNodeImpl(String teamName, String officeLocation,
			NodeContext context) {
		this.teamName = teamName;
		this.officeLocation = officeLocation;
		this.context = context;
	}

	/*
	 * ================== OfficeTeamNode ============================
	 */

	@Override
	public void addOfficeFloorContext(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;
		this.isInOfficeFloorContext = true;
	}

	/*
	 * ================= OfficeTeam ==============================
	 */

	@Override
	public String getOfficeTeamName() {
		return this.teamName;
	}

	/*
	 * ================== ManagedObjectTeam =========================
	 */

	@Override
	public String getManagedObjectTeamName() {
		return this.teamName;
	}

	/*
	 * ================== LinkTeamNode ============================
	 */

	/**
	 * Linked {@link LinkTeamNode}.
	 */
	private LinkTeamNode linkedTeamNode = null;

	@Override
	public boolean linkTeamNode(LinkTeamNode node) {

		// Ensure not already linked
		if (this.linkedTeamNode != null) {
			if (this.isInOfficeFloorContext) {
				// Deployed office team
				this.context.getCompilerIssues().addIssue(
						LocationType.OFFICE_FLOOR, this.officeFloorLocation,
						null, null, this.teamName + " already assigned");
			} else {
				// Office required team
				this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
						this.officeLocation, null, null,
						this.teamName + " already assigned");
			}
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