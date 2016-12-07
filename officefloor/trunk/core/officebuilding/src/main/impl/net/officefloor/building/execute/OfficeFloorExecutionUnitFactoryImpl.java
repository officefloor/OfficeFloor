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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessConfiguration;

/**
 * {@link OfficeFloorExecutionUnitFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExecutionUnitFactoryImpl implements OfficeFloorExecutionUnitFactory {

	/**
	 * Environment {@link Properties}.
	 */
	private final Properties environment;

	/**
	 * {@link OfficeFloorDecorator} instances.
	 */
	private final OfficeFloorDecorator[] decorators;

	/**
	 * Initiate.
	 * 
	 * @param environment
	 *            Environment {@link Properties}.
	 * @param decorators
	 *            {@link OfficeFloorDecorator} instances.
	 */
	public OfficeFloorExecutionUnitFactoryImpl(Properties environment, OfficeFloorDecorator[] decorators) {
		this.environment = environment;
		this.decorators = decorators;
	}

	/*
	 * ================= OfficeFloorExecutionUnitFactory ==================
	 */

	@Override
	public OfficeFloorExecutionUnit createExecutionUnit(OfficeFloorCommand command)
			throws OfficeFloorExecutionUnitCreateException {

		// TODO obtain OfficeFloor workspace
		File workspace = null;

		// Create the command context
		OfficeFloorCommandContextImpl context = new OfficeFloorCommandContextImpl(workspace, this.decorators);

		// Initialise the environment
		try {
			command.initialiseEnvironment(context);
		} catch (ClassPathError ex) {
			throw new OfficeFloorExecutionUnitCreateException("Failed to initialise command class path", ex);
		} catch (Exception ex) {
			throw new OfficeFloorExecutionUnitCreateException("Failed to initialise environment", ex);
		}

		// Ensure no warnings
		String[] warnings = context.getWarnings();
		if (warnings.length > 0) {
			StringWriter warningText = new StringWriter();
			PrintWriter writer = new PrintWriter(warningText);
			for (String warning : warnings) {
				writer.println(warning);
			}
			throw new OfficeFloorExecutionUnitCreateException(warningText.toString());
		}

		// Create copy of the environment
		Properties env = new Properties();
		env.putAll(this.environment);

		// Load environment properties onto parameters
		for (OfficeFloorCommandParameter parameter : command.getParameters()) {
			String parameterName = parameter.getName();

			// Add the environment property
			String value = env.getProperty(parameterName);
			if (value != null) {
				parameter.addValue(value);
			}
		}

		// Create the managed object process
		ManagedProcess managedProcess;
		CommandEnvironment commandEnvironment = new CommandEnvironment(env);
		try {
			managedProcess = command.createManagedProcess(commandEnvironment);
		} catch (Exception ex) {
			throw new OfficeFloorExecutionUnitCreateException("Failed to create Managed Process", ex);
		}

		// Create the process configuration
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setProcessName(commandEnvironment.processName);
		String additionalClassPath = context.getCommandClassPath();
		if ((additionalClassPath != null) && (additionalClassPath.trim().length() > 0)) {
			configuration.setAdditionalClassPath(additionalClassPath);
		}
		for (String jvmOption : commandEnvironment.jvmOptions) {
			configuration.addJvmOption(jvmOption);
		}

		// Create and return the execution unit
		return new OfficeFloorExecutionUnitImpl(managedProcess, configuration, commandEnvironment.isSpawnProcess);
	}

	/**
	 * {@link OfficeFloorCommandEnvironment}.
	 */
	private static class CommandEnvironment implements OfficeFloorCommandEnvironment {

		/**
		 * {@link Process} name.
		 */
		public String processName = null;

		/**
		 * Flag to spawn a {@link Process}.
		 */
		public boolean isSpawnProcess = false;

		/**
		 * JVM options.
		 */
		public final List<String> jvmOptions = new LinkedList<String>();

		/**
		 * Environment properties.
		 */
		private final Properties properties;

		/**
		 * Initiate.
		 * 
		 * @param properties
		 *            Environment properties.
		 */
		public CommandEnvironment(Properties properties) {
			this.properties = properties;
		}

		/*
		 * ================ OfficeFloorCommandEnvironment ===============
		 */

		@Override
		public String getProperty(String name) {
			return this.properties.getProperty(name);
		}

		@Override
		public void setProcessName(String processName) {
			this.processName = processName;
		}

		@Override
		public void setSpawnProcess(boolean isSpawn) {
			this.isSpawnProcess = isSpawn;
		}

		@Override
		public void addJvmOption(String jvmOption) {
			this.jvmOptions.add(jvmOption);
		}
	}

}