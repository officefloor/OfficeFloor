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

import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeTeamNode} implementation.
 * 
 * @author Daniel
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
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

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
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public OfficeTeamNodeImpl(String teamName, String officeLocation,
			CompilerIssues issues) {
		this.teamName = teamName;
		this.officeLocation = officeLocation;
		this.issues = issues;
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
				this.issues.addIssue(LocationType.OFFICE_FLOOR,
						this.officeFloorLocation, null, null, this.teamName
								+ " already assigned");
			} else {
				// Office required team
				this.issues.addIssue(LocationType.OFFICE, this.officeLocation,
						null, null, this.teamName + " already assigned");
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