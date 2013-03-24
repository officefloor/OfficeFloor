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
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.administrator.clazz.ClassAdministratorSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests compiling the {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileAdministratorTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link Administrator}.
	 */
	public void testSimpleAdministrator() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addThreadAdministrator("ADMIN",
				"OFFICE_TEAM", ClassAdministratorSource.class,
				ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
				SimpleAdmin.class.getName());

		// Compile
		this.compile(true);
	}

	/**
	 * Tests {@link Administrator} pre-administering a {@link Task}.
	 */
	public void testPreAdministerTask() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkPreTaskAdministration("ADMIN", "duty");
		AdministratorBuilder<?> admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministratorSource.class,
						ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		this.recordReturn(admin, admin.addDuty("duty"), null);

		// Compile
		this.compile(true);
	}

	/**
	 * Tests {@link Administrator} post-administering a {@link Task}.
	 */
	public void testPostAdministerTask() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkPostTaskAdministration("ADMIN", "duty");
		AdministratorBuilder<?> admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministratorSource.class,
						ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		this.recordReturn(admin, admin.addDuty("duty"), null);

		// Compile
		this.compile(true);
	}

	/**
	 * Tests administering an {@link OfficeFloorManagedObject}.
	 */
	public void testAdministerOfficeFloorManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		AdministratorBuilder<?> admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministratorSource.class,
						ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		admin.administerManagedObject("MANAGED_OBJECT");
		office.registerManagedObjectSource("MANAGED_OBJECT",
				"MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT",
				"MANAGED_OBJECT");

		// Compile
		this.compile(true);
	}

	/**
	 * Tests administering an {@link OfficeManagedObject}.
	 */
	public void testAdministerOfficeManagedObject() {

		// Record building the office floor
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
		AdministratorBuilder<?> admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministratorSource.class,
						ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		admin.administerManagedObject("OFFICE.MANAGED_OBJECT");

		// Compile
		this.compile(true);
	}

	/**
	 * Simple {@link Administrator}.
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