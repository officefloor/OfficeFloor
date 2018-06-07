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
package net.officefloor.building.execute;

import java.io.File;

import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorCommandContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCommandContextTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCommandContext} to test.
	 */
	private OfficeFloorCommandContextImpl context;

	/**
	 * Workspace for the {@link OfficeFloor}.
	 */
	private File workspace;

	@Override
	protected void setUp() throws Exception {

		// Obtain the workspace location
		this.workspace = new File(System.getProperty("java.io.tmpdir"));

		// Create non-decorated context for testing
		this.context = this.createContext();
	}

	/**
	 * Ensure able to include class path entry but does not include
	 * dependencies.
	 */
	public void testIncludeClassPathEntry() throws Exception {

		// Obtain path to jar
		final String ENTRY_PATH = "test.jar";

		// Include jar
		this.context.includeClassPathEntry(ENTRY_PATH);

		// Ensure jar on class path but not its dependencies
		assertNoWarnings(this.context);
		assertClassPath(this.context, ENTRY_PATH);
	}

	/**
	 * Ensure {@link OfficeFloorDecorator} can override the class path entry.
	 */
	public void testDecoratorOverrideClassPathEntry() throws Exception {

		// Override class path entry
		final String CLASS_PATH_OVERRIDE = "/test/override.jar";

		// Path to jar
		final String JAR = "test.jar";

		// Create the decorator
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context) throws Exception {
				assertEquals("Incorrect entry", JAR, context.getRawClassPathEntry());
				context.includeResolvedClassPathEntry(CLASS_PATH_OVERRIDE);
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include jar for decoration
		this.replayMockObjects();
		this.context.includeClassPathEntry(JAR);
		this.verifyMockObjects();

		// Ensure class path not changed (not decorated)
		assertNoWarnings(this.context);
		assertClassPath(this.context, CLASS_PATH_OVERRIDE);
	}

	/**
	 * Ensure able to create a {@link File} by the {@link OfficeFloorDecorator}.
	 */
	public void testDecoratingCreateFile() throws Exception {

		final String FILE_PATH = "test.war";

		// Create the decorator
		final File[] file = new File[1];
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context) throws Exception {
				// Create the file
				file[0] = context.createWorkspaceFile("decorate", "test");
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include artifact with dependencies
		this.replayMockObjects();
		this.context.includeClassPathEntry(FILE_PATH);
		this.verifyMockObjects();

		// Ensure no class path warnings
		assertEquals("Should be no class path warnings", 0, this.context.getWarnings().length);

		// Ensure file created in the workspace
		assertTrue("File should be created", file[0].exists());
		assertEquals("File should be in workspace", this.workspace, file[0].getParentFile());
	}

	/**
	 * Creates the {@link OfficeFloorCommandContext} for testing.
	 * 
	 * @param decorators
	 *            {@link OfficeFloorDecorator} instances.
	 * @return {@link OfficeFloorCommandContext} for testing.
	 */
	private OfficeFloorCommandContextImpl createContext(OfficeFloorDecorator... decorators) throws Exception {
		return new OfficeFloorCommandContextImpl(this.workspace, decorators);
	}

	/**
	 * Ensure no issues for {@link OfficeFloorCommandContext}.
	 * 
	 * @param context
	 *            {@link OfficeFloorCommandContext}.
	 */
	private static void assertNoWarnings(OfficeFloorCommandContextImpl context) {
		String[] warnings = context.getWarnings();
		if (warnings.length > 0) {
			StringBuilder message = new StringBuilder();
			for (String warning : warnings) {
				message.append(warning + "\n");
			}
			fail(message.toString());
		}
	}

	/**
	 * Asserts the built class path is correct.
	 * 
	 * @param context
	 *            {@link OfficeFloorCommandContext} to validate its class path.
	 * @param expectedClassPathEntries
	 *            Expected class path entries.
	 */
	private static void assertClassPath(OfficeFloorCommandContextImpl context, String... expectedClassPathEntries)
			throws Exception {

		// Create the expected class path
		StringBuilder path = new StringBuilder();
		boolean isFirst = true;
		for (String expectedClassPathEntry : expectedClassPathEntries) {
			if (!isFirst) {
				path.append(File.pathSeparator);
			}
			isFirst = false;
			path.append(expectedClassPathEntry);
		}
		String expectedClassPath = path.toString();

		// Obtain the actual class path
		String actualClassPath = context.getCommandClassPath();

		// Ensure correct class path
		assertEquals(expectedClassPath, actualClassPath);
	}

}