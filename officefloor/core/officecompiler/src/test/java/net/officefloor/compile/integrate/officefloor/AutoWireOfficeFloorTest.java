/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.integrate.officefloor;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.extension.AutoWireOfficeFloorExtensionService;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link AutoWire} of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorTest extends AbstractCompileTestCase {

	/**
	 * Ensure can auto-wire an {@link OfficeFloorManagedObject}.
	 */
	public void testAutoWireOfficeFloorManagedObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeFloorExtensionService.enableAutoWireObjects();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Auto-wire the object
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Build the section
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileSectionClass.class, "function");
		function.linkManagedObject(1, "MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to auto-wire the {@link OfficeFloorTeam}.
	 */
	public void testAutoWireOfficeFloorTeam() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeFloorExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor (with auto-wire of team)
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Auto-wire the team
		office.registerTeam("OFFICE_TEAM", "OFFICEFLOOR_TEAM");

		// Build the section
		this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION", CompileSimpleSectionClass.class,
				"function", "OFFICE_TEAM");

		// Compile the OfficeFloor
		this.compile(true);
	}

	public static class CompileManagedObject {
	}

	public static class CompileSectionClass {
		public void function(CompileManagedObject object) {
		}
	}

	public static class CompileSimpleSectionClass {
		public void function() {
		}
	}

}