/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.command;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorContext;
import net.officefloor.building.util.OfficeBuildingTestUtil;
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

	@Override
	protected void setUp() throws Exception {

		// Ensure clean starting point for test
		OfficeBuildingTestUtil.cleanupClassPathTestArtifactInLocalRepository();

		// Create non-decorated context for testing
		this.context = this.createContext();
	}

	@Override
	protected void tearDown() throws Exception {

		// Clean up after test to keep local repository tidy
		OfficeBuildingTestUtil.cleanupClassPathTestArtifactInLocalRepository();

		// Ensure clean up of context
		if (this.context != null) {
			this.context.close();
		}
	}

	/**
	 * Ensure able to include a jar in class path.
	 */
	public void testIncludeJar() throws Exception {

		// Obtain path to jar
		final File JAR = getTestArtifactJar(true,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Include jar
		this.context.includeClassPathArtifact(JAR.getCanonicalPath());

		// Ensure jar on class path with no warnings
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR);
	}

	/**
	 * Ensure able to include a jar with dependencies in class path.
	 */
	public void testIncludeJarWithDependencies() throws Exception {

		// Obtain paths to jars
		final File JAR_WITH_DEPENDENCIES = getTestArtifactJar(true,
				OfficeBuildingTestUtil.TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID);
		final File JAR = getTestArtifactJar(false,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Include jar with dependencies
		this.context.includeClassPathArtifact(JAR_WITH_DEPENDENCIES
				.getCanonicalPath());

		// Ensure jars on class path with no warnings
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR_WITH_DEPENDENCIES, JAR);
	}

	/**
	 * Ensure able to include class path entry but does not include
	 * dependencies.
	 */
	public void testIncludeClassPathEntry() throws Exception {

		// Obtain path to jar
		final File JAR_WITH_DEPENDENCIES = getTestArtifactJar(true,
				OfficeBuildingTestUtil.TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID);

		// Include jar
		this.context.includeClassPathEntry(JAR_WITH_DEPENDENCIES
				.getCanonicalPath());

		// Ensure jar on class path but not its dependencies
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR_WITH_DEPENDENCIES);
	}

	/**
	 * Ensure can include a directory in class path.
	 */
	public void testIncludeDir() throws Exception {

		// Obtain path to directory
		final File DIR = this.findFile(this.getClass(), "DirArtifact/Test.txt")
				.getParentFile();

		// Include jar
		this.context.includeClassPathArtifact(DIR.getCanonicalPath());

		// Ensure directory on class path with no warnings
		assertNoWarnings(this.context);
		assertClassPath(this.context, DIR);
	}

	/**
	 * Ensure can include artifact in class path.
	 */
	public void testIncludeArtifact() throws Exception {

		// Obtain path to artifact
		final File JAR = getTestArtifactJar(false,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Include jar
		this.context.includeClassPathArtifact(
				OfficeBuildingTestUtil.TEST_GROUP_ID,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID,
				OfficeBuildingTestUtil.TEST_ARTIFACT_VERSION, null, null);

		// Ensure jar on class path with no warnings
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR);
	}

	/**
	 * Ensure able to include a artifact with dependencies in class path.
	 */
	public void testIncludeArtifactWithDependencies() throws Exception {

		// Obtain paths to jars
		final File JAR_WITH_DEPENDENCIES = getTestArtifactJar(false,
				OfficeBuildingTestUtil.TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID);
		final File JAR = getTestArtifactJar(false,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Include artifact
		this.context.includeClassPathArtifact(
				OfficeBuildingTestUtil.TEST_GROUP_ID,
				OfficeBuildingTestUtil.TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID,
				OfficeBuildingTestUtil.TEST_ARTIFACT_VERSION, null, null);

		// Ensure jars on class path with no warnings
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR_WITH_DEPENDENCIES, JAR);
	}

	/**
	 * Ensure can decorate with environment property.
	 */
	public void testEnvironmentPropertyDecoration() throws Exception {

		// Property details
		final String NAME = "name";
		final String VALUE = "value";

		// Obtain path to jar
		final File JAR = getTestArtifactJar(true,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Create the decorator
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect entry", JAR.getCanonicalPath(), context
						.getRawClassPathEntry());
				context.setEnvironmentProperty(NAME, VALUE);
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include jar for decoration
		this.context.includeClassPathArtifact(JAR.getCanonicalPath());

		// Ensure class path not changed (not decorated)
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR);
		assertCommandOptions(this.context);

		// Ensure property in command environment
		assertEnvironment(this.context, NAME, VALUE);
	}

	/**
	 * Ensure not override environment property for second decoration.
	 */
	public void testNotOverrideEnvironmentPropertyForSecondDecoration()
			throws Exception {

		// Property details
		final String NAME = "name";
		final String VALUE_ONE = "one";
		final String VALUE_TWO = "two";

		// Obtain path to jar
		final File JAR = getTestArtifactJar(true,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Create the decorators
		final OfficeFloorDecorator decoratorOne = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				context.setEnvironmentProperty(NAME, VALUE_ONE);
			}
		};
		final OfficeFloorDecorator decoratorTwo = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				context.setEnvironmentProperty(NAME, VALUE_TWO);
			}
		};

		// Create the context with the decorators
		this.context = this.createContext(decoratorOne, decoratorTwo);

		// Include jar for decoration
		this.context.includeClassPathArtifact(JAR.getCanonicalPath());

		// Ensure class path not changed (not decorated)
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR);
		assertCommandOptions(this.context);

		// Ensure property in command environment
		assertEnvironment(this.context, NAME, VALUE_ONE);
	}

	/**
	 * Ensure can decorate with command option.
	 */
	public void testCommandOptionDecoration() throws Exception {

		// Command option
		final String NAME = "name";
		final String VALUE = "value";

		// Obtain path to jar
		final File JAR = getTestArtifactJar(true,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Create the decorator
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect entry", JAR.getCanonicalPath(), context
						.getRawClassPathEntry());
				context.addCommandOption(NAME, VALUE);
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include jar for decoration
		this.context.includeClassPathArtifact(JAR.getCanonicalPath());

		// Ensure class path not changed (not decorated)
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR);
		assertEnvironment(this.context);

		// Ensure command option
		assertCommandOptions(this.context, NAME, VALUE);
	}

	/**
	 * Ensure can decorate with multiple command option values.
	 */
	public void testMultipleCommandOptionValuesDecoration() throws Exception {

		// Command option
		final String NAME = "name";
		final String VALUE_ONE = "one";
		final String VALUE_TWO = "two";
		final String VALUE_THREE = "three";
		final String VALUE_FOUR = "four";

		// Obtain path to jar
		final File JAR = getTestArtifactJar(true,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Create the decorator
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect entry", JAR.getCanonicalPath(), context
						.getRawClassPathEntry());
				context.addCommandOption(NAME, VALUE_ONE);
				context.addCommandOption(NAME, VALUE_TWO);
				context.addCommandOption(NAME, VALUE_THREE);
				context.addCommandOption(NAME, VALUE_FOUR);
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include jar for decoration
		this.context.includeClassPathArtifact(JAR.getCanonicalPath());

		// Ensure class path not changed (not decorated)
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR);
		assertEnvironment(this.context);

		// Ensure command option
		assertCommandOptions(this.context, NAME, VALUE_ONE + "," + VALUE_TWO
				+ "," + VALUE_THREE + "," + VALUE_FOUR);
	}

	/**
	 * Ensure {@link OfficeFloorDecorator} can override the class path entry.
	 */
	public void testDecoratorOverrideClassPathEntry() throws Exception {

		// Override class path entry
		final File CLASS_PATH_OVERRIDE = new File("/test/override.jar");

		// Obtain path to jar
		final File JAR = getTestArtifactJar(true,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Create the decorator
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect entry", JAR.getCanonicalPath(), context
						.getRawClassPathEntry());
				context.includeResolvedClassPathEntry(CLASS_PATH_OVERRIDE
						.getCanonicalPath());
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include jar for decoration
		this.context.includeClassPathArtifact(JAR.getCanonicalPath());

		// Ensure class path not changed (not decorated)
		assertNoWarnings(this.context);
		assertClassPath(this.context, CLASS_PATH_OVERRIDE);
		assertEnvironment(this.context);
		assertCommandOptions(this.context);
	}

	/**
	 * Ensure {@link OfficeFloorDecorator} can decorate all dependencies.
	 */
	public void testDecoratingAllDependencies() throws Exception {

		// Obtain paths to jars
		final File JAR_WITH_DEPENDENCIES = getTestArtifactJar(false,
				OfficeBuildingTestUtil.TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID);
		final File JAR = getTestArtifactJar(false,
				OfficeBuildingTestUtil.TEST_JAR_ARTIFACT_ID);

		// Create the decorator
		final List<String> rawClassPathEntries = new LinkedList<String>();
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				rawClassPathEntries.add(context.getRawClassPathEntry());
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include artifact with dependencies
		this.context.includeClassPathArtifact(
				OfficeBuildingTestUtil.TEST_GROUP_ID,
				OfficeBuildingTestUtil.TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID,
				OfficeBuildingTestUtil.TEST_ARTIFACT_VERSION, null, null);

		// Ensure class path not changed (not decorated)
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR_WITH_DEPENDENCIES, JAR);
		assertEnvironment(this.context);
		assertCommandOptions(this.context);

		// Ensure all dependencies able to be decorated
		assertEquals(
				"Incorrect number of raw class path entries for decoration", 2,
				rawClassPathEntries.size());
		assertEquals("Incorrect first raw class path entry",
				JAR_WITH_DEPENDENCIES.getCanonicalPath(), rawClassPathEntries
						.get(0));
		assertEquals("Incorrect second raw class path entry", JAR
				.getCanonicalPath(), rawClassPathEntries.get(1));
	}

	/**
	 * Creates the {@link OfficeFloorCommandContext} for testing.
	 * 
	 * @param decorators
	 *            {@link OfficeFloorDecorator} instances.
	 * @return {@link OfficeFloorCommandContext} for testing.
	 */
	private OfficeFloorCommandContextImpl createContext(
			OfficeFloorDecorator... decorators) throws Exception {
		return new OfficeFloorCommandContextImpl(null, OfficeBuildingTestUtil
				.getRemoteRepositoryUrls(), decorators);
	}

	/**
	 * Obtain the path to the test artifact jar.
	 * 
	 * @param isResolved
	 *            Indicates if the artifact is already resolved.
	 * @param artifactId
	 *            Test artifact id.
	 * @return Path to the test artifact jar.
	 */
	private static File getTestArtifactJar(boolean isResolved, String artifactId)
			throws Exception {

		// Use appropriate repository
		File repository = (isResolved ? OfficeBuildingTestUtil
				.getRemoteRepositoryDirectory() : OfficeBuildingTestUtil
				.getLocalRepositoryDirectory());

		// Return artifact file
		return OfficeBuildingTestUtil.getArtifactFile(repository,
				OfficeBuildingTestUtil.TEST_GROUP_ID, artifactId,
				OfficeBuildingTestUtil.TEST_ARTIFACT_VERSION, "jar");
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
	private static void assertClassPath(OfficeFloorCommandContextImpl context,
			File... expectedClassPathEntries) throws Exception {

		// Create the expected class path
		StringBuilder path = new StringBuilder();
		boolean isFirst = true;
		for (File expectedClassPathEntry : expectedClassPathEntries) {
			if (!isFirst) {
				path.append(File.pathSeparator);
			}
			isFirst = false;
			path.append(expectedClassPathEntry.getCanonicalPath());
		}
		String expectedClassPath = path.toString();

		// Obtain the actual class path
		String actualClassPath = context.getCommandClassPath();

		// Ensure correct class path
		assertEquals(expectedClassPath, actualClassPath);
	}

	/**
	 * Asserts the built environment is correct.
	 * 
	 * @param context
	 *            {@link OfficeFloorCommandContext}.
	 * @param nameValuePairs
	 *            Expected property name value pairs.
	 */
	private static void assertEnvironment(
			OfficeFloorCommandContextImpl context, String... nameValuePairs) {
		Properties environment = context.getCommandEnvironment();
		assertEquals("Incorrect number of properties",
				(nameValuePairs.length / 2), environment.size());
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = nameValuePairs[i];
			String value = nameValuePairs[i + 1];
			assertEquals("Incorrect property value for '" + name + "'", value,
					environment.getProperty(name));
		}
	}

	/**
	 * Asserts the command options are correct.
	 * 
	 * @param context
	 *            OfficeFloorCommandContext.
	 * @param nameValuesPairs
	 *            Expected command option and possibly multiple values
	 *            (separated by comma ',') pairs.
	 */
	private static void assertCommandOptions(
			OfficeFloorCommandContextImpl context, String... nameValuesPairs) {
		Map<String, List<String>> options = context.getCommandOptions();
		assertEquals("Incorrect number of options",
				(nameValuesPairs.length / 2), options.size());
		for (int i = 0; i < nameValuesPairs.length; i += 2) {
			String name = nameValuesPairs[i];
			String[] values = nameValuesPairs[i + 1].split(",");
			List<String> actual = options.get(name);
			assertNotNull("Expecting value for option '" + name + "'", actual);
			assertEquals("Incorrect number of values for option " + name,
					values.length, actual.size());
			for (int j = 0; j < values.length; j++) {
				assertEquals("Incorrect value for option " + name + " index "
						+ j, values[j], actual.get(j));
			}
		}
	}

}