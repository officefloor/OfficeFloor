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
package net.officefloor.compile.integrate.escalation;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * Tests compiling the {@link Office} {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeEscalationTest extends AbstractCompileTestCase {

	/**
	 * Test an {@link OfficeEscalation}.
	 */
	public void testSimpleEscalation() {

		// Record building the OfficeFloor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_registerTeam("OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		this.record_workBuilder_addTask("INPUT", "OFFICE_TEAM").linkParameter(
				0, Throwable.class);
		this.record_officeBuilder_addEscalation(Exception.class,
				"SECTION.WORK", "INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Test an multiple {@link OfficeEscalation} instances.
	 */
	public void testMultipleEscalationOrdering() {

		// Record building the OfficeFloor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_registerTeam("OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		this.record_workBuilder_addTask("INPUT", "OFFICE_TEAM").linkParameter(
				0, Throwable.class);
		this.record_officeBuilder_addEscalation(IOException.class,
				"SECTION.WORK", "INPUT");
		this.record_officeBuilder_addEscalation(SQLException.class,
				"SECTION.WORK", "INPUT");
		this.record_officeBuilder_addEscalation(Exception.class,
				"SECTION.WORK", "INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Class for {@link ClassWorkSource}.
	 */
	public static class EscalationWork {
		public void handle(Throwable parameter) {
		}
	}

}