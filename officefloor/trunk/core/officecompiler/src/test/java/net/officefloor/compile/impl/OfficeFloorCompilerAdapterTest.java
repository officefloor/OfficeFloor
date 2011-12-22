/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.compile.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
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

	@Override
	protected void setUp() throws Exception {

		// Create Class Loader for testing
		String[] classPathEntries = System.getProperty("java.class.path")
				.split(File.pathSeparator);
		URL[] urls = new URL[classPathEntries.length];
		for (int i = 0; i < urls.length; i++) {
			String classPathEntry = classPathEntries[i];
			classPathEntry = (classPathEntry.startsWith(File.separator) ? "file://"
					+ classPathEntry
					: classPathEntry);
			classPathEntry = (classPathEntry.endsWith(".jar") ? classPathEntry
					: classPathEntry + "/");
			urls[i] = new URL(classPathEntry);
		}
		ClassLoader classLoader = new URLClassLoader(urls, null);

		// Ensure adapted for testing
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
		assertTrue("Ensure compiler is adapted",
				this.compiler instanceof OfficeFloorCompilerAdapter);

		// Load the properties and aliases
		this.compiler.addEnvProperties();
		this.compiler.addSourceAliases();
	}

	@Override
	protected void tearDown() throws Exception {
		AutoWireManagement.closeAllOfficeFloors();
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
		 * {@link Task}.
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
		 * {@link Task}.
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
	 * Tests the {@link WorkLoader}.
	 */
	public void testWorkLoader() {
		WorkLoader loader = this.compiler.getWorkLoader();
		PropertyList specification = loader
				.loadSpecification(ClassWorkSource.class);
		assertEquals("Should have a property", 1,
				specification.getPropertyNames().length);
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
	 * Tests the {@link AdministratorLoader}.
	 */
	public void testAdministratorLoader() {
		AdministratorLoader loader = this.compiler.getAdministratorLoader();
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
			public void addIssue(LocationType locationType, String location,
					AssetType assetType, String assetName,
					String issueDescription, Throwable cause) {
				// Register the adapted cause
				adaptedCause[0] = cause;
			}

			@Override
			public void addIssue(LocationType locationType, String location,
					AssetType assetType, String assetName,
					String issueDescription) {
				fail("Should not be invoked - " + issueDescription);
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