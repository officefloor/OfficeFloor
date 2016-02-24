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

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * {@link TeamNode} implementation.
 * 
 * @author Daniel Sagenschneider
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
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Indicates if the {@link TeamType} is loaded.
	 */
	private boolean isTeamTypeLoaded = false;

	/**
	 * Loaded {@link TeamType}.
	 */
	private TeamType teamType;

	/**
	 * Indicates if the {@link OfficeFloorTeamSourceType} is loaded.
	 */
	private boolean isOfficeFloorTeamSourceTypeLoaded = false;

	/**
	 * Loaded {@link OfficeFloorTeamSourceType}.
	 */
	private OfficeFloorTeamSourceType teamSourceType;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of this {@link OfficeFloorTeam}.
	 * @param teamSourceClassName
	 *            Class name of the {@link TeamSource}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public TeamNodeImpl(String teamName, String teamSourceClassName,
			String officeFloorLocation, NodeContext context) {
		this.teamName = teamName;
		this.teamSourceClassName = teamSourceClassName;
		this.officeFloorLocation = officeFloorLocation;
		this.context = context;
	}

	/*
	 * =============== TeamNode ======================================
	 */

	@Override
	public boolean hasTeamSource() {
		return !CompileUtil.isBlank(this.teamSourceClassName);
	}

	@Override
	public void loadTeamType() {

		// Only load the team type once (whether successful or not)
		if (this.isTeamTypeLoaded) {
			return;
		}
		this.isTeamTypeLoaded = true;

		// Obtain the loader
		TeamLoader loader = this.context.getTeamLoader(
				LocationType.OFFICE_FLOOR, this.officeFloorLocation,
				this.teamName);

		// Obtain the team source class
		Class<TeamSource> teamSourceClass = this.context.getTeamSourceClass(
				this.teamSourceClassName, this.officeFloorLocation,
				this.teamName);
		if (teamSourceClass == null) {
			return; // must have source class
		}

		// Load the team type
		this.teamType = loader.loadTeamType(teamSourceClass, this.propertyList);
	}

	@Override
	public TeamType getTeamType() {

		// Ensure the team type is loaded
		if (!this.isTeamTypeLoaded) {
			throw new IllegalStateException("Team type must be loaded");
		}

		// Return the loaded team type (if loaded, otherwise null)
		return this.teamType;
	}

	@Override
	public void loadOfficeFloorTeamSourceType() {

		// Only load the team source type once (whether successful or not)
		if (this.isOfficeFloorTeamSourceTypeLoaded) {
			return;
		}
		this.isOfficeFloorTeamSourceTypeLoaded = true;

		// Obtain the loader
		TeamLoader loader = this.context.getTeamLoader(
				LocationType.OFFICE_FLOOR, this.officeFloorLocation,
				this.teamName);

		// Obtain the team source class
		Class<TeamSource> teamSourceClass = this.context.getTeamSourceClass(
				this.teamSourceClassName, this.officeFloorLocation,
				this.teamName);
		if (teamSourceClass == null) {
			return; // must have source class
		}

		// Load the team source type
		this.teamSourceType = loader.loadOfficeFloorTeamSourceType(
				teamSourceClass, this.propertyList);
	}

	@Override
	public OfficeFloorTeamSourceType getOfficeFloorTeamSourceType() {

		// Ensure the team source type is loaded
		if (!this.isOfficeFloorTeamSourceTypeLoaded) {
			throw new IllegalStateException("Team source type must be loaded");
		}

		// Return the loaded team source type (if loaded, otherwise null)
		return this.teamSourceType;
	}

	@Override
	public void buildTeam(OfficeFloorBuilder builder) {

		// Obtain the team source class
		Class<? extends TeamSource> teamSourceClass = this.context
				.getTeamSourceClass(this.teamSourceClassName,
						this.officeFloorLocation, this.teamName);
		if (teamSourceClass == null) {
			return; // must obtain team source class
		}

		// Build the team
		TeamBuilder<?> teamBuilder = builder.addTeam(this.teamName,
				teamSourceClass);
		for (Property property : this.propertyList) {
			teamBuilder.addProperty(property.getName(), property.getValue());
		}
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