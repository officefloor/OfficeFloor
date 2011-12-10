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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.office.OfficeModelOfficeSource;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;
import net.officefloor.model.impl.section.SectionModelSectionSource;
import net.officefloor.plugin.administrator.clazz.ClassAdministratorSource;
import net.officefloor.plugin.autowire.AutoWireAdministration;
import net.officefloor.plugin.autowire.AutoWireOfficeFloor;
import net.officefloor.plugin.autowire.AutoWireOfficeFloorSource;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Govern;
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
		AutoWireAdministration.closeAllOfficeFloors();
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
				AdaptManagedObject.class).addProperty(
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
		source.addObject(object, AdaptManagedObject.class);
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
	 * {@link CompilerIssues} issue.
	 */
	public void testCompilerIssueCause() {

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

		// Load governance class without @govern to trigger issue cause
		PropertyList properties = this.compiler.createPropertyList();
		properties.addProperty(ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MockInvalidGovernance.class.getName());

		// Load type to trigger issue with cause
		GovernanceLoader loader = this.compiler.getGovernanceLoader();
		GovernanceType<?, ?> type = loader.loadGovernanceType(
				ClassGovernanceSource.class, properties);
		assertNull("Should not load governance type", type);

		// Validate adapted the cause
		assertNotNull("Should have adapted cause", adaptedCause[0]);
		assertTrue("Should be adapted cause of this class loader",
				adaptedCause[0] instanceof AdaptedException);
		assertEquals("Ensure provides details of original cause",
				"A method must be annotated with @Govern",
				adaptedCause[0].getMessage());
	}

	/**
	 * Mock invalid class for {@link ClassGovernanceSource}.
	 */
	public static class MockInvalidGovernance {

		/**
		 * No {@link Govern} annotation which will trigger failure.
		 */
		public void invalid() {
		}
	}

}