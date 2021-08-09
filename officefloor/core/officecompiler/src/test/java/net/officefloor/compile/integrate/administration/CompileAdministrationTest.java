/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.integrate.administration;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
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
	 * Tests {@link Administration} post-administering a {@link ManagedFunction}.
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
		this.doAdministerOfficeFloorManagedObjectTest(false);
	}

	/**
	 * Tests auto-wiring an {@link OfficeFloorManagedObject}.
	 */
	public void testAdministerOfficeFloorManagedObjectAutowire() {
		this.doAdministerOfficeFloorManagedObjectTest(false);
	}

	/**
	 * Tests administering an {@link OfficeFloorSupplier}
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testAdministerOfficeFloorSuppliedManagedObject() {
		this.doAdministerOfficeFloorManagedObjectTest(true);
	}

	/**
	 * Tests auto-wiring an {@link OfficeFloorSupplier}
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testAdministerOfficeFloorSuppliedManagedObjectAutowire() {

		// Naming is different, so record to supplied
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		AdministrationBuilder<?, ?> admin = this.record_functionBuilder_preAdministration("ADMIN",
				SimpleManagedObject.class);
		admin.administerManagedObject(Node.escape(SimpleManagedObject.class.getName()));
		this.record_officeFloorBuilder_addManagedObject(Node.escape(SimpleManagedObject.class.getName()),
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource(Node.escape(SimpleManagedObject.class.getName()),
				Node.escape(SimpleManagedObject.class.getName()));
		this.record_officeBuilder_addThreadManagedObject(Node.escape(SimpleManagedObject.class.getName()),
				Node.escape(SimpleManagedObject.class.getName()));

		// Compile
		this.compile(true);
	}

	/**
	 * Undertakes administering an {@link OfficeFloorManagedObject}.
	 */
	private void doAdministerOfficeFloorManagedObjectTest(boolean isSupplied) {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		if (isSupplied) {
			this.record_supplierSetup();
		}
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		AdministrationBuilder<?, ?> admin = this.record_functionBuilder_preAdministration("ADMIN",
				SimpleManagedObject.class);
		admin.administerManagedObject("MANAGED_OBJECT");

		// Compile
		this.compile(true);
	}

	/**
	 * Ensure can pre-load administer {@link OfficeObject}.
	 */
	public void testPreLoadAdministerOfficeObject() {

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");
		this.record_dependencyMappingBuilder_preLoadAdminister("ADMIN", SimpleManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile
		this.compile(true);
	}

	/**
	 * Tests administering an {@link OfficeManagedObject}.
	 */
	public void testAdministerOfficeManagedObject() {
		this.doAdministerOfficeManagedObjectTest(false);
	}

	/**
	 * Tests auto-wiring an {@link OfficeManagedObject}.
	 */
	public void testAdministerOfficeManagedObjectAutowire() {
		this.doAdministerOfficeManagedObjectTest(false);
	}

	/**
	 * Tests administering an {@link OfficeSupplier} {@link OfficeManagedObject}.
	 */
	public void testAdministerOfficeSuppliedManagedObject() {
		this.doAdministerOfficeManagedObjectTest(true);
	}

	/**
	 * Tests auto-wiring an {@link OfficeSupplier} {@link OfficeManagedObject}.
	 */
	public void testAdministerOfficeSuppliedManagedObjectAutowire() {

		// Naming is different, so record to supplied
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE." + Node.escape(SimpleManagedObject.class.getName()),
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE." + Node.escape(SimpleManagedObject.class.getName()),
				"OFFICE." + Node.escape(SimpleManagedObject.class.getName()));
		this.record_officeBuilder_addThreadManagedObject("OFFICE." + Node.escape(SimpleManagedObject.class.getName()),
				"OFFICE." + Node.escape(SimpleManagedObject.class.getName()));
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		AdministrationBuilder<?, ?> admin = this.record_functionBuilder_preAdministration("ADMIN",
				SimpleManagedObject.class);
		admin.administerManagedObject("OFFICE." + Node.escape(SimpleManagedObject.class.getName()));

		// Compile
		this.compile(true);
	}

	/**
	 * Undertakes testing administering an {@link OfficeManagedObject}.
	 */
	private void doAdministerOfficeManagedObjectTest(boolean isSupplied) {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		if (isSupplied) {
			this.record_supplierSetup();
		}
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
	 * Ensure can pre-load administer {@link OfficeManagedObject}.
	 */
	public void testPreLoadAdministerOfficeManagedObject() {

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		this.record_dependencyMappingBuilder_preLoadAdminister("ADMIN", SimpleManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile
		this.compile(true);
	}

	/**
	 * Tests administering an {@link OfficeSectionManagedObject}.
	 */
	public void testAdministerOfficeSectionManagedObject() {
		this.doAdministerOfficeSectionManagedObjectTest();
	}

	/**
	 * Tests auto-wiring an {@link OfficeSectionManagedObject}.
	 */
	public void testAdministerOfficeSectionManagedObjectAutowire() {
		this.doAdministerOfficeSectionManagedObjectTest();
	}

	/**
	 * Undertakes testing administering an {@link OfficeSectionManagedObject}.
	 */
	private void doAdministerOfficeSectionManagedObjectTest() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		AdministrationBuilder<?, ?> admin = this.record_functionBuilder_preAdministration("ADMIN",
				SimpleManagedObject.class);
		office.registerManagedObjectSource("OFFICE.SECTION.MANAGED_OBJECT", "OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.MANAGED_OBJECT");
		admin.administerManagedObject("OFFICE.SECTION.MANAGED_OBJECT");

		// Compile
		this.compile(true);
	}

	/**
	 * Ensure can pre-load administer {@link OfficeSectionManagedObject}.
	 */
	public void testPreLoadAdministerOfficeSectionManagedObject() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.MANAGED_OBJECT", "OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.MANAGED_OBJECT");
		this.record_dependencyMappingBuilder_preLoadAdminister("ADMIN", SimpleManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

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

	/**
	 * Mock {@link SupplierSource}.
	 */
	public static class MockSupplierSource extends AbstractSupplierSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			context.addManagedObjectSource(null, SimpleManagedObject.class, new ClassManagedObjectSource()).addProperty(
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, SimpleManagedObject.class.getName());
		}

		@Override
		public void terminate() {
			// nothing to clean up
		}
	}

}
