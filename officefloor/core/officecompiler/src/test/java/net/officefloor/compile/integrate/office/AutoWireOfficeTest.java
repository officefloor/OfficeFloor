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
package net.officefloor.compile.integrate.office;

import net.officefloor.autowire.AutoWire;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.extension.AutoWireOfficeExtensionService;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link AutoWire} of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeTest extends AbstractCompileTestCase {

	/**
	 * Ensure can auto-wire an {@link OfficeManagedObject}.
	 */
	public void testAutoWireOfficeManagedObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");

		// Build the section
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileSectionClass.class, "function");

		// Auto-wire
		function.linkManagedObject(1, "OFFICE.MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire an {@link OfficeObject}.
	 */
	public void testAutoWireOfficeObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Build the section
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileSectionClass.class, "function");

		// Auto-wire
		function.linkManagedObject(1, "MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire an {@link OfficeFloorManagedObject}.
	 */
	public void testAutoWireOfficeFloorManagedObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the section
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileSectionClass.class, "function");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Auto-wire
		function.linkManagedObject(1, "MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to auto-wire the {@link OfficeTeam}.
	 */
	public void testAutoWireOfficeTeam() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Build the section (with auto-wire of team)
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileSectionClass.class, "function", "OFFICE_TEAM");
		function.linkManagedObject(1, "MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure auto-wire of {@link OfficeTeam} is optional.
	 */
	public void testNotAutoWireOfficeTeam() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");

		// Build the section (with auto-wire of team)
		this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION", NoDependencySectionClass.class,
				"function");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to auto-wire the {@link OfficeTeam}.
	 */
	public void testAutoWireOfficeFloorTeam() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICEFLOOR_TEAM",
				"OFFICEFLOOR_TEAM");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Build the section
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileSectionClass.class, "function", "OFFICEFLOOR_TEAM");
		function.linkManagedObject(1, "MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to auto-wire the {@link OfficeFloorTeam} via existing
	 * {@link OfficeTeam}.
	 */
	public void testAutoWireOfficeFloorTeamThroughExistingOfficeTeam() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Build the section
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileSectionClass.class, "function", "OFFICE_TEAM");
		function.linkManagedObject(1, "MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to auto-wire the {@link OfficeFloorTeam} even if
	 * {@link OfficeTeam} by same name exists.
	 */
	public void testAutoWireOfficeFloorTeamWithSameOfficeTeamName() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("EXISTING_TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerTeam("OFFICEFLOOR_TEAM", "EXISTING_TEAM");
		office.registerTeam("OFFICEFLOOR_TEAM_2", "OFFICEFLOOR_TEAM");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Build the section
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileSectionClass.class, "function", "OFFICEFLOOR_TEAM_2");
		function.linkManagedObject(1, "MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto wire {@link Governance} {@link Team} from
	 * {@link OfficeObject}.
	 */
	public void testAutoWireGovernanceTeamFromOfficeObject() {

		// Record building the OfficeFloor (auto wiring governance team)
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM", ClassGovernanceSource.class,
				CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		DependencyMappingBuilder managedObject = this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT",
				"MANAGED_OBJECT");
		managedObject.mapGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto wire {@link Governance} {@link Team} from
	 * {@link ManagedObject}.
	 */
	public void testAutoWireGovernanceTeamFromManagedObject() {

		// Record building the OfficeFloor (auto wiring governance team)
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM", ClassGovernanceSource.class,
				CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		DependencyMappingBuilder managedObject = this
				.record_officeBuilder_addThreadManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		managedObject.mapGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto wire {@link Administration} {@link Team} from
	 * {@link OfficeObject}.
	 */
	public void testAutoWireAdministrationTeamFromOfficeObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Build the section with function pre-administration
		this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION", NoDependencySectionClass.class,
				"function");
		AdministrationBuilder<?, ?> administration = this.record_functionBuilder_preAdministration("ADMINISTRATION",
				CompileManagedObject.class);
		administration.administerManagedObject("MANAGED_OBJECT");

		// Auto wire team
		administration.setResponsibleTeam("OFFICE_TEAM");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto wire {@link Administration} {@link Team} from
	 * {@link ManagedObject}.
	 */
	public void testAutoWireAdministrationTeamFromManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor (auto wiring administration team)
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");

		// Build the section with function post-administration
		this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION", NoDependencySectionClass.class,
				"function");
		AdministrationBuilder<?, ?> administration = this.record_functionBuilder_postAdministration("ADMINISTRATION",
				CompileManagedObject.class);
		administration.administerManagedObject("OFFICE.MANAGED_OBJECT");

		// Auto wire team
		administration.setResponsibleTeam("OFFICE_TEAM");

		// Compile the OfficeFloor
		this.compile(true);
	}

	public static class CompileSectionClass {
		public void function(CompileManagedObject object) {
		}
	}

	public static class NoDependencySectionClass {
		public void function() {
		}
	}

	public static class CompileManagedObject {
	}

	public static class CompileGovernance {
		@Govern
		public void govern(CompileManagedObject extension) {
		}

		@Enforce
		public void enforce() {
		}
	}

	public static class CompileAdministration {
		public void administer(CompileManagedObject[] extensions) {
		}
	}

}