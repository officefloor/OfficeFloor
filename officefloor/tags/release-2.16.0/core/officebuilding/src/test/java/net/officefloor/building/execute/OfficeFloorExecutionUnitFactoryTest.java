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
import java.util.Properties;

import net.officefloor.building.classpath.ClassPathFactory;
import net.officefloor.building.classpath.ClassPathFactoryImpl;
import net.officefloor.building.classpath.RemoteRepository;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorContext;
import net.officefloor.building.execute.MockCommand.MockInitialiser;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessConfiguration;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeFloorExecutionUnit}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExecutionUnitFactoryTest extends OfficeFrameTestCase {

	/**
	 * Environment {@link Properties}.
	 */
	private final Properties environment = new Properties();

	/**
	 * Ensure can create an {@link OfficeFloorExecutionUnit} for a simple
	 * command.
	 */
	public void testSimpleCommand() throws Exception {
		MockCommand command = this.createCommand("test", null);
		this.doTest(command, null, false);
	}

	/**
	 * Ensure {@link OfficeFloorDecorator} alter the class path.
	 */
	public void testDecorateClassPathEntry() throws Exception {

		final String RAW_CLASS_PATH_ENTRY = "raw_entry";
		final String RESOLVED_CLASS_PATH_ENTRY = "resolved_entry";

		// Create the command expecting environment property
		MockCommand command = this.createCommand("test", new MockInitialiser() {
			@Override
			public void initialiseEnvironment(OfficeFloorCommandContext context)
					throws Exception {
				context.includeClassPathEntry(RAW_CLASS_PATH_ENTRY);
			}
		});

		// Decorate the environment property
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect raw class path", RAW_CLASS_PATH_ENTRY,
						context.getRawClassPathEntry());
				context.includeResolvedClassPathEntry(RESOLVED_CLASS_PATH_ENTRY);
			}
		};

		// Test
		this.doTest(command, RESOLVED_CLASS_PATH_ENTRY, false, decorator);
	}

	/**
	 * Ensure can configure {@link OfficeFloorCommandParameter} from environment
	 * property.
	 */
	public void testConfigureParameterFromEnvironment() throws Exception {

		final String PARAMETER_NAME = "env-name";
		final String PARAMETER_VALUE = "env-value";

		// Provide property in environment
		this.environment.put(PARAMETER_NAME, PARAMETER_VALUE);

		// Create command with parameter
		MockCommand command = this.createCommand("test", null, PARAMETER_NAME);

		// Test
		this.doTest(command, null, false);

		// Ensure parameter loaded onto command parameter
		assertEquals("Parameter value not loaded from environment",
				PARAMETER_VALUE,
				command.getParameterValues().get(PARAMETER_NAME));
	}

	/**
	 * Ensure can configure the {@link ProcessConfiguration}.
	 */
	public void testConfigureProcessEnvironment() throws Exception {

		// Create command and expected to spawn
		MockCommand command = this.createCommand("test", null);
		command.setSpawn(true);
		command.addJvmOption("-Done=a");
		command.addJvmOption("-Dtwo=b");

		// Test
		OfficeFloorExecutionUnit executionUnit = this.doTest(command, null,
				true);

		// Ensure will spawn
		assertTrue("Should spawn process", executionUnit.isSpawnProcess());

		// Ensure correct configuration
		ProcessConfiguration configuration = executionUnit
				.getProcessConfiguration();
		assertEquals("Incorrect process name", "test",
				configuration.getProcessName());
		String[] jvmOptions = configuration.getJvmOptions();
		assertEquals("Incorrect number of JVM options", 2, jvmOptions.length);
		assertEquals("Incorrect first JVM option", "-Done=a", jvmOptions[0]);
		assertEquals("Incorrect second JVM option", "-Dtwo=b", jvmOptions[1]);
	}

	/**
	 * Creates the {@link OfficeFloorExecutionUnit} and validates it.
	 * 
	 * @param command
	 *            {@link MockCommand}.
	 * @param additionalClassPath
	 *            Additional class path.
	 * @param isSpawnProcess
	 *            Indicating if spawning process.
	 * @param decorators
	 *            {@link OfficeFloorDecorator} instances.
	 * @return {@link OfficeFloorExecutionUnit}.
	 */
	private OfficeFloorExecutionUnit doTest(MockCommand command,
			String additionalClassPath, boolean isSpawnProcess,
			OfficeFloorDecorator... decorators) throws Exception {

		// Obtain execution factory details
		File localRepositoryDirectory = OfficeBuildingTestUtil
				.getTestLocalRepository();
		String remoteRepositoryUrl = "file://"
				+ OfficeBuildingTestUtil.getUserLocalRepository()
						.getAbsolutePath();
		ClassPathFactory classPathFactory = new ClassPathFactoryImpl(
				localRepositoryDirectory,
				new RemoteRepository[] { new RemoteRepository(
						remoteRepositoryUrl) });

		// Test
		this.replayMockObjects();

		// Create the factory
		final OfficeFloorExecutionUnitFactory factory = new OfficeFloorExecutionUnitFactoryImpl(
				classPathFactory, this.environment, decorators);

		// Create the execution units
		OfficeFloorExecutionUnit executionUnit = factory
				.createExecutionUnit(command);

		// Verify functionality
		this.verifyMockObjects();

		// Validate the execution unit
		assertExecutionUnit(executionUnit, command, additionalClassPath,
				isSpawnProcess);

		// Return the execution units for further validation
		return executionUnit;
	}

	/**
	 * Creates a {@link MockCommand} for testing.
	 * 
	 * @param commandName
	 *            Command name.
	 * @param initialiser
	 *            {@link MockInitialiser}. May be <code>null</code>.
	 * @param parameters
	 *            Names of the {@link OfficeFloorCommandParameter} instances for
	 *            the {@link MockCommand}.
	 * @return {@link MockCommand}.
	 */
	private MockCommand createCommand(String commandName,
			MockInitialiser initialiser, String... parameters) {
		ManagedProcess managedProcess = this.createMock(ManagedProcess.class);
		return new MockCommand(commandName, managedProcess, initialiser,
				parameters);
	}

	/**
	 * Validates the {@link OfficeFloorExecutionUnit}.
	 * 
	 * @param executionUnit
	 *            {@link OfficeFloorExecutionUnit} to validate.
	 * @param command
	 *            Corresponding {@link MockCommand}.
	 * @param additionalClassPath
	 *            Additional class path.
	 * @param isSpawnProcess
	 *            Spawn process flag.
	 */
	private static void assertExecutionUnit(
			OfficeFloorExecutionUnit executionUnit, MockCommand command,
			String additionalClassPath, boolean isSpawnProcess) {
		assertEquals("Incorrect managed process",
				executionUnit.getManagedProcess(), command.getManagedProcess());
		ProcessConfiguration configuration = executionUnit
				.getProcessConfiguration();
		assertEquals("Incorrect process name", command.getCommandName(),
				configuration.getProcessName());
		assertEquals("Incorrect additional class path", additionalClassPath,
				configuration.getAdditionalClassPath());
		assertEquals("Incorrectly spawning process", isSpawnProcess,
				executionUnit.isSpawnProcess());
	}

}