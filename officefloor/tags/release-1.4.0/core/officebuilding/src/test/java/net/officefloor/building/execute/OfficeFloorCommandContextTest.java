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

package net.officefloor.building.execute;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.building.classpath.ClassPathFactory;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorContext;
import net.officefloor.building.execute.OfficeFloorCommandContextImpl;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorCommandContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCommandContextTest extends OfficeFrameTestCase {

	/**
	 * {@link ClassPathFactory}.
	 */
	private final ClassPathFactory classPathFactory = this
			.createMock(ClassPathFactory.class);

	/**
	 * {@link OfficeFloorCommandContext} to test.
	 */
	private OfficeFloorCommandContextImpl context;

	@Override
	protected void setUp() throws Exception {

		// Create non-decorated context for testing
		this.context = this.createContext();
	}

	/**
	 * Ensure can include artifact in class path.
	 */
	public void testIncludeArtifact() throws Exception {

		final String PATH = "path.jar";
		this.recordReturn(this.classPathFactory, this.classPathFactory
				.createArtifactClassPath("group.id", "artifact", "1.0.0",
						"jar", "test"), new String[] { PATH });

		// Include jar
		this.replayMockObjects();
		this.context.includeClassPathArtifact("group.id", "artifact", "1.0.0",
				"jar", "test");
		this.verifyMockObjects();

		// Ensure jar on class path with no warnings
		assertNoWarnings(this.context);
		assertClassPath(this.context, PATH);
	}

	/**
	 * Ensure able to include an artifact location.
	 */
	public void testIncludeArtifactLocation() throws Exception {

		final String LOCATION = "test.jar";
		final String PATH = "path.jar";
		this.recordReturn(this.classPathFactory, this.classPathFactory
				.createArtifactClassPath(LOCATION), new String[] { PATH });

		// Include jar
		this.replayMockObjects();
		this.context.includeClassPathArtifact(LOCATION);
		this.verifyMockObjects();

		// Ensure jar on class path with no warnings
		assertNoWarnings(this.context);
		assertClassPath(this.context, PATH);
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
	 * Ensure can decorate with environment property.
	 */
	public void testEnvironmentPropertyDecoration() throws Exception {

		// Property details
		final String NAME = "name";
		final String VALUE = "value";

		// Obtain path to jar
		final String JAR = "test.jar";
		this.recordReturn(this.classPathFactory, this.classPathFactory
				.createArtifactClassPath(JAR), new String[] { JAR });

		// Create the decorator
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect entry", JAR, context
						.getRawClassPathEntry());
				context.setEnvironmentProperty(NAME, VALUE);
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include jar for decoration
		this.replayMockObjects();
		this.context.includeClassPathArtifact(JAR);
		this.verifyMockObjects();

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
		final String JAR = "test.jar";
		this.recordReturn(this.classPathFactory, this.classPathFactory
				.createArtifactClassPath(JAR), new String[] { JAR });

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
		this.replayMockObjects();
		this.context.includeClassPathArtifact(JAR);
		this.verifyMockObjects();

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
		final String JAR = "test.jar";
		this.recordReturn(this.classPathFactory, this.classPathFactory
				.createArtifactClassPath(JAR), new String[] { JAR });

		// Create the decorator
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect entry", JAR, context
						.getRawClassPathEntry());
				context.addCommandOption(NAME, VALUE);
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include jar for decoration
		this.replayMockObjects();
		this.context.includeClassPathArtifact(JAR);
		this.verifyMockObjects();

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
		final String JAR = "test.jar";
		this.recordReturn(this.classPathFactory, this.classPathFactory
				.createArtifactClassPath(JAR), new String[] { JAR });

		// Create the decorator
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect entry", JAR, context
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
		this.replayMockObjects();
		this.context.includeClassPathArtifact(JAR);
		this.verifyMockObjects();

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
		final String CLASS_PATH_OVERRIDE = "/test/override.jar";

		// Obtain path to jar
		final String JAR = "test.jar";
		this.recordReturn(this.classPathFactory, this.classPathFactory
				.createArtifactClassPath(JAR), new String[] { JAR });

		// Create the decorator
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect entry", JAR, context
						.getRawClassPathEntry());
				context.includeResolvedClassPathEntry(CLASS_PATH_OVERRIDE);
			}
		};

		// Create the context with the decorator
		this.context = this.createContext(decorator);

		// Include jar for decoration
		this.replayMockObjects();
		this.context.includeClassPathArtifact(JAR);
		this.verifyMockObjects();

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
		final String JAR_ONE = "one.jar";
		final String JAR_TWO = "two.jar";

		// Record creating two class path entries
		this.recordReturn(this.classPathFactory, this.classPathFactory
				.createArtifactClassPath("group.id", "artifact", "1.0.0",
						"jar", "test"), new String[] { JAR_ONE, JAR_TWO });

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
		this.replayMockObjects();
		this.context.includeClassPathArtifact("group.id", "artifact", "1.0.0",
				"jar", "test");
		this.verifyMockObjects();

		// Ensure class path not changed (not decorated)
		assertNoWarnings(this.context);
		assertClassPath(this.context, JAR_ONE, JAR_TWO);
		assertEnvironment(this.context);
		assertCommandOptions(this.context);

		// Ensure all dependencies able to be decorated
		assertEquals(
				"Incorrect number of raw class path entries for decoration", 2,
				rawClassPathEntries.size());
		assertEquals("Incorrect first raw class path entry", JAR_ONE,
				rawClassPathEntries.get(0));
		assertEquals("Incorrect second raw class path entry", JAR_TWO,
				rawClassPathEntries.get(1));
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
		return new OfficeFloorCommandContextImpl(this.classPathFactory,
				decorators);
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
			String... expectedClassPathEntries) throws Exception {

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