package net.officefloor.compile.integrate.managedobject;

import java.sql.Connection;

import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFlowNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFunctionDependencyNodeImpl;
import net.officefloor.compile.impl.supplier.MockTypeManagedObjectSource;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
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
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * Tests compiling an {@link Office} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeManagedObjectTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedObjectSource}.
	 */
	public void testSimpleManagedObjectSource() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				10, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} bound to {@link ProcessState}.
	 */
	public void testProcessBoundManagedObject() {

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.recordReturn(office, office.addProcessManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT"),
				null);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a supplied {@link ManagedObject} bound to
	 * {@link ProcessState}.
	 */
	public void testSuppliedManagedObjectSource() {

		// Setup to provide managed object source instance
		MockSupplierSource.reset();
		final String managedObjectName = "OFFICE.MANAGED_OBJECT_SOURCE";
		final MockTypeManagedObjectSource mos = new MockTypeManagedObjectSource(Object.class, managedObjectName);
		MockSupplierSource.managedObjectSource = mos;

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", managedObjectName);
		this.recordReturn(office, office.addProcessManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT"),
				null);

		// Record instance (as supplied)
		this.record_officeFloorBuilder_addManagedObject(managedObjectName, mos, 0, "MO_NAME", "MO_VALUE");

		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
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
			assertEquals("Incorrect property value", "SUPPLY_VALUE", value);

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
	public void testManagedObjectWithDependencyNotLinked() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the office managed object with dependency not linked
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DEPENDENT", "OFFICE.DEPENDENT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENT", "OFFICE.DEPENDENT");
		this.issues.recordIssue("OFFICE.DEPENDENT.dependency", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency dependency is not linked to a BoundManagedObjectNode");

		// Add managed objects to OfficeFloor
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency in {@link Office}.
	 */
	public void testManagedObjectWithDependencyInOffice() {

		// Record building the office
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DEPENDENT", "OFFICE.DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENT",
				"OFFICE.DEPENDENT");
		mapper.mapDependency(0, "OFFICE.SIMPLE");
		office.registerManagedObjectSource("OFFICE.SIMPLE", "OFFICE.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE", "OFFICE.SIMPLE");

		// Add managed objects to office
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency outside
	 * {@link Office}.
	 */
	public void testManagedObjectWithDependencyOutsideOffice() {

		// Record building the office
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DEPENDENT", "OFFICE.DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENT",
				"OFFICE.DEPENDENT");
		mapper.mapDependency(0, "SIMPLE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a
	 * {@link ManagedObjectFunctionDependency} not linked.
	 */
	public void testManagedObjectWithFunctionDependencyNotLinked() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the office managed object with dependency not linked
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DEPENDENT", "OFFICE.DEPENDENT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.DEPENDENT", "OFFICE.DEPENDENT");
		this.issues.recordIssue("OFFICE.DEPENDENT_SOURCE.DEPENDENCY", ManagedObjectFunctionDependencyNodeImpl.class,
				"Managed Object Function Dependency DEPENDENCY is not linked to a BoundManagedObjectNode");

		// Add managed objects to OfficeFloor
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE",
				FunctionDependencyManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a
	 * {@link ManagedObjectFunctionDependency} linked in {@link Office}.
	 */
	public void testManagedObjectWithFunctionDependencyInOffice() {

		// Record building the office
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE",
				FunctionDependencyManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Map in function dependencies
		this.record_managingOfficeBuilder_mapFunctionDependency("DEPENDENCY", "OFFICE.SIMPLE");

		// Register remaining
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SIMPLE", "OFFICE.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE", "OFFICE.SIMPLE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a
	 * {@link ManagedObjectFunctionDependency} linked outside {@link Office}.
	 */
	public void testManagedObjectWithFunctionDependencyOutsideOffice() {

		// Record building the office
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.DEPENDENT_SOURCE",
				FunctionDependencyManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Map in function dependency
		this.record_managingOfficeBuilder_mapFunctionDependency("DEPENDENCY", "SIMPLE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling an Input {@link ManagedObject} with dependency linked to a
	 * {@link ManagedObject} in the {@link Office}.
	 */
	public void testInputManagedObjectWithDependencyInOffice() {

		// Record obtaining the Section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();

		// Register the office with the namespace for the input process flow
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.INPUT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", InputManagedObject.class.getName());
		ManagingOfficeBuilder<?> inputMos = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder inputDependencies = this
				.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.INPUT_SOURCE");
		office.registerManagedObjectSource("OFFICE.SIMPLE", "OFFICE.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE", "OFFICE.SIMPLE");
		inputDependencies.mapDependency(0, "OFFICE.SIMPLE");
		inputMos.linkFlow(0, "SECTION.INPUT");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling an Input {@link ManagedObject} with dependency linked to a
	 * {@link ManagedObject} outside the {@link Office} (e.g. {@link OfficeFloor}).
	 */
	public void testInputManagedObjectWithDependencyOutsideOffice() {

		// Record obtaining the Section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();

		// Register the office with the namespace for the input process flow
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.INPUT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", InputManagedObject.class.getName());
		ManagingOfficeBuilder<?> inputMos = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder inputDependencies = this
				.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.INPUT_SOURCE");
		inputDependencies.mapDependency(0, "SIMPLE");
		inputMos.linkFlow(0, "SECTION.INPUT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if {@link ManagedObjectFlow} of
	 * {@link OfficeManagedObjectSource} is not linked.
	 */
	public void testManagedObjectSourceFlowNotLinked() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.MANAGED_OBJECT_SOURCE");
		this.issues.recordIssue("OFFICE.MANAGED_OBJECT_SOURCE.doProcess", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow doProcess is not linked to a ManagedFunctionNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked {@link ProcessState}
	 * with a {@link ManagedFunction}.
	 */
	public void testManagedObjectSourceFlowLinkedToFunction() {

		// Record obtaining the section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.MANAGED_OBJECT_SOURCE");
		managingOffice.linkFlow(0, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure not required to provide {@link ManagedObjectTeam} for
	 * {@link ManagedObjectSource}.
	 */
	public void testManagedObjectSourceTeamNotLinked() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", TeamManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to link in {@link Team} required by a
	 * {@link ManagedObjectSource}.
	 */
	public void testManagedObjectSourceTeamLinked() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", TeamManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_registerTeam("OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_registerTeam("OFFICE.MANAGED_OBJECT_SOURCE.MANAGED_OBJECT_SOURCE_TEAM", "TEAM");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests to ensure can link {@link ManagedObject} with
	 * {@link ManagedObjectPool}.
	 */
	public void testManagedObjectPooling() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the managed object pool for managed object
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		this.record_managedObjectBuilder_setManagedObjectPool("POOL");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Simple class for {@link ClassManagedObjectSource}.
	 */
	public static class SimpleManagedObject {
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
			fail("Should not require obtaining managed object in compiling");
			return null;
		}

		/*
		 * ==================== ManagedFunctionFactory ====================
		 */

		@Override
		public ManagedFunction<None, None> createManagedFunction() {
			fail("Should not require function in compiling");
			return null;
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
			fail("Should not require obtaining managed object in compiling");
			return null;
		}
	}

}