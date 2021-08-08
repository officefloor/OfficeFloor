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

package net.officefloor.compile.impl.adapt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Supplier;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;
import net.officefloor.compile.TypeLoader;
import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.executive.ExecutiveLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.IssueCapture;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.office.OfficeModelOfficeSource;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;
import net.officefloor.model.impl.section.SectionModelSectionSource;
import net.officefloor.plugin.administration.clazz.ClassAdministrationSource;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

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
		assertTrue("Ensure compiler is adapted", this.compiler instanceof OfficeFloorCompilerAdapter);

		// Load the properties and aliases
		this.compiler.addEnvProperties();
		this.compiler.addSourceAliases();

		// Obtain the type loader
		this.typeLoader = this.compiler.getTypeLoader();
	}

	/**
	 * Ensure able to run {@link OfficeFloorCompilerRunnable} by non-adapted
	 * {@link OfficeFloorCompiler}.
	 */
	public void testOfficeFloorCompilerRunnable() throws Exception {

		// Obtain non-adapted compiler executes runnable
		OfficeFloorCompiler nonAdaptedCompiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		assertFalse("Should not be adapted", nonAdaptedCompiler instanceof OfficeFloorCompilerAdapter);

		// Ensure obtain changes in this class loader
		MockOfficeFloorCompilerRunnable.value = "CURRENT";
		MockRunnableResult result = nonAdaptedCompiler.run(MockOfficeFloorCompilerRunnable.class, "ONE", "TWO",
				new MockOfficeFloorCompilerRunnable());
		assertEquals("Should be value from current class loader", "CURRENT", result.getValue());
	}

	/**
	 * Ensure able to run {@link OfficeFloorCompilerRunnable} by
	 * {@link OfficeFloorCompilerAdapter}.
	 */
	public void testOfficeFloorCompilerAdapterRunnable() throws Exception {

		// Ensure not use current class loader values
		MockOfficeFloorCompilerRunnable.value = "CURRENT";
		MockRunnableResult result = this.compiler.run(MockOfficeFloorCompilerRunnable.class, "ONE", "TWO",
				new MockOfficeFloorCompilerRunnable());
		assertEquals("Should be initial value from newly loaded class", "INITIAL", result.getValue());
	}

	/**
	 * Mock {@link OfficeFloorCompilerRunnable} result to ensure can adapt result.
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
	public static class MockOfficeFloorCompilerRunnable
			implements OfficeFloorCompilerRunnable<MockRunnableResult>, MockRunnableResult {

		/**
		 * Initial value for each {@link ClassLoader}.
		 */
		public static String value = "INITIAL";

		/*
		 * ============== OfficeFloorCompilerRunnable ============
		 */

		@Override
		public MockRunnableResult run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception {

			// Ensure have OfficeFloorCompiler
			assertNotNull("Should have compiler", compiler);

			// Ensure appropriate parameters
			assertEquals("Incorrect number of parameters", 3, parameters.length);
			assertEquals("Incorrect first parameter", "ONE", parameters[0]);
			assertEquals("Incorrect second parameter", "TWO", parameters[1]);

			// Ensure can bridge parameter interfaces
			MockRunnableResult parameter = (MockRunnableResult) parameters[2];
			assertEquals("Incorrect third parameter", "CURRENT", parameter.getValue());

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
		this.compiler.setOfficeFloorSourceClass(OfficeLoaderOfficeFloorSource.class);
		this.compiler.addProperty(OfficeLoaderOfficeFloorSource.PROPERTY_OFFICE_CLASS_NAME,
				CompileAndRunOfficeSource.class.getName());
		OfficeFloor officeFloor = this.compiler.compile("OfficeFloor");
		officeFloor.openOfficeFloor();

		// Invoke the function
		File checkFile = File.createTempFile(this.getClass().getSimpleName(), "test");
		Office office = officeFloor.getOffice("OFFICE");
		FunctionManager function = office.getFunctionManager("TEST.task");
		function.invokeProcess(checkFile.getAbsolutePath(), null);

		// Ensure the invoked task
		String contents = this.getFileContents(checkFile);
		assertEquals("Task should be invoked", "TASK INVOKED", contents);
	}

	@TestSource
	public static class CompileAndRunOfficeSource extends AbstractOfficeSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			officeArchitect.addOfficeSection("TEST", ClassSectionSource.class.getName(), AdaptWork.class.getName());
		}
	}

	/**
	 * Functionality for testing.
	 */
	public static class AdaptWork {

		/**
		 * {@link ManagedFunction}.
		 * 
		 * @param fileLocation Location of the {@link File} to notify run. Necessary as
		 *                     running in separate {@link ClassLoader} instances so can
		 *                     not use static check.
		 * @throws IOException If fails to write task invoked to file.
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
		this.compiler.setOfficeFloorSourceClass(OfficeLoaderOfficeFloorSource.class);
		this.compiler.addProperty(OfficeLoaderOfficeFloorSource.PROPERTY_OFFICE_CLASS_NAME,
				ManagedObjectOfficeSource.class.getName());
		OfficeFloor officeFloor = this.compiler.compile("OfficeFloor");
		officeFloor.openOfficeFloor();

		// Invoke the function
		File checkFile = File.createTempFile(this.getClass().getSimpleName(), "test");
		officeFloor.getOffice("OFFICE").getFunctionManager("TEST.task").invokeProcess(checkFile.getAbsolutePath(),
				null);

		// Ensure the invoked task
		String contents = this.getFileContents(checkFile);
		assertEquals("Task should be invoked", "MANAGED OBJECT INVOKED", contents);
	}

	@TestSource
	public static class ManagedObjectOfficeSource extends AbstractOfficeSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			officeArchitect.enableAutoWireObjects();
			officeArchitect.addOfficeSection("TEST", ClassSectionSource.class.getName(),
					AdaptManagedObjectWork.class.getName());
			OfficeManagedObjectSource mos = officeArchitect.addOfficeManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, AdaptManagedObjectImpl.class.getName());
			mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		}
	}

	/**
	 * Ensure able to use a {@link Object}.
	 */
	public void testObject() throws Exception {

		final AdaptManagedObjectImpl object = new AdaptManagedObjectImpl();

		// Build OfficeFloor
		this.compiler.setOfficeFloorSource(new ObjectOfficeFloorSource(object));
		OfficeFloor officeFloor = this.compiler.compile("OfficeFloor");
		officeFloor.openOfficeFloor();

		// Invoke the function
		File checkFile = File.createTempFile(this.getClass().getSimpleName(), "test");
		officeFloor.getOffice("OFFICE").getFunctionManager("TEST.task").invokeProcess(checkFile.getAbsolutePath(),
				null);

		// Ensure the invoked managed object
		assertTrue("Managed Object should be flagged as invoked", object.isInvoked);
		String contents = this.getFileContents(checkFile);
		assertEquals("Managed Object should be invoked", "MANAGED OBJECT INVOKED", contents);
	}

	@TestSource
	public static class ObjectOfficeFloorSource extends AbstractOfficeFloorSource {

		private AdaptManagedObject object;

		public ObjectOfficeFloorSource(AdaptManagedObject object) {
			this.object = object;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void specifyConfigurationProperties(RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
			deployer.addDeployedOffice("OFFICE", new ObjectOfficeSource(this.object), null);
		}
	}

	@TestSource
	public static class ObjectOfficeSource extends AbstractOfficeSource {

		private AdaptManagedObject object;

		public ObjectOfficeSource(AdaptManagedObject object) {
			this.object = object;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			officeArchitect.enableAutoWireObjects();
			officeArchitect.addOfficeSection("TEST", ClassSectionSource.class.getName(),
					AdaptManagedObjectWork.class.getName());
			Singleton.load(officeArchitect, this.object);
		}
	}

	/**
	 * Functionality for testing.
	 */
	public static class AdaptManagedObjectWork {

		/**
		 * {@link ManagedFunction}.
		 * 
		 * @param mo           {@link AdaptManagedObject}.
		 * @param fileLocation Location of the file.
		 */
		public void task(AdaptManagedObject mo, @Parameter String fileLocation) throws IOException {
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
		 * @param fileLocation Location of the {@link File} to notify run. Necessary as
		 *                     running in separate {@link ClassLoader} instances so can
		 *                     not use static check.
		 * @throws IOException If fails to write {@link ManagedObject} invoked to file.
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
		PropertyList specification = loader.loadSpecification(OfficeFloorModelOfficeFloorSource.class);
		assertEquals("Should be no properties", 0, specification.getPropertyNames().length);
	}

	/**
	 * Tests the {@link OfficeLoader}.
	 */
	public void testOfficeLoader() {
		OfficeLoader loader = this.compiler.getOfficeLoader();
		PropertyList specification = loader.loadSpecification(OfficeModelOfficeSource.class);
		assertEquals("Should be no properties", 0, specification.getPropertyNames().length);
	}

	/**
	 * Tests the {@link SectionLoader}.
	 */
	public void testSectionLoader() {
		SectionLoader loader = this.compiler.getSectionLoader();
		PropertyList specification = loader.loadSpecification(SectionModelSectionSource.class);
		assertEquals("Should be no properties", 0, specification.getPropertyNames().length);
	}

	/**
	 * Tests obtaining specification from {@link ManagedFunctionLoader}.
	 */
	public void testManagedFunctionLoaderSpecification() {
		ManagedFunctionLoader loader = this.compiler.getManagedFunctionLoader();
		PropertyList specification = loader.loadSpecification(ClassManagedFunctionSource.class);
		assertEquals("Should have a property", 1, specification.getPropertyNames().length);
	}

	/**
	 * Tests obtaining {@link FunctionNamespaceType} from {@link TypeLoader}.
	 */
	public void testManagedFunctionFromTypeLoader() {
		PropertyList properties = this.compiler.createPropertyList();
		properties.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(AdaptManagedObjectWork.class.getName());
		FunctionNamespaceType namespaceType = this.typeLoader.loadManagedFunctionType("MANAGED_FUNCTION",
				ClassManagedFunctionSource.class.getName(), properties);
		assertNotNull("Must have managed function type", namespaceType);
	}

	/**
	 * Tests the {@link ManagedObjectLoader}.
	 */
	public void testManagedObjectLoader() {
		ManagedObjectLoader loader = this.compiler.getManagedObjectLoader();
		PropertyList specification = loader.loadSpecification(ClassManagedObjectSource.class);
		assertEquals("Should have a property", 1, specification.getPropertyNames().length);
	}

	/**
	 * Tests obtaining {@link ManagedObjectType} from {@link TypeLoader}.
	 */
	public void testManagedObjectFromTypeLoader() {
		PropertyList properties = this.compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(AdaptManagedObjectImpl.class.getName());
		ManagedObjectType<?> managedObjectType = this.typeLoader.loadManagedObjectType("MANAGED_OBJECT",
				ClassManagedObjectSource.class.getName(), properties);
		assertNotNull("Must have managed object type", managedObjectType);
	}

	/**
	 * Tests the {@link SupplierLoader}.
	 */
	public void testSupplierLoader() {
		SupplierLoader loader = this.compiler.getSupplierLoader();
		PropertyList specification = loader.loadSpecification(MockSupplierSource.class);
		assertEquals("Should have a property", 1, specification.getPropertyNames().length);
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

		@Override
		public void terminate() {
			// nothing to terminate
		}
	}

	/**
	 * Tests the {@link GovernanceLoader}.
	 */
	public void testGovernanceLoader() {
		GovernanceLoader loader = this.compiler.getGovernanceLoader();
		PropertyList specification = loader.loadSpecification(ClassGovernanceSource.class);
		assertEquals("Should have a property", 1, specification.getPropertyNames().length);
	}

	/**
	 * Tests the {@link AdministrationLoader}.
	 */
	public void testAdministrationLoader() {
		AdministrationLoader loader = this.compiler.getAdministrationLoader();
		PropertyList specification = loader.loadSpecification(ClassAdministrationSource.class);
		assertEquals("Should have a property", 1, specification.getPropertyNames().length);
	}

	/**
	 * Tests the {@link Team}.
	 */
	public void testTeamLoader() {
		TeamLoader loader = this.compiler.getTeamLoader();
		PropertyList specification = loader.loadSpecification(PassiveTeamSource.class);
		assertEquals("Should be no properties", 0, specification.getPropertyNames().length);
	}

	/**
	 * Tests the {@link Executive}.
	 */
	public void testExecutiveLoader() {
		ExecutiveLoader loader = this.compiler.getExecutiveLoader();
		PropertyList specification = loader.loadSpecification(DefaultExecutive.class);
		assertEquals("Should be no properties", 0, specification.getPropertyNames().length);
	}

	/**
	 * Ensure able to be provided details of the cause of the {@link CompilerIssues}
	 * issue maintaining type.
	 */
	public void testCompilerIssueSameTypeCause() {
		this.doCompilerIssueCauseTest(new NullPointerException("TEST"), NullPointerException.class);
	}

	/**
	 * Ensure able to be provided details of the cause of the {@link CompilerIssues}
	 * issue, with need to adapt the cause.
	 */
	public void testCompilerIssueAdaptCause() {
		this.doCompilerIssueCauseTest(new NonAdaptableException("TEST", null), AdaptedException.class);
	}

	/**
	 * Undertakes the {@link CompilerIssues} cause test.
	 */
	private void doCompilerIssueCauseTest(Exception cause, Class<?> expectedCauseType) {

		// Specify to capture the cause
		final Throwable[] adaptedCause = new Throwable[1];
		final CompilerIssues issues = new CompilerIssues() {
			@Override
			public CompileError addIssue(Node node, String issueDescription, Throwable cause) {
				// Register the adapted cause
				adaptedCause[0] = cause;
				return new CompileError(issueDescription);
			}

			@Override
			public CompileError addIssue(Node node, String issueDescription, CompilerIssue... causes) {
				fail("Should not be invoked - " + issueDescription);
				return new CompileError(issueDescription);
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
		ManagedObjectType<None> type = loader.loadManagedObjectType(new MockFailManagedObjectSource(cause),
				this.compiler.createPropertyList());
		assertNull("Should not load type", type);

		// Validate adapted the cause
		Throwable actualCause = adaptedCause[0];
		assertNotNull("Should have adapted cause", actualCause);
		assertEquals("Incorrect adapted cause type", expectedCauseType, actualCause.getClass());
		assertEquals("Ensure provides details of original cause", cause.getMessage(), adaptedCause[0].getMessage());
	}

	/**
	 * Non-adaptable {@link Exception}.
	 */
	public static class NonAdaptableException extends Exception {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Initiate.
		 * 
		 * @param message           Message.
		 * @param nonAdaptSignature Constructor is non-adaptable.
		 */
		public NonAdaptableException(String message, Object nonAdaptSignature) {
			super(message);
		}
	}

	/**
	 * Mock {@link ManagedObjectSource} to propagate a failure.
	 */
	@TestSource
	public static class MockFailManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		/**
		 * Failure.
		 */
		private final Exception failure;

		/**
		 * Initiate.
		 * 
		 * @param failure Failure.
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
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			throw this.failure;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not be invoked");
			return null;
		}
	}

	/**
	 * {@link OfficeFloorSource} to load {@link Office} in another
	 * {@link ClassLoader}.
	 */
	@TestSource
	public static class OfficeLoaderOfficeFloorSource extends AbstractOfficeFloorSource {

		/**
		 * Name of {@link Property} to obtain the {@link OfficeSource} class name.
		 */
		public static final String PROPERTY_OFFICE_CLASS_NAME = "office.class.name";

		/*
		 * ===================== OfficeFloorSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_OFFICE_CLASS_NAME);
		}

		@Override
		public void specifyConfigurationProperties(RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
			String officeSourceClassName = context.getProperty(PROPERTY_OFFICE_CLASS_NAME);
			deployer.addDeployedOffice("OFFICE", officeSourceClassName, null);
		}
	}

}
