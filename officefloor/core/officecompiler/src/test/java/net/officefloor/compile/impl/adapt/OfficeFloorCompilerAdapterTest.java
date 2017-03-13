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
package net.officefloor.compile.impl.adapt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;
import net.officefloor.compile.TypeLoader;
import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.IssueCapture;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.office.OfficeModelOfficeSource;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;
import net.officefloor.model.impl.section.SectionModelSectionSource;
import net.officefloor.plugin.administrator.clazz.ClassAdministratorSource;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * Tests the {@link OfficeFloorCompilerAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCompilerAdapterTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCompiler} that is being adapted by
	 * {@link OfficeFloorCompilerAdapter} for testing.
	 */
	private OfficeFloorCompiler compiler;

	/**
	 * {@link TypeLoader}.
	 */
	private TypeLoader typeLoader;

	@Override
	protected void setUp() throws Exception {

		// Create Class Loader for testing
		ClassLoader classLoader = createNewClassLoader();

		// Ensure adapted for testing
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
		assertTrue("Ensure compiler is adapted",
				this.compiler instanceof OfficeFloorCompilerAdapter);

		// Load the properties and aliases
		this.compiler.addEnvProperties();
		this.compiler.addSourceAliases();

		// Obtain the type loader
		this.typeLoader = this.compiler.getTypeLoader();
	}

	@Override
	protected void tearDown() throws Exception {
		AutoWireManagement.closeAllOfficeFloors();
	}

	/**
	 * Ensure able to run {@link OfficeFloorCompilerRunnable} by non-adapted
	 * {@link OfficeFloorCompiler}.
	 */
	public void testOfficeFloorCompilerRunnable() throws Exception {

		// Obtain non-adapted compiler executes runnable
		OfficeFloorCompiler nonAdaptedCompiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		assertFalse("Should not be adapted",
				nonAdaptedCompiler instanceof OfficeFloorCompilerAdapter);

		// Ensure obtain changes in this class loader
		MockOfficeFloorCompilerRunnable.value = "CURRENT";
		MockRunnableResult result = nonAdaptedCompiler.run(
				MockOfficeFloorCompilerRunnable.class, "ONE", "TWO",
				new MockOfficeFloorCompilerRunnable());
		assertEquals("Should be value from current class loader", "CURRENT",
				result.getValue());
	}

	/**
	 * Ensure able to run {@link OfficeFloorCompilerRunnable} by
	 * {@link OfficeFloorCompilerAdapter}.
	 */
	public void testOfficeFloorCompilerAdapterRunnable() throws Exception {

		// Ensure not use current class loader values
		MockOfficeFloorCompilerRunnable.value = "CURRENT";
		MockRunnableResult result = this.compiler.run(
				MockOfficeFloorCompilerRunnable.class, "ONE", "TWO",
				new MockOfficeFloorCompilerRunnable());
		assertEquals("Should be initial value from newly loaded class",
				"INITIAL", result.getValue());
	}

	/**
	 * Mock {@link OfficeFloorCompilerRunnable} result to ensure can adapt
	 * result.
	 */
	public static interface MockRunnableResult {

		/**
		 * Obtains the value.
		 * 
		 * @return Value.
		 */
		String getValue();
	}

	/**
	 * Mock {@link OfficeFloorCompilerRunnable}.
	 */
	public static class MockOfficeFloorCompilerRunnable implements
			OfficeFloorCompilerRunnable<MockRunnableResult>, MockRunnableResult {

		/**
		 * Initial value for each {@link ClassLoader}.
		 */
		public static String value = "INITIAL";

		/*
		 * ============== OfficeFloorCompilerRunnable ============
		 */

		@Override
		public MockRunnableResult run(OfficeFloorCompiler compiler,
				Object[] parameters) throws Exception {

			// Ensure have OfficeFloorCompiler
			assertNotNull("Should have compiler", compiler);

			// Ensure appropriate parameters
			assertEquals("Incorrect number of parameters", 3, parameters.length);
			assertEquals("Incorrect first parameter", "ONE", parameters[0]);
			assertEquals("Incorrect second parameter", "TWO", parameters[1]);

			// Ensure can bridge parameter interfaces
			MockRunnableResult parameter = (MockRunnableResult) parameters[2];
			assertEquals("Incorrect third parameter", "CURRENT",
					parameter.getValue());

			// Return to ensure can adapt result
			return this;
		}

		/*
		 * ================== MockRunnableResult =================
		 */

		@Override
		public String getValue() {
			// Return the value (for current class loader)
			return value;
		}
	}

	/**
	 * Test able to compile and run {@link OfficeFloor}.
	 */
	public void testCompileAndRunOfficeFloor() throws Exception {

		// Build OfficeFloor
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource(
				this.compiler);
		source.assignDefaultTeam(PassiveTeamSource.class.getName());
		source.addSection("TEST", ClassSectionSource.class.getName(),
				AdaptWork.class.getName());
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();

		// Invoke the task
		File checkFile = File.createTempFile(this.getClass().getSimpleName(),
				"test");
		officeFloor
				.invokeTask("TEST.WORK", "task", checkFile.getAbsolutePath());

		// Ensure the invoked task
		String contents = this.getFileContents(checkFile);
		assertEquals("Task should be invoked", "TASK INVOKED", contents);
	}

	/**
	 * Functionality for testing.
	 */
	public static class AdaptWork {

		/**
		 * {@link ManagedFunction}.
		 * 
		 * @param fileLocation
		 *            Location of the {@link File} to notify run. Necessary as
		 *            running in separate {@link ClassLoader} instances so can
		 *            not use static check.
		 * @throws IOException
		 *             If fails to write task invoked to file.
		 */
		public void task(@Parameter String fileLocation) throws IOException {
			// Write invoked task to file
			FileWriter writer = new FileWriter(fileLocation, false);
			writer.write("TASK INVOKED");
			writer.flush();
			writer.close();
		}
	}

	/**
	 * Ensure able to use a {@link ManagedObject}.
	 */
	public void testManagedObject() throws Exception {

		// Build OfficeFloor
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource(
				this.compiler);
		source.assignDefaultTeam(PassiveTeamSource.class.getName());
		source.addSection("TEST", ClassSectionSource.class.getName(),
				AdaptManagedObjectWork.class.getName());
		source.addManagedObject(ClassManagedObjectSource.class.getName(), null,
				new AutoWire(AdaptManagedObject.class)).addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				AdaptManagedObjectImpl.class.getName());
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();

		// Invoke the task
		File checkFile = File.createTempFile(this.getClass().getSimpleName(),
				"test");
		officeFloor
				.invokeTask("TEST.WORK", "task", checkFile.getAbsolutePath());

		// Ensure the invoked task
		String contents = this.getFileContents(checkFile);
		assertEquals("Task should be invoked", "MANAGED OBJECT INVOKED",
				contents);
	}

	/**
	 * Ensure able to use a {@link Object}.
	 */
	public void testObject() throws Exception {

		final AdaptManagedObjectImpl object = new AdaptManagedObjectImpl();

		// Build OfficeFloor
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource(
				this.compiler);
		source.assignDefaultTeam(PassiveTeamSource.class.getName());
		source.addSection("TEST", ClassSectionSource.class.getName(),
				AdaptManagedObjectWork.class.getName());
		source.addObject(object, new AutoWire(AdaptManagedObject.class));
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();

		// Invoke the task
		File checkFile = File.createTempFile(this.getClass().getSimpleName(),
				"test");
		officeFloor
				.invokeTask("TEST.WORK", "task", checkFile.getAbsolutePath());

		// Ensure the invoked managed object
		assertTrue("Managed Object should be flagged as invoked",
				object.isInvoked);
		String contents = this.getFileContents(checkFile);
		assertEquals("Managed Object should be invoked",
				"MANAGED OBJECT INVOKED", contents);
	}

	/**
	 * Functionality for testing.
	 */
	public static class AdaptManagedObjectWork {

		/**
		 * {@link ManagedFunction}.
		 * 
		 * @param mo
		 *            {@link AdaptManagedObject}.
		 * @param fileLocation
		 *            Location of the file.
		 */
		public void task(AdaptManagedObject mo, @Parameter String fileLocation)
				throws IOException {
			mo.useManagedObject(fileLocation);
		}
	}

	/**
	 * {@link ManagedObject} for testing.
	 */
	public static interface AdaptManagedObject {

		/**
		 * Invoked to use the {@link ManagedObject}.
		 * 
		 * @param fileLocation
		 *            Location of the {@link File} to notify run. Necessary as
		 *            running in separate {@link ClassLoader} instances so can
		 *            not use static check.
		 * @throws IOException
		 *             If fails to write {@link ManagedObject} invoked to file.
		 */
		void useManagedObject(String fileLocation) throws IOException;
	}

	/**
	 * Implementation for testing.
	 */
	public static class AdaptManagedObjectImpl implements AdaptManagedObject {

		/**
		 * Flag indicating if invoked.
		 */
		public boolean isInvoked = false;

		@Override
		public void useManagedObject(String fileLocation) throws IOException {

			// Indicate invoked
			this.isInvoked = true;

			// Write invoked task to file
			FileWriter writer = new FileWriter(fileLocation, false);
			writer.write("MANAGED OBJECT INVOKED");
			writer.flush();
			writer.close();
		}
	}

	/**
	 * Tests the {@link OfficeFloorLoader}.
	 */
	public void testOfficeFloorLoader() {
		OfficeFloorLoader loader = this.compiler.getOfficeFloorLoader();
		PropertyList specification = loader
				.loadSpecification(OfficeFloorModelOfficeFloorSource.class);
		assertEquals("Should be no properties", 0,
				specification.getPropertyNames().length);
	}

	/**
	 * Tests the {@link OfficeLoader}.
	 */
	public void testOfficeLoader() {
		OfficeLoader loader = this.compiler.getOfficeLoader();
		PropertyList specification = loader
				.loadSpecification(OfficeModelOfficeSource.class);
		assertEquals("Should be no properties", 0,
				specification.getPropertyNames().length);
	}

	/**
	 * Tests the {@link SectionLoader}.
	 */
	public void testSectionLoader() {
		SectionLoader loader = this.compiler.getSectionLoader();
		PropertyList specification = loader
				.loadSpecification(SectionModelSectionSource.class);
		assertEquals("Should be no properties", 0,
				specification.getPropertyNames().length);
	}

	/**
	 * Tests obtaining specification from {@link ManagedFunctionLoader}.
	 */
	public void testWorkLoaderSpecification() {
		ManagedFunctionLoader loader = this.compiler.getWorkLoader();
		PropertyList specification = loader
				.loadSpecification(ClassWorkSource.class);
		assertEquals("Should have a property", 1,
				specification.getPropertyNames().length);
	}

	/**
	 * Tests obtaining {@link FunctionNamespaceType} from {@link TypeLoader}.
	 */
	public void testWorkFromTypeLoader() {
		PropertyList properties = this.compiler.createPropertyList();
		properties.addProperty(ClassWorkSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(AdaptManagedObjectWork.class.getName());
		FunctionNamespaceType<?> workType = this.typeLoader.loadWorkType(
				ClassWorkSource.class.getName(), properties);
		assertNotNull("Must have work type", workType);
	}

	/**
	 * Tests the {@link ManagedObjectLoader}.
	 */
	public void testManagedObjectLoader() {
		ManagedObjectLoader loader = this.compiler.getManagedObjectLoader();
		PropertyList specification = loader
				.loadSpecification(ClassManagedObjectSource.class);
		assertEquals("Should have a property", 1,
				specification.getPropertyNames().length);
	}

	/**
	 * Tests obtaining {@link ManagedObjectType} from {@link TypeLoader}.
	 */
	public void testManagedObjectFromTypeLoader() {
		PropertyList properties = this.compiler.createPropertyList();
		properties.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(
				AdaptManagedObjectImpl.class.getName());
		ManagedObjectType<?> managedObjectType = this.typeLoader
				.loadManagedObjectType(
						ClassManagedObjectSource.class.getName(), properties);
		assertNotNull("Must have managed object type", managedObjectType);
	}

	/**
	 * Tests the {@link SupplierLoader}.
	 */
	public void testSupplierLoader() {
		SupplierLoader loader = this.compiler.getSupplierLoader();
		PropertyList specification = loader
				.loadSpecification(MockSupplierSource.class);
		assertEquals("Should have a property", 1,
				specification.getPropertyNames().length);
	}

	/**
	 * Mock {@link SupplierSource}.
	 */
	public static class MockSupplierSource extends AbstractSupplierSource {

		/*
		 * ===================== SupplierSource ====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("TEST");
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			fail("Should not be invoked in checking specification");
		}
	}

	/**
	 * Tests the {@link GovernanceLoader}.
	 */
	public void testGovernanceLoader() {
		GovernanceLoader loader = this.compiler.getGovernanceLoader();
		PropertyList specification = loader
				.loadSpecification(ClassGovernanceSource.class);
		assertEquals("Should have a property", 1,
				specification.getPropertyNames().length);
	}

	/**
	 * Tests the {@link AdministrationLoader}.
	 */
	public void testAdministratorLoader() {
		AdministrationLoader loader = this.compiler.getAdministratorLoader();
		PropertyList specification = loader
				.loadSpecification(ClassAdministratorSource.class);
		assertEquals("Should have a property", 1,
				specification.getPropertyNames().length);
	}

	/**
	 * Tests the {@link Team}.
	 */
	public void testTeamLoader() {
		TeamLoader loader = this.compiler.getTeamLoader();
		PropertyList specification = loader
				.loadSpecification(PassiveTeamSource.class);
		assertEquals("Should be no properties", 0,
				specification.getPropertyNames().length);
	}

	/**
	 * Ensure able to be provided details of the cause of the
	 * {@link CompilerIssues} issue maintaining type.
	 */
	public void testCompilerIssueSameTypeCause() {
		this.doCompilerIssueCauseTest(new NullPointerException("TEST"),
				NullPointerException.class);
	}

	/**
	 * Ensure able to be provided details of the cause of the
	 * {@link CompilerIssues} issue, with need to adapt the cause.
	 */
	public void testCompilerIssueAdaptCause() {
		this.doCompilerIssueCauseTest(new NonAdaptableException("TEST", null),
				AdaptedException.class);
	}

	/**
	 * Undertakes the {@link CompilerIssues} cause test.
	 */
	private void doCompilerIssueCauseTest(Exception cause,
			Class<?> expectedCauseType) {

		// Specify to capture the cause
		final Throwable[] adaptedCause = new Throwable[1];
		final CompilerIssues issues = new CompilerIssues() {
			@Override
			public void addIssue(Node node, String issueDescription,
					Throwable cause) {
				// Register the adapted cause
				adaptedCause[0] = cause;
			}

			@Override
			public void addIssue(Node node, String issueDescription,
					CompilerIssue... causes) {
				fail("Should not be invoked - " + issueDescription);
			}

			@Override
			public <R> IssueCapture<R> captureIssues(Supplier<R> runnable) {
				fail("Should not be invoked");
				return null;
			}
		};
		this.compiler.setCompilerIssues(issues);

		// Load type to trigger issue with cause
		ManagedObjectLoader loader = this.compiler.getManagedObjectLoader();
		ManagedObjectType<None> type = loader.loadManagedObjectType(
				new MockFailManagedObjectSource(cause),
				this.compiler.createPropertyList());
		assertNull("Should not load type", type);

		// Validate adapted the cause
		Throwable actualCause = adaptedCause[0];
		assertNotNull("Should have adapted cause", actualCause);
		assertEquals("Incorrect adapted cause type", expectedCauseType,
				actualCause.getClass());
		assertEquals("Ensure provides details of original cause",
				cause.getMessage(), adaptedCause[0].getMessage());
	}

	/**
	 * Non-adaptable {@link Exception}.
	 */
	public static class NonAdaptableException extends Exception {

		/**
		 * Initiate.
		 * 
		 * @param message
		 *            Message.
		 * @param nonAdaptSignature
		 *            Constructor is non-adaptable.
		 */
		public NonAdaptableException(String message, Object nonAdaptSignature) {
			super(message);
		}
	}

	/**
	 * Mock {@link ManagedObjectSource} to propagate a failure.
	 */
	@TestSource
	public static class MockFailManagedObjectSource extends
			AbstractManagedObjectSource<None, None> {

		/**
		 * Failure.
		 */
		private final Exception failure;

		/**
		 * Initiate.
		 * 
		 * @param failure
		 *            Failure.
		 */
		public MockFailManagedObjectSource(Exception failure) {
			this.failure = failure;
		}

		/*
		 * =================== ManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not be invoked");
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			throw this.failure;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not be invoked");
			return null;
		}
	}

}