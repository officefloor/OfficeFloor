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
package net.officefloor.autowire.impl;

import java.sql.Connection;

import javax.sql.DataSource;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeamSource;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Tests the {@link OfficeFloorTeam} configuration of the
 * {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSource_Team_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	/**
	 * Ensure not build {@link Team} if unused.
	 */
	public void testUnusedTeams() throws Exception {

		final AutoWire autoWire = new AutoWire(Connection.class);

		// Assign an unused team (default team also unused)
		this.source.assignTeam(OnePersonTeamSource.class.getName(), autoWire);

		// Record (no used teams)
		this.recordOffice();

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure build {@link Team} as used.
	 */
	public void testDefaultTeam() throws Exception {

		// Record (default team)
		this.registerDefaultOfficeTeam();
		this.recordOffice();
		this.recordDefaultTeamLinkedToOffice();

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure able to override the default {@link Team}.
	 */
	public void testOverrideDefaultTeam() throws Exception {

		// Override the default team
		AutoWireTeam defaultTeam = this.source
				.assignDefaultTeam(OnePersonTeamSource.class.getName());
		defaultTeam.addProperty("name", "value");

		// Record
		this.registerOfficeTeam(DEFAULT_TEAM);
		this.recordOffice();
		this.recordTeam(OnePersonTeamSource.class, DEFAULT_TEAM, "name",
				"value");
		this.recordLinkTeamToOffice(DEFAULT_TEAM, DEFAULT_TEAM);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure able to assign a {@link Team} to non-qualified {@link OfficeTeam}.
	 */
	public void testAssignTeamToOfficeTeam() throws Exception {

		final AutoWire autoWire = new AutoWire(Connection.class);

		// Assign the team
		AutoWireTeam team = this.source.assignTeam(
				OnePersonTeamSource.class.getName(), autoWire);
		team.addProperty("name", "value");

		// Record
		this.registerOfficeTeam(autoWire);
		this.recordOffice();
		this.recordTeam(OnePersonTeamSource.class, autoWire, "name", "value");
		this.recordLinkTeamToOffice(autoWire, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure able to assign a {@link Team} to qualified {@link OfficeTeam}.
	 */
	public void testAssignQualifiedTeamToQualifiedOfficeTeam() throws Exception {

		final AutoWire autoWire = new AutoWire("QUALIFIED",
				Connection.class.getName());

		// Assign the team
		AutoWireTeam team = this.source.assignTeam(
				OnePersonTeamSource.class.getName(), autoWire);
		team.addProperty("name", "value");

		// Record
		this.registerOfficeTeam(autoWire);
		this.recordOffice();
		this.recordTeam(OnePersonTeamSource.class, autoWire, "name", "value");
		this.recordLinkTeamToOffice(autoWire, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure able to assign a non-qualified {@link Team} to qualified
	 * {@link AutoWire}.
	 */
	public void testAssignTeamToQualifiedOfficeTeam() throws Exception {

		final AutoWire unqualified = new AutoWire(Connection.class);
		final AutoWire qualified = new AutoWire("QUALIFIED",
				Connection.class.getName());

		// Assign the team
		AutoWireTeam team = this.source.assignTeam(
				OnePersonTeamSource.class.getName(), unqualified);
		team.addProperty("name", "value");

		// Record
		this.registerOfficeTeam(qualified);
		this.recordOffice();
		this.recordTeam(OnePersonTeamSource.class, unqualified, "name", "value");
		this.recordLinkTeamToOffice(unqualified, qualified);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure able to assign a {@link Team} to multiple {@link OfficeTeam}
	 * instances.
	 */
	public void testAssignTeamToMultipleOfficeTeams() throws Exception {

		final AutoWire connectionAutoWire = new AutoWire(Connection.class);
		final AutoWire qualifiedConnectionAutoWire = new AutoWire("QUALIFIED",
				Connection.class.getName());
		final AutoWire dataSourceAutoWire = new AutoWire(DataSource.class);
		final AutoWire qualifiedDataSourceAutoWire = new AutoWire("QUALIFIED",
				DataSource.class.getName());

		// Assign the team with multiple matching OfficeTeams
		this.source.assignTeam(LeaderFollowerTeamSource.class.getName(),
				connectionAutoWire, dataSourceAutoWire);

		// Record
		this.registerOfficeTeam(connectionAutoWire);
		this.registerOfficeTeam(qualifiedConnectionAutoWire);
		this.registerOfficeTeam(dataSourceAutoWire);
		this.registerOfficeTeam(qualifiedDataSourceAutoWire);
		this.recordOffice();
		this.recordTeam(LeaderFollowerTeamSource.class, connectionAutoWire);
		this.recordLinkTeamToOffice(connectionAutoWire, connectionAutoWire);
		this.recordLinkTeamToOffice(connectionAutoWire,
				qualifiedConnectionAutoWire);
		this.recordLinkTeamToOffice(connectionAutoWire, dataSourceAutoWire);
		this.recordLinkTeamToOffice(connectionAutoWire,
				qualifiedDataSourceAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

}