/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.integrate.office;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.SectionObjectNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.integrate.officefloor.AugmentManagedObjectSourceFlowTest.Section;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplierThreadLocal;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.extension.AutoWireOfficeExtensionService;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Tests the {@link AutoWire} of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeTest extends AbstractCompileTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Reset the supplier
		CompileSupplierSource.reset();
	}

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
	 * Ensure can auto-wire {@link ManagedObjectDependency} for the
	 * {@link OfficeManagedObject}.
	 */
	public void testAutoWireOfficeManagedObjectDependency() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object with dependency
		office.registerManagedObjectSource("OFFICE.DEPENDENCY", "OFFICE.DEPENDENCY_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENCY_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder dependency = this.record_officeBuilder_addThreadManagedObject("OFFICE.DEPENDENCY",
				"OFFICE.DEPENDENCY");

		// Build the dependency
		office.registerManagedObjectSource("OFFICE.SIMPLE_OBJECT", "OFFICE.SIMPLE_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.SIMPLE_OBJECT", "OFFICE.SIMPLE_OBJECT");

		// Should auto-wire the dependency
		dependency.mapDependency(0, "OFFICE.SIMPLE_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire {@link ManagedObjectDependency} for an input
	 * {@link OfficeManagedObject}.
	 */
	public void testAutoWireOfficeInputManagedObjectDependency() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record obtaining the Section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENCY_OBJECT", ClassManagedObjectSource.class, 0,
				"class.name", DependencyProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder inputMo = this
				.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.DEPENDENCY_OBJECT");

		// Build the Managed Object
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SIMPLE_OBJECT", "OFFICE.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE_OBJECT", "OFFICE.SIMPLE_OBJECT");

		// Map the auto-wired dependency
		inputMo.mapDependency(0, "OFFICE.SIMPLE_OBJECT");

		// Link the input
		managingOffice.linkFlow(0, "SECTION.INPUT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire {@link Office} {@link SuppliedManagedObjectSource}.
	 */
	public void testAutoWireOfficeSuppliedManagedObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Build the Managed Object with dependency
		office.registerManagedObjectSource("OFFICE.DEPENDENCY", "OFFICE.DEPENDENCY_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENCY_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder dependency = this.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENCY",
				"OFFICE.DEPENDENCY");

		// Register the supplied managed object source
		Singleton mos = new Singleton(new CompileManagedObject());
		CompileSupplierSource.addSuppliedManagedObjectSource(CompileManagedObject.class, mos);

		// Should supply and auto-wire the dependency
		final String mosName = "OFFICE." + CompileManagedObject.class.getName();
		final String moName = "OFFICE." + CompileManagedObject.class.getName();
		office.registerManagedObjectSource(mosName, mosName);
		this.record_officeFloorBuilder_addManagedObject(mosName, mos, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject(moName, mosName);
		dependency.mapDependency(0, moName);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire {@link Office} {@link ManagedObjectDependency} for a
	 * {@link SuppliedManagedObjectSource}.
	 */
	public void testAutoWireOfficeSuppliedManagedObjectDependency() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Register the supplied managed object source
		CompileSupplierSource.addSuppliedManagedObjectSource(DependencyManagedObject.class,
				new ClassManagedObjectSource(), ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				DependencyManagedObject.class.getName());

		// Should supply the dependency for auto-wiring
		final String mosName = "OFFICE." + DependencyManagedObject.class.getName();
		final String moName = "OFFICE." + DependencyManagedObject.class.getName();
		office.registerManagedObjectSource(mosName, mosName);
		this.record_officeFloorBuilder_addManagedObject(mosName, ClassManagedObjectSource.class, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder mo = this.record_officeBuilder_addThreadManagedObject(moName, mosName);

		// Build the Managed Object
		office.registerManagedObjectSource("OFFICE.SIMPLE_OBJECT", "OFFICE.SIMPLE_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.SIMPLE_OBJECT", "OFFICE.SIMPLE_OBJECT");

		// Build the section
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				DependencySectionClass.class, "function");

		// Auto-wire the function dependency
		function.linkManagedObject(1, moName, DependencyManagedObject.class);

		// Auto-wire the supplied dependency
		mo.mapDependency(0, "OFFICE.SIMPLE_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire {@link OfficeFloor} {@link SuppliedManagedObjectSource}.
	 */
	public void testAutoWireOfficeFloorSuppliedManagedObject() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Register the supplied managed object source
		Singleton mos = new Singleton(new CompileManagedObject());
		CompileSupplierSource.addSuppliedManagedObjectSource(CompileManagedObject.class, mos);

		// Build the Managed Object with dependency
		office.registerManagedObjectSource("OFFICE.DEPENDENCY", "OFFICE.DEPENDENCY_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENCY_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder dependency = this.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENCY",
				"OFFICE.DEPENDENCY");

		// Should supply the dependency for auto-wiring
		final String mosName = CompileManagedObject.class.getName();
		final String moName = CompileManagedObject.class.getName();
		office.registerManagedObjectSource(mosName, mosName);
		this.record_officeFloorBuilder_addManagedObject(mosName, mos, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject(moName, mosName);

		// Auto-wire the dependency
		dependency.mapDependency(0, moName);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire {@link OfficeFloor} {@link ManagedObjectDependency} for
	 * a {@link SuppliedManagedObjectSource}.
	 */
	public void testAutoWireOfficeFloorSuppliedManagedObjectDependency() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Register the supplied managed object source
		CompileSupplierSource.addSuppliedManagedObjectSource(DependencyManagedObject.class,
				new ClassManagedObjectSource(), ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				DependencyManagedObject.class.getName());
		final String mosName = DependencyManagedObject.class.getName();
		final String moName = DependencyManagedObject.class.getName();

		// Build the section
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				DependencySectionClass.class, "function");

		// Auto-wire the function dependency
		function.linkManagedObject(1, moName, DependencyManagedObject.class);

		// Should supply the dependency for auto-wiring
		office.registerManagedObjectSource(mosName, mosName);
		this.record_officeFloorBuilder_addManagedObject(mosName, ClassManagedObjectSource.class, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder mo = this.record_officeBuilder_addThreadManagedObject(moName, mosName);

		// Build the Managed Object
		office.registerManagedObjectSource("SIMPLE_OBJECT", "SIMPLE_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("SIMPLE_OBJECT", "SIMPLE_OBJECT");

		// Auto-wire the supplied dependency
		mo.mapDependency(0, "SIMPLE_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * <p>
	 * Ensure {@link SuppliedManagedObjectSource} requiring a {@link Flow} is not
	 * available for auto-wiring. Must be manually added with {@link Flow}
	 * configured.
	 * <p>
	 * Purpose of {@link SupplierSource} is integration with object dependency
	 * injection libraries. These are not expected to support continuation/thread
	 * injection.
	 */
	public void testSuppliedManagedObjectWithFlowNotAvailable() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");

		// Register supplied managed object with flow
		CompileSupplierSource.addSuppliedManagedObjectSource(CompileManagedObject.class, new FlowManagedObjectSource());

		// Build the Managed Object with dependency
		office.registerManagedObjectSource("OFFICE.DEPENDENCY", "OFFICE.DEPENDENCY_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENCY_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENCY", "OFFICE.DEPENDENCY");

		// Should not supply managed object as requires flow configuration
		// (both instance and input require dependency)
		this.issues.recordIssue("dependency", ManagedObjectDependencyNodeImpl.class, "No target found by auto-wiring");
		this.issues.recordIssue("dependency", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency dependency is not linked to a BoundManagedObjectNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * <p>
	 * Ensure {@link SuppliedManagedObjectSource} requiring a {@link Team} is not
	 * available for auto-wiring. Must be manually added with {@link Team}
	 * configured.
	 * <p>
	 * Purpose of {@link SupplierSource} is integration with object dependency
	 * injection libraries. These are not expected to support continuation/thread
	 * injection.
	 */
	public void testSuppliedManagedObjectWithTeamNotAvailable() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Provide the supplied managed object
		TeamManagedObjectSource mos = new TeamManagedObjectSource();
		CompileSupplierSource.addSuppliedManagedObjectSource(TeamManagedObjectSource.class, mos);

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor (auto wiring team of managed object)
		this.record_supplierSetup();
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");
		this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION", TeamSectionClass.class, "function");

		// Should not supply managed object as requires team configuration
		this.issues.recordIssue(TeamManagedObjectSource.class.getName(), SectionObjectNodeImpl.class,
				"No target found by auto-wiring");
		this.issues.recordIssue(TeamManagedObjectSource.class.getName(), SectionObjectNodeImpl.class, "Section Object "
				+ TeamManagedObjectSource.class.getName() + " is not linked to a BoundManagedObjectNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to auto-wire the {@link OfficeFloorSupplierThreadLocal}.
	 */
	public void testAutoWireSupplierThreadLocal() {

		// Flag to enable auto-wiring of the objects
		AutoWireOfficeExtensionService.enableAutoWireObjects();

		// Register supplier thread local
		CompileSupplierSource.SupplierThreadLocalInstance instance = CompileSupplierSource
				.addSupplierThreadLocal(CompileManagedObject.class);

		// Provide supplied managed object for auto-wiring
		CompileSupplierSource.addSuppliedManagedObjectSource(CompileManagedObject.class, new ClassManagedObjectSource(),
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());

		// Managed Object name
		final String MO_NAME = "OFFICE." + CompileManagedObject.class.getName();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(MO_NAME, ClassManagedObjectSource.class, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource(MO_NAME, MO_NAME);
		ThreadDependencyMappingBuilder dependencyMapper = this.record_officeBuilder_addThreadManagedObject(MO_NAME,
				MO_NAME);

		// Should obtain thread local
		OptionalThreadLocal<?> threadLocal = this.createMock(OptionalThreadLocal.class);
		this.recordReturn(dependencyMapper, dependencyMapper.getOptionalThreadLocal(), threadLocal);

		// Complete the office
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				CompileSectionClass.class, "function");
		function.linkManagedObject(1, MO_NAME, CompileManagedObject.class);

		// Record obtain value from thread local
		this.recordReturn(threadLocal, threadLocal.get(), instance);

		// Ensure correct supplier thread local
		this.addValidator((officeFloor) -> {
			assertSame("Invalid thread local", instance, instance.supplierThreadLocal.get());
		});

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
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
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
	 * Ensure can auto-wire the {@link Team} for the {@link SubSection}
	 * {@link ManagedFunction}.
	 */
	public void testAutoWireSubSectionFunctionTeam() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Build the section (with auto-wire of team)
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE",
				"SECTION.SUB_SECTION", CompileSectionClass.class, "function", "OFFICE_TEAM");
		function.linkManagedObject(1, "MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to auto-wire the {@link Team} for a {@link SubSection}
	 * {@link ManagedFunction} based on transitive {@link ManagedObjectDependency}.
	 */
	public void testAutoWireSubSectionFunctionTeamFromTransitiveDependency() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();

		// Register the team
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "OFFICEFLOOR_TEAM");

		// Build the dependency
		this.record_officeFloorBuilder_addManagedObject("DEPENDENCY_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENCY_OBJECT", "DEPENDENCY_SOURCE");
		DependencyMappingBuilder dependency = this.record_officeBuilder_addThreadManagedObject("DEPENDENCY_OBJECT",
				"DEPENDENCY_OBJECT");

		// Register the compile object
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE_OBJECT", "SIMPLE_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("SIMPLE_OBJECT", "SIMPLE_OBJECT");

		// Link the dependency
		dependency.mapDependency(0, "SIMPLE_OBJECT");

		// Build the section (with auto-wire of team)
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION",
				DependencySectionClass.class, "function", "OFFICE_TEAM");
		function.linkManagedObject(1, "DEPENDENCY_OBJECT", DependencyManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire the {@link Team} for a {@link Section}
	 * {@link ManagedFunction} based on {@link OfficeFloorInputManagedObject}.
	 */
	public void testAutoWireOfficeFloorTeamViaInputManagedObject() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();

		// Register the team
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addOffice("OFFICE", "TEAM", "TEAM");

		// Build the input dependency
		this.record_officeFloorBuilder_addManagedObject("INPUT_OBJECT", ClassManagedObjectSource.class, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mo = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MANAGED_OBJECT");
		mo.linkFlow(0, "SECTION.INPUT");

		// Build the function (with auto-wire of team)
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT", "TEAM");
		function.linkManagedObject(0, "INPUT_MANAGED_OBJECT", ProcessManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire the {@link OfficeFloorInputManagedObject} and then based
	 * on that the {@link Team}.
	 */
	public void testAutoWireInputManagedObjectThenAutoWireTeam() {

		// Flag to enable auto-wiring both objects and teams
		AutoWireOfficeExtensionService.enableAutoWireObjects();
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();

		// Register the team
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addOffice("OFFICE", "TEAM", "TEAM");

		// Build the input dependency
		this.record_officeFloorBuilder_addManagedObject("INPUT_OBJECT", ClassManagedObjectSource.class, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mo = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MANAGED_OBJECT");
		mo.linkFlow(0, "SECTION.INPUT");

		// Build the function (with auto-wire of team)
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT", "TEAM");
		function.linkManagedObject(0, "INPUT_MANAGED_OBJECT", ProcessManagedObject.class);

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
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
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
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
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
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
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
		this.record_officeFloorBuilder_addTeam("EXISTING_TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
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

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record building the OfficeFloor (auto wiring governance team)
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
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

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record building the OfficeFloor (auto wiring governance team)
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
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

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
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

		// Auto wire team
		administration.setResponsibleTeam("OFFICE_TEAM");

		// Administer the managed object
		administration.administerManagedObject("MANAGED_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto wire {@link Administration} {@link Team} from
	 * {@link ManagedObject}.
	 */
	public void testAutoWireAdministrationTeamFromManagedObject() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor (auto wiring administration team)
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
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

		// Auto wire team
		administration.setResponsibleTeam("OFFICE_TEAM");

		// Administer the managed object
		administration.administerManagedObject("OFFICE.MANAGED_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can auto-wire the {@link Team} for the {@link ManagedFunctionSource}
	 * {@link ManagedFunction}.
	 */
	public void testAutoWireManagedObjectSourceFunctionTeam() {

		// Flag to enable auto-wiring of the teams
		AutoWireOfficeExtensionService.enableAutoWireTeams();

		// Record building the OfficeFloor (auto wiring managed object function)
		this.record_init();
		this.record_officeFloorBuilder_addTeam("OFFICEFLOOR_TEAM", new OnePersonTeamSource());
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICEFLOOR_TEAM",
				"OFFICEFLOOR_TEAM");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.FUNCTION_MANAGED_OBJECT_SOURCE",
				CompileFunctionManagedObjectSource.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Auto wire team
		office.registerTeam("OFFICE.FUNCTION_MANAGED_OBJECT_SOURCE.MO_TEAM", "OFFICEFLOOR_TEAM");

		// Build remaining of managed object
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.DEPENDENT_MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	public static class CompileSectionClass {
		public void function(CompileManagedObject object) {
		}
	}

	public static class DependencySectionClass {
		public void function(DependencyManagedObject object) {
		}
	}

	public static class ProcessSectionClass {
		public void function(ProcessManagedObject object) {
		}
	}

	public static class TeamSectionClass {
		public void function(TeamManagedObjectSource object) {
		}
	}

	public static class NoDependencySectionClass {
		public void function() {
		}
	}

	public static class CompileManagedObject {
	}

	public static class DependencyManagedObject {
		@Dependency
		private CompileManagedObject dependency;
	}

	public static class ProcessManagedObject extends CompileManagedObject {

		@FlowInterface
		public static interface Flows {
			void doFlow();
		}

		Flows flows;
	}

	public static class DependencyProcessManagedObject {

		@FlowInterface
		public static interface Flows {
			void doFlow();
		}

		Flows flows;

		@Dependency
		private CompileManagedObject dependency;
	}

	public static class ProcessClass {
		public void process(Integer value) {
		}
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

	@TestSource
	public static class CompileSupplierSource extends AbstractSupplierSource {

		private static class SupplierThreadLocalInstance {
			private final String qualifier;
			private final Class<?> type;
			private SupplierThreadLocal<?> supplierThreadLocal = null;

			public SupplierThreadLocalInstance(String qualifier, Class<?> type) {
				this.qualifier = qualifier;
				this.type = type;
			}
		}

		private static class SuppliedManagedObjectSourceInstance {
			private final String qualifier;
			private final Class<?> type;
			private final ManagedObjectSource<?, ?> managedObjectSource;
			private final String[] propertyNameValuePairs;

			public SuppliedManagedObjectSourceInstance(String qualifier, Class<?> type,
					ManagedObjectSource<?, ?> managedObjectSource, String[] propertyNameValuePairs) {
				this.qualifier = qualifier;
				this.type = type;
				this.managedObjectSource = managedObjectSource;
				this.propertyNameValuePairs = propertyNameValuePairs;
			}
		}

		private static final List<SupplierThreadLocalInstance> supplierThreadLocals = new LinkedList<>();

		private static final List<SuppliedManagedObjectSourceInstance> suppliedManagedObjectSources = new LinkedList<>();

		public static void reset() {
			supplierThreadLocals.clear();
			suppliedManagedObjectSources.clear();
		}

		public static SupplierThreadLocalInstance addSupplierThreadLocal(String qualifier, Class<?> type) {
			SupplierThreadLocalInstance instance = new SupplierThreadLocalInstance(qualifier, type);
			supplierThreadLocals.add(instance);
			return instance;
		}

		public static SupplierThreadLocalInstance addSupplierThreadLocal(Class<?> type) {
			return addSupplierThreadLocal(null, type);
		}

		public static void addSuppliedManagedObjectSource(String qualifier, Class<?> type,
				ManagedObjectSource<?, ?> managedObjectSource, String... propertyNameValuePairs) {
			suppliedManagedObjectSources.add(new SuppliedManagedObjectSourceInstance(qualifier, type,
					managedObjectSource, propertyNameValuePairs));
		}

		public static void addSuppliedManagedObjectSource(Class<?> type, ManagedObjectSource<?, ?> managedObjectSource,
				String... propertyNameValuePairs) {
			addSuppliedManagedObjectSource(null, type, managedObjectSource, propertyNameValuePairs);
		}

		/*
		 * ================= SupplierSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Add the supplier thread locals
			for (SupplierThreadLocalInstance instance : supplierThreadLocals) {
				instance.supplierThreadLocal = context.addSupplierThreadLocal(instance.qualifier, instance.type);
			}

			// Add the supplied managed object sources
			for (SuppliedManagedObjectSourceInstance instance : suppliedManagedObjectSources) {
				SuppliedManagedObjectSource mos = context.addManagedObjectSource(instance.qualifier, instance.type,
						instance.managedObjectSource);
				for (int i = 0; i < instance.propertyNameValuePairs.length; i += 2) {
					String name = instance.propertyNameValuePairs[i];
					String value = instance.propertyNameValuePairs[i + 1];
					mos.addProperty(name, value);
				}
			}
		}

		@Override
		public void terminate() {
			// nothing to clean up
		}
	}

	@TestSource
	public static class CompileFunctionManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
			implements ManagedObject, ManagedFunction<Indexed, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No properties required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			context.setObjectClass(this.getClass());

			// Add the dependency
			context.addDependency(CompileManagedObject.class).setTypeQualifier("QUALIFIER");

			// Add the function
			context.getManagedObjectSourceContext().addManagedFunction("FUNCTION", () -> this)
					.setResponsibleTeam("MO_TEAM");

			// Add the recycle function
			context.getManagedObjectSourceContext().getRecycleFunction(() -> this).setResponsibleTeam("MO_TEAM");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		@Override
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {
			return null;
		}
	}

	@TestSource
	public static class FlowManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
			implements ManagedObject {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			context.setObjectClass(this.getClass());

			// Require flow
			context.addFlow(String.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	@TestSource
	public static class TeamManagedObjectSource extends AbstractManagedObjectSource<None, Indexed>
			implements ManagedObject, ManagedFunction<None, None> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, Indexed> context) throws Exception {
			context.setObjectClass(this.getClass());

			// Require team
			context.getManagedObjectSourceContext().getRecycleFunction(() -> this).setResponsibleTeam("MO_TEAM");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		@Override
		public Object execute(ManagedFunctionContext<None, None> context) throws Throwable {
			return null;
		}
	}

}