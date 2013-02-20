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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandContext;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandFactory;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.process.ManagedProcess;

/**
 * Mock {@link OfficeFloorCommand} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockCommand implements OfficeFloorCommand,
		OfficeFloorCommandFactory {

	/**
	 * Initialises the environment.
	 */
	public static interface MockInitialiser {

		/**
		 * Initialises the environment.
		 * 
		 * @param context
		 *            {@link OfficeFloorCommandContext}.
		 * @throws Exception
		 *             If fails to initialise.
		 */
		void initialiseEnvironment(OfficeFloorCommandContext context)
				throws Exception;
	}

	/**
	 * {@link OfficeFloorCommand} name.
	 */
	private final String commandName;

	/**
	 * {@link ManagedProcess}.
	 */
	private final ManagedProcess managedProcess;

	/**
	 * {@link MockInitialiser}.
	 */
	private final MockInitialiser initialiser;

	/**
	 * {@link OfficeFloorCommandParameter} instances.
	 */
	private final List<OfficeFloorCommandParameter> parameters = new LinkedList<OfficeFloorCommandParameter>();

	/**
	 * {@link OfficeFloorCommandParameter} values.
	 */
	private final Map<String, String> parameterValues = new HashMap<String, String>();

	/**
	 * Expected environment name value pairs.
	 */
	private final Map<String, String> expectedEnvironment = new HashMap<String, String>();

	/**
	 * Expected parameter values.
	 */
	private final Map<String, String> expectedParameters = new HashMap<String, String>();

	/**
	 * Flag indicating if spawning.
	 */
	private boolean isSpawn = false;

	/**
	 * Listng of JVM options to add to environment.
	 */
	private final List<String> jvmOptions = new LinkedList<String>();

	/**
	 * Initiate.
	 * 
	 * @param commandName
	 *            {@link OfficeFloorCommand} name.
	 * @param managedProcess
	 *            {@link ManagedProcess}.
	 * @param initialiser
	 *            {@link EnvironmentInitialiser}. May be <code>null</code> to
	 *            not initialise.
	 * @param parameters
	 *            Name for the {@link OfficeFloorCommandParameter} instances.
	 */
	public MockCommand(String commandName, ManagedProcess managedProcess,
			MockInitialiser initialiser, String... parameters) {
		this.commandName = commandName;
		this.managedProcess = managedProcess;
		this.initialiser = initialiser;

		// Create the listing of parameters
		for (int i = 0; i < parameters.length; i++) {
			this.parameters.add(new MockCommandParameter(parameters[i]));
		}
	}

	/**
	 * Sets an {@link OfficeFloorCommandParameter} as flag (i.e. no value).
	 * 
	 * @param parameterName
	 *            Name of {@link OfficeFloorCommandParameter} to be a flag.
	 */
	public void setParameterAsFlag(String parameterName) {

		// Set parameter by name as flag
		for (OfficeFloorCommandParameter parameter : this.parameters) {
			if (parameter.getName().equals(parameterName)) {
				MockCommandParameter mockParameter = (MockCommandParameter) parameter;
				mockParameter.isRequireValue = false;
				return; // flagged as parameter
			}
		}

		// Must ensure parameter is a flag
		TestCase.fail("Unknown parameter '" + parameterName + "'");
	}

	/**
	 * Flags whether to spawn.
	 * 
	 * @param isSpawn
	 *            <code>true</code> to spawn.
	 */
	public void setSpawn(boolean isSpawn) {
		this.isSpawn = isSpawn;
	}

	/**
	 * Adds a JVM option to be loaded to the environment.
	 * 
	 * @param jvmOption
	 *            JVM option.
	 */
	public void addJvmOption(String jvmOption) {
		this.jvmOptions.add(jvmOption);
	}

	/**
	 * Obtains the {@link ManagedProcess}.
	 * 
	 * @return {@link ManagedProcess}.
	 */
	public ManagedProcess getManagedProcess() {
		return this.managedProcess;
	}

	/**
	 * Adds a {@link OfficeFloorCommandParameter}.
	 * 
	 * @param parameter
	 *            {@link OfficeFloorCommandParameter}.
	 */
	public void addParameter(OfficeFloorCommandParameter parameter) {
		this.parameters.add(parameter);
	}

	/**
	 * Adds an expected environment property.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public void addExpectedEnvironmentProperty(String name, String value) {
		this.expectedEnvironment.put(name, value);
	}

	/**
	 * Adds an expected {@link OfficeFloorCommandParameter} value.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public void addExepctedParameter(String name, String value) {
		this.expectedParameters.put(name, value);
	}

	/**
	 * Obtains the parameters values.
	 * 
	 * @return Parameter values.
	 */
	public Map<String, String> getParameterValues() {
		return this.parameterValues;
	}

	/*
	 * ================ OfficeFloorCommandFactory ===================
	 */

	@Override
	public String getCommandName() {
		return this.commandName;
	}

	@Override
	public OfficeFloorCommand createCommand() {
		return this;
	}

	/*
	 * ==================== OfficeFloorCommand ======================
	 */

	@Override
	public String getDescription() {
		return "command " + this.commandName.toUpperCase();
	}

	@Override
	public OfficeFloorCommandParameter[] getParameters() {
		return this.parameters
				.toArray(new OfficeFloorCommandParameter[this.parameters.size()]);
	}

	@Override
	public void initialiseEnvironment(OfficeFloorCommandContext context)
			throws Exception {

		// Initialise environment only if have initialiser
		if (this.initialiser != null) {
			this.initialiser.initialiseEnvironment(context);
		}
	}

	@Override
	public ManagedProcess createManagedProcess(
			OfficeFloorCommandEnvironment environment) throws Exception {

		// Provide process name and whether to spawn
		environment.setProcessName(this.commandName);
		environment.setSpawnProcess(this.isSpawn);

		// Provide the JVM option
		for (String jvmOption : this.jvmOptions) {
			environment.addJvmOption(jvmOption);
		}

		// Ensure correct environment
		for (String name : this.expectedEnvironment.keySet()) {
			String value = this.expectedEnvironment.get(name);
			TestCase.assertEquals("Incorrect environment property value for "
					+ name, value, environment.getProperty(name));
		}

		// Ensure correct property values
		for (String name : this.expectedParameters.keySet()) {
			String value = this.expectedParameters.get(name);
			TestCase.assertEquals("Incorrect parameter value for " + name,
					value, this.parameterValues.get(name));
		}

		// Return the managed process
		return this.managedProcess;
	}

	/**
	 * Mock {@link OfficeFloorCommandParameter}.
	 */
	private class MockCommandParameter implements OfficeFloorCommandParameter {

		/**
		 * Name of this {@link OfficeFloorCommandParameter}.
		 */
		private final String parameterName;

		/**
		 * Require value by default.
		 */
		public boolean isRequireValue = true;

		/**
		 * Initiate.
		 * 
		 * @param parameterName
		 *            Name of this {@link OfficeFloorCommandParameter}.
		 */
		public MockCommandParameter(String parameterName) {
			this.parameterName = parameterName;
		}

		/*
		 * ================= OfficeFloorCommandParameter ================
		 */

		@Override
		public String getName() {
			return this.parameterName;
		}

		@Override
		public String getShortName() {
			// Simplified for testing (take first character of name)
			return this.parameterName.substring(0, 1);
		}

		@Override
		public String getDescription() {
			return "parameter " + this.parameterName.toUpperCase();
		}

		@Override
		public boolean isRequireValue() {
			return this.isRequireValue;
		}

		@Override
		public void addValue(String value) {
			// Only maintain first value
			if (!MockCommand.this.parameterValues
					.containsKey(this.parameterName)) {
				MockCommand.this.parameterValues.put(this.parameterName, value);
			}
		}
	}

}