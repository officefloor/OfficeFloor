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
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireAdministration;
import net.officefloor.plugin.autowire.AutoWireOfficeFloor;
import net.officefloor.plugin.autowire.AutoWireOfficeFloorSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
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

		// Wrap with adapter to test adapter
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
		assertTrue("Ensure compiler is adapted",
				this.compiler instanceof OfficeFloorCompilerAdapter);
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
		source.assignDefaultTeam(PassiveTeamSource.class);
		source.addSection("TEST", ClassSectionSource.class,
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
		source.assignDefaultTeam(PassiveTeamSource.class);
		source.addSection("TEST", ClassSectionSource.class,
				AdaptManagedObjectWork.class.getName());
		source.addManagedObject(ClassManagedObjectSource.class, null,
				AdaptManagedObject.class).addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				AdaptManagedObject.class.getName());
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
	public static class AdaptManagedObject {

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
		public void useManagedObject(String fileLocation) throws IOException {
			// Write invoked task to file
			FileWriter writer = new FileWriter(fileLocation, false);
			writer.write("MANAGED OBJECT INVOKED");
			writer.flush();
			writer.close();
		}
	}

}