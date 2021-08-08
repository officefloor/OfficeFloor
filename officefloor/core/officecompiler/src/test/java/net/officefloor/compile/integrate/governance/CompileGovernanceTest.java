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

package net.officefloor.compile.integrate.governance;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.integrate.managedobject.CompileOfficeFloorManagedObjectTest.InputManagedObject;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.ManagedObject;
import net.officefloor.plugin.section.clazz.PropertyValue;

/**
 * Tests compiling a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileGovernanceTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling {@link Governance} for an {@link OfficeFloorManagedObject}.
	 */
	public void testGovernOfficeFloorManagedObject() {
		this.doGovernOfficeFloorManagedObjectTest(false);
	}

	/**
	 * Tests auto-wiring {@link Governance} for an {@link OfficeFloorManagedObject}.
	 */
	public void testGovernOfficeFloorManagedObjectAutowire() {
		this.doGovernOfficeFloorManagedObjectTest(false);
	}

	/**
	 * Tests compiling {@link Governance} for an {@link OfficeFloorSupplier}
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testGovernOfficeFloorSuppliedManagedObject() {
		this.doGovernOfficeFloorManagedObjectTest(true);
	}

	/**
	 * Tests auto-wiring {@link Governance} for an {@link OfficeFloorSupplier}
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testGovernOfficeFloorSuppliedManagedObjectAutowire() {

		// Naming is different, so record to supplied

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM", ClassGovernanceSource.class,
				SimpleManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject(Node.escape(SimpleManagedObject.class.getName()),
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource(Node.escape(SimpleManagedObject.class.getName()),
				Node.escape(SimpleManagedObject.class.getName()));
		DependencyMappingBuilder dependencies = this.record_officeBuilder_addThreadManagedObject(
				Node.escape(SimpleManagedObject.class.getName()), Node.escape(SimpleManagedObject.class.getName()));
		dependencies.mapGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Undertakes governing an {@link OfficeFloorManagedObject}.
	 */
	private void doGovernOfficeFloorManagedObjectTest(boolean isSupplied) {

		// Record building the OfficeFloor
		if (isSupplied) {
			this.record_supplierSetup();
		}
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM", ClassGovernanceSource.class,
				SimpleManagedObject.class);
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		DependencyMappingBuilder dependencies = this.record_officeBuilder_addProcessManagedObject("MANAGED_OBJECT",
				"MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		dependencies.mapGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to provide {@link Governance} to a
	 * {@link OfficeFloorInputManagedObjectModel} that has multiple
	 * {@link OfficeFloorManagedObjectSourceModel} instances (along with
	 * {@link BoundManagedObject}).
	 */
	public void testGovernInputManagedObject() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addGovernance("GOVERNANCE", ClassGovernanceSource.class, SimpleManagedObject.class);
		office.setBoundInputManagedObject("INPUT_MO", "MANAGED_OBJECT_SOURCE_A");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE_A", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mosA = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder mapperA = this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		mapperA.mapGovernance("GOVERNANCE");
		mosA.linkFlow(0, "SECTION.INPUT");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE_B", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mosB = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder mapperB = this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		mapperB.mapGovernance("GOVERNANCE");
		mosB.linkFlow(0, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} for an {@link OfficeManagedObject}.
	 */
	public void testGovernOfficeManagedObject() {
		this.doGovernOfficeManagedObjectTest(false);
	}

	/**
	 * Tests auto-wiring {@link Governance} for an {@link OfficeManagedObject}.
	 */
	public void testGovernOfficeManagedObjectAutowire() {
		this.doGovernOfficeManagedObjectTest(false);
	}

	/**
	 * Tests compiling {@link Governance} for an {@link OfficeSupplier}
	 * {@link OfficeManagedObject}.
	 */
	public void testGovernOfficeSuppliedManagedObject() {
		this.doGovernOfficeManagedObjectTest(true);
	}

	/**
	 * Tests auto-wiring {@link Governance} for an {@link OfficeSupplier}
	 * {@link OfficeManagedObject}.
	 */
	public void testGovernOfficeSuppliedManagedObjectAutowire() {

		// Naming is different, so record to supplied
		this.record_supplierSetup();
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM", ClassGovernanceSource.class,
				SimpleManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE." + Node.escape(SimpleManagedObject.class.getName()),
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE." + Node.escape(SimpleManagedObject.class.getName()),
				"OFFICE." + Node.escape(SimpleManagedObject.class.getName()));
		DependencyMappingBuilder dependencies = this.record_officeBuilder_addThreadManagedObject(
				"OFFICE." + Node.escape(SimpleManagedObject.class.getName()),
				"OFFICE." + Node.escape(SimpleManagedObject.class.getName()));
		dependencies.mapGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Undertakes test to compile {@link Governance} for an
	 * {@link OfficeManagedObject}.
	 */
	private void doGovernOfficeManagedObjectTest(boolean isSupplied) {

		// Record building the OfficeFloor
		if (isSupplied) {
			this.record_supplierSetup();
		}
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM", ClassGovernanceSource.class,
				SimpleManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		DependencyMappingBuilder dependencies = this
				.record_officeBuilder_addProcessManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		dependencies.mapGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} for a {@link OfficeSectionManagedObject}.
	 */
	public void testGovernSectionManagedObject() {
		this.doGovernSectionManagedObjectTest();
	}

	/**
	 * Tests auto-wiring {@link Governance} for a
	 * {@link OfficeSectionManagedObject}.
	 */
	public void testGovernSectionManagedObjectAutowire() {
		this.doGovernSectionManagedObjectTest();
	}

	/**
	 * Undertakes test to compile {@link Governance} for a
	 * {@link OfficeSectionManagedObject}.
	 */
	private void doGovernSectionManagedObjectTest() {

		// Record obtaining the section, managed object and namespace types
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		this.record_officeBuilder_addGovernance("GOVERNANCE", ClassGovernanceSource.class, SimpleManagedObject.class);

		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "doSomething");
		function.linkManagedObject(0, "OFFICE.SECTION.OBJECT", SectionWithManagedObject.class);

		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION." + Node.escape(SimpleManagedObject.class.getName()), ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION." + Node.escape(SimpleManagedObject.class.getName()),
				"OFFICE.SECTION." + Node.escape(SimpleManagedObject.class.getName()));
		DependencyMappingBuilder managedObjectDependencies = this.record_officeBuilder_addThreadManagedObject(
				"OFFICE.SECTION." + Node.escape(SimpleManagedObject.class.getName()),
				"OFFICE.SECTION." + Node.escape(SimpleManagedObject.class.getName()));
		managedObjectDependencies.mapGovernance("GOVERNANCE");

		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.OBJECT", ClassManagedObjectSource.class, 0,
				"class.name", SectionWithManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.OBJECT", "OFFICE.SECTION.OBJECT");
		DependencyMappingBuilder sectionDependencies = this
				.record_officeBuilder_addThreadManagedObject("OFFICE.SECTION.OBJECT", "OFFICE.SECTION.OBJECT");
		sectionDependencies.mapDependency(0, "OFFICE.SECTION." + Node.escape(SimpleManagedObject.class.getName()));

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} over a {@link ManagedFunction} within a
	 * {@link OfficeSection}.
	 */
	public void testGovernSection() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addGovernance("GOVERNANCE", ClassGovernanceSource.class, SimpleManagedObject.class);
		ManagedFunctionBuilder<?, ?> governedFunction = this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		governedFunction.addGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} for a specific {@link OfficeSubSection}.
	 */
	public void testGovernSubSection() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addGovernance("GOVERNANCE", ClassGovernanceSource.class, SimpleManagedObject.class);
		ManagedFunctionBuilder<?, ?> governedFunction = this.record_officeBuilder_addFunction("SECTION.SUB_SECTION",
				"FUNCTION");
		governedFunction.addGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests assigning the {@link Team} for the {@link Governance}.
	 */
	public void testAssignGovernanceTeam() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM", ClassGovernanceSource.class,
				SimpleManagedObject.class);
		ManagedFunctionBuilder<?, ?> governedFunction = this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		governedFunction.addGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} for a specific
	 * {@link OfficeSectionFunction}.
	 */
	public void testGovernOfficeFunction() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addGovernance("GOVERNANCE", ClassGovernanceSource.class, SimpleManagedObject.class);
		ManagedFunctionBuilder<?, ?> governedFunction = this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		governedFunction.addGovernance("GOVERNANCE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} over a {@link ManagedFunction} within a
	 * {@link OfficeSubSection}.
	 */
	public void testSubSectionFunctionInheritGovernance() {

		// Record obtaining the section types
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addGovernance("GOVERNANCE", ClassGovernanceSource.class, SimpleManagedObject.class);
		ManagedFunctionBuilder<?, ?> governedFunction = this
				.record_officeBuilder_addFunction("GOVERNED_SECTION.GOVERNED_SECTION", "FUNCTION");
		governedFunction.addGovernance("GOVERNANCE");
		this.record_officeBuilder_addFunction("NON_GOVERNED_SECTION", "FUNCTION");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Simple class for {@link ClassManagedObjectSource}.
	 */
	public static class SimpleManagedObject {
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing a
	 * {@link FlowInterface}.
	 */
	public static class ProcessManagedObject {

		@FlowInterface
		public static interface Processes {
			void doProcess(Integer parameter);
		}

		Processes processes;
	}

	/**
	 * Simple class for {@link ClassManagedFunctionSource}.
	 */
	public static class SimpleClass {

		public void simpleFunction() {
			fail("Should not be invoked in compiling");
		}
	}

	/**
	 * {@link InputManagedObject} for the {@link ClassManagedFunctionSource}.
	 */
	public static class InputManagedObjectClass {

		public void handleInputManagedObject(Integer parameter) {
		}
	}

	/**
	 * Simple class for {@link ClassGovernanceSource}.
	 */
	public static class SimpleGovernance {

		@Govern
		public void govern(SimpleManagedObject managedObject) {
		}

		@Enforce
		public void enforce() {
		}
	}

	/**
	 * Class for {@link ClassSectionSource} with a {@link ManagedObject}.
	 */
	public static class SectionWithManagedObject {

		@ManagedObject(source = ClassManagedObjectSource.class, properties = @PropertyValue(name = "class.name", valueClass = SimpleManagedObject.class))
		SimpleManagedObject managedObject;

		public void doSomething() {
		}
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
