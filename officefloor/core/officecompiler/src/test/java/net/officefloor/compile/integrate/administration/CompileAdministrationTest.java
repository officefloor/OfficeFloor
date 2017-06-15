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
package net.officefloor.compile.integrate.administration;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests compiling the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileAdministrationTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link Administration}.
	 */
	public void testSimpleAdministration() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		// Only compiled in if attached to function

		// Compile
		this.compile(true);
	}

	/**
	 * Tests {@link Administration} pre-administering a {@link ManagedFunction}.
	 */
	public void testPreAdministerFunction() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		this.record_functionBuilder_preAdministration("ADMIN", SimpleManagedObject.class);

		// Compile
		this.compile(true);
	}

	/**
	 * Tests {@link Administration} post-administering a
	 * {@link ManagedFunction}.
	 */
	public void testPostAdministerFunction() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		this.record_functionBuilder_postAdministration("ADMIN", SimpleManagedObject.class);

		// Compile
		this.compile(true);
	}

	/**
	 * Tests {@link Administration} assigned responsible {@link Team}.
	 */
	public void testAssignAdministrationTeam() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		this.record_functionBuilder_postAdministration("ADMIN", SimpleManagedObject.class)
				.setResponsibleTeam("OFFICE_TEAM");

		// Compile
		this.compile(true);
	}

	/**
	 * Tests administering an {@link OfficeFloorManagedObject}.
	 */
	public void testAdministerOfficeFloorManagedObject() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		AdministrationBuilder<?, ?> admin = this.record_functionBuilder_preAdministration("ADMIN",
				SimpleManagedObject.class);
		admin.administerManagedObject("MANAGED_OBJECT");

		// Compile
		this.compile(true);
	}

	/**
	 * Tests administering an {@link OfficeManagedObject}.
	 */
	public void testAdministerOfficeManagedObject() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		AdministrationBuilder<?, ?> admin = this.record_functionBuilder_preAdministration("ADMIN",
				SimpleManagedObject.class);
		admin.administerManagedObject("OFFICE.MANAGED_OBJECT");

		// Compile
		this.compile(true);
	}

	/**
	 * Simple {@link Administration}.
	 */
	public static class SimpleAdmin {
		public void administer(SimpleManagedObject[] extensions) {
		}
	}

	/**
	 * Simple {@link Class}.
	 */
	public static class SimpleClass {
		public void function() {
		}
	}

	/**
	 * Simple {@link ManagedObject}.
	 */
	public static class SimpleManagedObject {
	}

}