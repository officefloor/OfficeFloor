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
package net.officefloor.compile.integrate.administrator;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.Work;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.plugin.administrator.clazz.ClassAdministrationSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests compiling the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileAdministratorTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link Administration}.
	 */
	public void testSimpleAdministrator() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addThreadAdministrator("ADMIN",
				"OFFICE_TEAM", ClassAdministrationSource.class,
				ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME,
				SimpleAdmin.class.getName());

		// Compile
		this.compile(true);
	}

	/**
	 * Tests {@link Administration} pre-administering a {@link ManagedFunction}.
	 */
	@SuppressWarnings("rawtypes")
	public void testPreAdministerTask() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("DESK.WORK");
		ManagedFunctionBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkPreTaskAdministration("ADMIN", "duty");
		AdministrationBuilder admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministrationSource.class,
						ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		this.recordReturn(admin, admin.addDuty("duty"), null);

		// Compile
		this.compile(true);
	}

	/**
	 * Tests {@link Administration} post-administering a {@link ManagedFunction}.
	 */
	@SuppressWarnings("rawtypes")
	public void testPostAdministerTask() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("DESK.WORK");
		ManagedFunctionBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkPostTaskAdministration("ADMIN", "duty");
		AdministrationBuilder admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministrationSource.class,
						ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		this.recordReturn(admin, admin.addDuty("duty"), null);

		// Compile
		this.compile(true);
	}

	/**
	 * Tests administering an {@link OfficeFloorManagedObject}.
	 */
	@SuppressWarnings("rawtypes")
	public void testAdministerOfficeFloorManagedObject() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("MANAGED_OBJECT",
				"MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT",
				"MANAGED_OBJECT");
		AdministrationBuilder admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministrationSource.class,
						ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		admin.administerManagedObject("MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile
		this.compile(true);
	}

	/**
	 * Tests administering an {@link OfficeManagedObject}.
	 */
	@SuppressWarnings("rawtypes")
	public void testAdministerOfficeManagedObject() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT",
				"OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject(
				"OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		AdministrationBuilder admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministrationSource.class,
						ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		admin.administerManagedObject("OFFICE.MANAGED_OBJECT");

		// Compile
		this.compile(true);
	}

	/**
	 * Simple {@link Administration}.
	 */
	public static class SimpleAdmin {

		public void duty(Object[] extensions) {
		}
	}

	/**
	 * Simple {@link Work}.
	 */
	public static class SimpleWork {

		public void task() {
		}
	}

	/**
	 * Simple {@link ManagedObject}.
	 */
	public static class SimpleManagedObject {
	}
}