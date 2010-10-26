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
package net.officefloor.building.execute;

import java.io.File;

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
	 * Ensure can create an {@link OfficeFloorExecutionUnit} for a simple
	 * command.
	 */
	public void testSimpleCommand() throws Exception {
		MockCommand command = this.createCommand("test", null);
		this.doTest(command, null, false);
	}

	/**
	 * Ensure {@link OfficeFloorDecorator} can specify environment value.
	 */
	public void testDecorateClassPathEntry() throws Exception {

		final String CLASS_PATH_ENTRY = "entry";
		final String NAME = "name";
		final String VALUE = "value";

		// Create the command expecting environment property
		MockCommand command = this.createCommand("test", new MockInitialiser() {
			@Override
			public void initialiseEnvironment(OfficeFloorCommandContext context)
					throws Exception {
				context.includeClassPathEntry(CLASS_PATH_ENTRY);
			}
		});
		command.addExpectedEnvironmentProperty(NAME, VALUE);

		// Decorate the environment property
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect class path", CLASS_PATH_ENTRY, context
						.getRawClassPathEntry());
				context.setEnvironmentProperty(NAME, VALUE);
			}
		};

		// Test
		this.doTest(command, CLASS_PATH_ENTRY, false, decorator);
	}

	/**
	 * Ensure {@link OfficeFloorDecorator} can specify environment value.
	 */
	public void testDecorateEnvironment() throws Exception {

		final String CLASS_PATH_ENTRY = "entry";
		final String NAME = "name";
		final String VALUE = "value";

		// Create the command expecting environment property
		MockCommand command = this.createCommand("test", new MockInitialiser() {
			@Override
			public void initialiseEnvironment(OfficeFloorCommandContext context)
					throws Exception {
				context.includeClassPathArtifact(CLASS_PATH_ENTRY);
			}
		});
		command.addExpectedEnvironmentProperty(NAME, VALUE);

		// Decorate the environment property
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect class path", CLASS_PATH_ENTRY, context
						.getRawClassPathEntry());
				context.setEnvironmentProperty(NAME, VALUE);
			}
		};

		// Test
		this.doTest(command, CLASS_PATH_ENTRY, false, decorator);
	}

	/**
	 * Ensure {@link OfficeFloorDecorator} can specify environment value that is
	 * also mapped onto an {@link OfficeFloorCommandParameter}.
	 */
	public void testDecorateCommandParameter() throws Exception {
		final String CLASS_PATH_ENTRY = "entry";
		final String NAME = "name";
		final String VALUE = "value";

		// Create the command expecting environment property
		MockCommand command = this.createCommand("test", new MockInitialiser() {
			@Override
			public void initialiseEnvironment(OfficeFloorCommandContext context)
					throws Exception {
				context.includeClassPathArtifact(CLASS_PATH_ENTRY);
			}
		}, NAME);
		command.addExpectedEnvironmentProperty(NAME, VALUE);
		command.addExepctedParameter(NAME, VALUE);

		// Decorate the environment property
		final OfficeFloorDecorator decorator = new OfficeFloorDecorator() {
			@Override
			public void decorate(OfficeFloorDecoratorContext context)
					throws Exception {
				assertEquals("Incorrect class path", CLASS_PATH_ENTRY, context
						.getRawClassPathEntry());
				context.setEnvironmentProperty(NAME, VALUE);
			}
		};

		// Test
		this.doTest(command, CLASS_PATH_ENTRY, false, decorator);
	}

	/**
	 * Ensure can configure the {@link ProcessConfiguration}.
	 */
	public void testConfigureProcessEnvironment() throws Exception {

		// Create command and expected to spawn
		MockCommand command = this.createCommand("test", null);
		command.setSpawn(true);

		// Test
		this.doTest(command, null, true);
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
				.getLocalRepositoryDirectory();
		String[] remoteRepositoryUrls = OfficeBuildingTestUtil
				.getRemoteRepositoryUrls();

		// Test
		this.replayMockObjects();

		// Create the factory
		final OfficeFloorExecutionUnitFactory factory = new OfficeFloorExecutionUnitFactoryImpl(
				localRepositoryDirectory, remoteRepositoryUrls, decorators);

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
		assertEquals("Incorrect managed process", executionUnit
				.getManagedProcess(), command.getManagedProcess());
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