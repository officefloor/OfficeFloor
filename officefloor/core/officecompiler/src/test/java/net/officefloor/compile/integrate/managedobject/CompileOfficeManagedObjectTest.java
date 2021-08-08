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

package net.officefloor.compile.integrate.managedobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFlowNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFunctionDependencyNodeImpl;
import net.officefloor.compile.impl.supplier.MockTypeManagedObjectSource;
import net.officefloor.compile.integrate.CompileTestSupport;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests compiling an {@link Office} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class CompileOfficeManagedObjectTest {

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link CompileTestSupport}.
	 */
	private final CompileTestSupport compile = new CompileTestSupport();

	/**
	 * Tests compiling a simple {@link ManagedObjectSource}.
	 */
	@Test
	public void simpleManagedObjectSource() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} bound to {@link ProcessState}.
	 */
	@Test
	public void processBoundManagedObject() {

		// Record building the OfficeFloor
		this.compile.record_init();
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.mocks.recordReturn(office,
				office.addProcessManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT"), null);
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling a supplied {@link ManagedObject} bound to
	 * {@link ProcessState}.
	 */
	@Test
	public void suppliedManagedObjectSource() {

		// Setup to provide managed object source instance
		MockSupplierSource.reset();
		final String managedObjectName = "OFFICE.MANAGED_OBJECT_SOURCE";
		final MockTypeManagedObjectSource mos = new MockTypeManagedObjectSource(Object.class, managedObjectName);
		MockSupplierSource.managedObjectSource = mos;

		// Record building the OfficeFloor
		this.compile.record_supplierSetup();
		this.compile.record_init();
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", managedObjectName);
		this.mocks.recordReturn(office,
				office.addProcessManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT"), null);

		// Record instance (as supplied)
		this.compile.record_officeFloorBuilder_addManagedObject(managedObjectName, mos, 0, "MO_NAME", "MO_VALUE");

		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Mock {@link SupplierSource}.
	 */
	public static class MockSupplierSource extends AbstractSupplierSource {

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			managedObjectSource = null;
		}

		/**
		 * {@link ManagedObjectSource}.
		 */
		public static ManagedObjectSource<?, ?> managedObjectSource = null;

		/*
		 * ===================== SupplierSource ========================+
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Specification should not be required");
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Ensure the property is available
			String value = context.getProperty("SUPPLY_NAME");
			assertEquals("SUPPLY_VALUE", value, "Incorrect property value");

			// Supply the managed object source
			SuppliedManagedObjectSource source = context.addManagedObjectSource("QUALIFIER", Connection.class,
					managedObjectSource);
			source.addProperty("MO_NAME", "MO_VALUE");
		}

		@Override
		public void terminate() {
			// nothing to clean up
		}
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not linked.
	 */
	@Test
	public void managedObjectWithDependencyNotLinked() {

		// Record building the OfficeFloor
		this.compile.record_init();

		// Register the office managed object with dependency not linked
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DEPENDENT", "OFFICE.DEPENDENT_SOURCE");
		this.compile.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENT", "OFFICE.DEPENDENT");
		this.compile.getIssues().recordIssue("OFFICE.DEPENDENT." + Node.escape(SimpleManagedObject.class.getName()),
				ManagedObjectDependencyNodeImpl.class, "Managed Object Dependency "
						+ SimpleManagedObject.class.getName() + " is not linked to a BoundManagedObjectNode");

		// Add managed objects to OfficeFloor
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", DependencyManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency in {@link Office}.
	 */
	@Test
	public void managedObjectWithDependencyInOffice() {

		// Record building the office
		this.compile.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DEPENDENT", "OFFICE.DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this.compile.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENT",
				"OFFICE.DEPENDENT");
		mapper.mapDependency(0, "OFFICE.SIMPLE");
		office.registerManagedObjectSource("OFFICE.SIMPLE", "OFFICE.SIMPLE_SOURCE");
		this.compile.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE", "OFFICE.SIMPLE");

		// Add managed objects to office
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", DependencyManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency outside
	 * {@link Office}.
	 */
	@Test
	public void managedObjectWithDependencyOutsideOffice() {

		// Record building the office
		this.compile.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.compile.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", DependencyManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DEPENDENT", "OFFICE.DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this.compile.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENT",
				"OFFICE.DEPENDENT");
		mapper.mapDependency(0, "SIMPLE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a
	 * {@link ManagedObjectFunctionDependency} not linked.
	 */
	@Test
	public void managedObjectWithFunctionDependencyNotLinked() {

		// Record building the OfficeFloor
		this.compile.record_init();

		// Register the office managed object with dependency not linked
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DEPENDENT", "OFFICE.DEPENDENT_SOURCE");
		this.compile.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENT", "OFFICE.DEPENDENT");
		this.compile.getIssues().recordIssue("OFFICE.DEPENDENT_SOURCE.DEPENDENCY",
				ManagedObjectFunctionDependencyNodeImpl.class,
				"Managed Object Function Dependency DEPENDENCY is not linked to a BoundManagedObjectNode");

		// Add managed objects to OfficeFloor
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE",
				FunctionDependencyManagedObject.class, 0);
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a
	 * {@link ManagedObjectFunctionDependency} linked in {@link Office}.
	 */
	@Test
	public void managedObjectWithFunctionDependencyInOffice() {

		// Record building the office
		this.compile.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE",
				FunctionDependencyManagedObject.class, 0);
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Map in function dependencies
		this.compile.record_managingOfficeBuilder_mapFunctionDependency("DEPENDENCY", "OFFICE.SIMPLE");

		// Register remaining
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SIMPLE", "OFFICE.SIMPLE_SOURCE");
		this.compile.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE", "OFFICE.SIMPLE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a
	 * {@link ManagedObjectFunctionDependency} linked outside {@link Office}.
	 */
	@Test
	public void managedObjectWithFunctionDependencyOutsideOffice() {

		// Record building the office
		this.compile.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.compile.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE",
				FunctionDependencyManagedObject.class, 0);
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Map in function dependency
		this.compile.record_managingOfficeBuilder_mapFunctionDependency("DEPENDENCY", "SIMPLE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling an Input {@link ManagedObject} with dependency linked to a
	 * {@link ManagedObject} in the {@link Office}.
	 */
	@Test
	public void inputManagedObjectWithDependencyInOffice() {

		// Record obtaining the Section type
		this.compile.getIssues().recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.compile.record_init();

		// Register the office with the namespace for the input process flow
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.INPUT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", InputManagedObject.class.getName());
		ManagingOfficeBuilder<?> inputMos = this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder inputDependencies = this.compile
				.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.INPUT_SOURCE");
		office.registerManagedObjectSource("OFFICE.SIMPLE", "OFFICE.SIMPLE_SOURCE");
		this.compile.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE", "OFFICE.SIMPLE");
		inputDependencies.mapDependency(0, "OFFICE.SIMPLE");
		inputMos.linkFlow(0, "SECTION.INPUT");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.compile.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests compiling an Input {@link ManagedObject} with dependency linked to a
	 * {@link ManagedObject} outside the {@link Office} (e.g. {@link OfficeFloor}).
	 */
	@Test
	public void inputManagedObjectWithDependencyOutsideOffice() {

		// Record obtaining the Section type
		this.compile.getIssues().recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.compile.record_init();

		// Register the office with the namespace for the input process flow
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.compile.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.INPUT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", InputManagedObject.class.getName());
		ManagingOfficeBuilder<?> inputMos = this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder inputDependencies = this.compile
				.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.INPUT_SOURCE");
		inputDependencies.mapDependency(0, "SIMPLE");
		inputMos.linkFlow(0, "SECTION.INPUT");
		ManagedFunctionBuilder<?, ?> function = this.compile.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Ensure issue if {@link ManagedObjectFlow} of
	 * {@link OfficeManagedObjectSource} is not linked.
	 */
	@Test
	public void managedObjectSourceFlowNotLinked() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", ProcessManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.MANAGED_OBJECT_SOURCE");
		this.compile.getIssues().recordIssue("OFFICE.MANAGED_OBJECT_SOURCE.doProcess", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow doProcess is not linked to a ManagedFunctionNode");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked {@link ProcessState}
	 * with a {@link ManagedFunction}.
	 */
	@Test
	public void managedObjectSourceFlowLinkedToFunction() {

		// Record obtaining the section type
		this.compile.getIssues().recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.compile.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.MANAGED_OBJECT_SOURCE");
		managingOffice.linkFlow(0, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Ensure not required to provide {@link ManagedObjectTeam} for
	 * {@link ManagedObjectSource}.
	 */
	@Test
	public void managedObjectSourceTeamNotLinked() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", TeamManagedObject.class,
				0);
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Ensure able to link in {@link Team} required by a
	 * {@link ManagedObjectSource}.
	 */
	@Test
	public void managedObjectSourceTeamLinked() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", TeamManagedObject.class,
				0);
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_officeBuilder_registerTeam("OFFICE_TEAM", "TEAM");
		this.compile.record_officeBuilder_registerTeam("OFFICE.MANAGED_OBJECT_SOURCE.MANAGED_OBJECT_SOURCE_TEAM",
				"TEAM");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests to ensure can link {@link ManagedObject} with
	 * {@link ManagedObjectPool}.
	 */
	@Test
	public void managedObjectPooling() {

		// Record building the OfficeFloor
		this.compile.record_init();

		// Register the managed object pool for managed object
		OfficeBuilder office = this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.compile.record_officeBuilder_addProcessManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		this.compile.record_managedObjectBuilder_setManagedObjectPool("POOL");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests starting before {@link ManagedObjectSource}.
	 */
	@Test
	public void startBeforeManagedObjectSource() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_managedObjectBuilder_startBefore("OFFICE.MOS_STARTING");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MOS_STARTING", ClassManagedObjectSource.class,
				10, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests starting before {@link ManagedObjectSource} by type.
	 */
	@Test
	public void startBeforeTypeManagedObjectSource() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_managedObjectBuilder_startBefore("OFFICE.MOS_STARTING");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MOS_STARTING", ClassManagedObjectSource.class,
				10, "class.name", AnotherManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests starting before {@link OfficeFloor} {@link ManagedObjectSource} by
	 * type.
	 */
	@Test
	public void startBeforeTypeOfficeFloorManagedObjectSource() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_managedObjectBuilder_startBefore("MOS_STARTING");
		this.compile.record_officeFloorBuilder_addManagedObject("MOS_STARTING", ClassManagedObjectSource.class, 10,
				"class.name", AnotherManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests starting after {@link ManagedObjectSource}.
	 */
	@Test
	public void startAfterManagedObjectSource() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_managedObjectBuilder_startAfter("OFFICE.MOS_STARTING");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MOS_STARTING", ClassManagedObjectSource.class,
				10, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests starting after {@link ManagedObjectSource} by type.
	 */
	@Test
	public void startAfterTypeManagedObjectSource() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_managedObjectBuilder_startAfter("OFFICE.MOS_STARTING");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MOS_STARTING", ClassManagedObjectSource.class,
				10, "class.name", AnotherManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Tests starting after {@link OfficeFloor} {@link ManagedObjectSource} by type.
	 */
	@Test
	public void startAfterTypeOfficeFloorManagedObjectSource() {

		// Record building the OfficeFloor
		this.compile.record_init();
		this.compile.record_officeFloorBuilder_addOffice("OFFICE");
		this.compile.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name", SimpleManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.compile.record_managedObjectBuilder_startAfter("MOS_STARTING");
		this.compile.record_officeFloorBuilder_addManagedObject("MOS_STARTING", ClassManagedObjectSource.class, 10,
				"class.name", AnotherManagedObject.class.getName());
		this.compile.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile.compile(true);
	}

	/**
	 * Simple class for {@link ClassManagedObjectSource}.
	 */
	public static class SimpleManagedObject {
	}

	/**
	 * Another class for {@link ClassManagedObjectSource}.
	 */
	public static class AnotherManagedObject {
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing a {@link Dependency}.
	 */
	public static class DependencyManagedObject {

		@Dependency
		SimpleManagedObject dependency;
	}

	/**
	 * Class for {@link ClassManagedFunctionSource}.
	 */
	public static class ProcessClass {

		public void process(Integer parameter) {
		}
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
	 * Class for {@link ClassManagedObjectSource} containing a {@link FlowInterface}
	 * and a {@link Dependency}.
	 */
	public static class InputManagedObject {

		@FlowInterface
		public static interface Processes {
			void doProcess(String parameter);
		}

		@Dependency
		SimpleManagedObject dependency;

		Processes processes;
	}

	/**
	 * {@link ManagedObjectSource} requiring a {@link Team}.
	 */
	@TestSource
	public static class TeamManagedObject extends AbstractManagedObjectSource<None, None>
			implements ManagedFunctionFactory<None, None> {

		/*
		 * ================= AbstractManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);

			// Require a team
			ManagedObjectSourceContext<?> mosContext = context.getManagedObjectSourceContext();
			mosContext.addManagedFunction("FUNCTION", this).setResponsibleTeam("MANAGED_OBJECT_SOURCE_TEAM");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return fail("Should not require obtaining managed object in compiling");
		}

		/*
		 * ==================== ManagedFunctionFactory ====================
		 */

		@Override
		public ManagedFunction<None, None> createManagedFunction() {
			return fail("Should not require function in compiling");
		}
	}

	/**
	 * {@link ManagedObjectSource} requiring a
	 * {@link ManagedObjectFunctionDependency}.
	 */
	@TestSource
	public static class FunctionDependencyManagedObject extends AbstractManagedObjectSource<None, None> {

		/*
		 * ================ ManagedObjectSource ====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);
			context.getManagedObjectSourceContext().addFunctionDependency("DEPENDENCY", String.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return fail("Should not require obtaining managed object in compiling");
		}
	}

}
