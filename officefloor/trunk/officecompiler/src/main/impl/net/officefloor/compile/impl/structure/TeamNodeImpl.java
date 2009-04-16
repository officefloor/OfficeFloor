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

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * {@link TeamNode} implementation.
 * 
 * @author Daniel
 */
public class TeamNodeImpl implements TeamNode {

	/**
	 * Name of this {@link OfficeFloorTeam}.
	 */
	private final String teamName;

	/**
	 * Class name of the {@link TeamSource}.
	 */
	private final String teamSourceClassName;

	/**
	 * {@link PropertyList} to source the {@link Team}.
	 */
	private final PropertyList propertyList = new PropertyListImpl();

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of this {@link OfficeFloorTeam}.
	 * @param teamSourceClassName
	 *            Class name of the {@link TeamSource}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public TeamNodeImpl(String teamName, String teamSourceClassName,
			String officeFloorLocation, CompilerIssues issues) {
		this.teamName = teamName;
		this.teamSourceClassName = teamSourceClassName;
		this.officeFloorLocation = officeFloorLocation;
		this.issues = issues;
	}

	/*
	 * ============= OfficeFloorTeam ====================================
	 */

	@Override
	public String getOfficeFloorTeamName() {
		return this.teamName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	/*
	 * =============== LinkTeamNode ===================================
	 */

	/**
	 * Linked {@link LinkTeamNode}.
	 */
	private LinkTeamNode linkedTeamNode;

	@Override
	public boolean linkTeamNode(LinkTeamNode node) {
		// Link
		this.linkedTeamNode = node;
		return true;
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}