/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import org.junit.Ignore;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.spi.team.Team;

/**
 * Tests the {@link OfficeFloorTeam} configuration of the
 * {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO create tests to build the Team only if used")
public class AutoWireOfficeFloorSource_Team_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	// TODO change team testing to only load the Team if required
	public void test_TODO_buildTeamOnlyAsRequired() {
		fail("TODO change team testing to only load the Team if required");
	}

	/**
	 * Ensure able to assign a {@link Team}.
	 */
	public void testAssignTeam() throws Exception {

		final AutoWire autoWire = new AutoWire(Connection.class);

		// Assign the team
		AutoWireTeam team = this.source.assignTeam(
				OnePersonTeamSource.class.getName(), autoWire);
		team.addProperty("name", "value");

		// Record
		this.recordTeam();
		this.recordOffice();
		this.recordTeam(new String[] { "name", "value" }, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure able to assign a {@link Team} with qualified type.
	 */
	public void testAssignTeamWithQualifiedType() throws Exception {

		final AutoWire autoWire = new AutoWire("QUALIFIED",
				Connection.class.getName());

		// Assign the team
		AutoWireTeam team = this.source.assignTeam(
				OnePersonTeamSource.class.getName(), autoWire);
		team.addProperty("name", "value");

		// Record
		this.recordTeam();
		this.recordOffice();
		this.recordTeam(new String[] { "name", "value" }, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure able to assign a {@link Team} multiple responsibilities.
	 */
	public void testAssignTeamMultipleResponsibilities() throws Exception {

		final AutoWire connectionAutoWire = new AutoWire(Connection.class);
		final AutoWire dataSourceAutoWire = new AutoWire(DataSource.class);

		// Assign the team with multiple responsibilities
		this.source.assignTeam(OnePersonTeamSource.class.getName(),
				connectionAutoWire, dataSourceAutoWire);

		// Record
		this.recordTeam();
		this.recordOffice();
		this.recordTeam(null, connectionAutoWire, dataSourceAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure able to override the default {@link Team}.
	 */
	public void testOverrideDefaultTeam() throws Exception {

		// Assign the default team
		AutoWireTeam defaultTeam = this.source
				.assignDefaultTeam(PassiveTeamSource.class.getName());
		defaultTeam.addProperty("name", "value");

		// Record
		OfficeFloorTeam officeFloorTeam = this
				.createMock(OfficeFloorTeam.class);
		this.recordReturn(this.deployer, this.deployer.addTeam("team",
				PassiveTeamSource.class.getName()), officeFloorTeam);
		officeFloorTeam.addProperty("name", "value");
		this.recordOffice(officeFloorTeam);

		// Test
		this.doSourceOfficeFloorTest();
	}

}