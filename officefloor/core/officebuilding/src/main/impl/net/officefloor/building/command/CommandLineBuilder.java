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
package net.officefloor.building.command;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.building.command.parameters.ClassPathOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.JvmOptionOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorLocationOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeFloorSourceOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ParameterOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.ProcessNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PropertiesOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.TaskNameOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.UploadArtifactsOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.WorkNameOfficeFloorCommandParameter;
import net.officefloor.building.manager.UploadArtifact;
import net.officefloor.building.process.ManagedProcess;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Builds a command line.
 * 
 * @author Daniel Sagenschneider
 */
public class CommandLineBuilder {

	/**
	 * Prefix for an option of the {@link OfficeFloorCommandParameter}.
	 */
	private static final String OPTION_PREFIX = OfficeFloorCommandParserImpl.OPTION_PREFIX;

	/**
	 * Command line.
	 */
	private final List<String> commandLine = new LinkedList<String>();

	/**
	 * Adds an {@link UploadArtifact} to the command line.
	 * 
	 * @param artifactLocation
	 *            {@link UploadArtifact} location.
	 */
	public void addUploadArtifact(String artifactLocation) {
		this.addOption(
				UploadArtifactsOfficeFloorCommandParameter.PARAMETER_UPLOAD_ARTIFACT_LOCATION,
				artifactLocation);
	}

	/**
	 * Adds a class path entry to the command line.
	 * 
	 * @param classPathEntry
	 *            Class path entry.
	 */
	public void addClassPathEntry(String classPathEntry) {
		this.addOption(
				ClassPathOfficeFloorCommandParameter.PARAMETER_CLASS_PATH,
				classPathEntry);
	}

	/**
	 * Adds the {@link OfficeFloorSource} class name to the command line.
	 * 
	 * @param officeFloorSourceClassName
	 *            {@link OfficeFloorSource} class name.
	 */
	public void addOfficeFloorSource(String officeFloorSourceClassName) {
		this.addOption(
				OfficeFloorSourceOfficeFloorCommandParameter.PARAMETER_OFFICE_FLOOR_SOURCE,
				officeFloorSourceClassName);
	}

	/**
	 * Adds an {@link OfficeFloor} location to the command line.
	 * 
	 * @param officeFloorLocation
	 *            {@link OfficeFloor} location.
	 */
	public void addOfficeFloor(String officeFloorLocation) {
		this.addOption(
				OfficeFloorLocationOfficeFloorCommandParameter.PARAMETER_OFFICE_FLOOR_LOCATION,
				officeFloorLocation);
	}

	/**
	 * Adds the {@link Properties} for the {@link OfficeFloor} to the command
	 * line.
	 * 
	 * @param properties
	 *            {@link Properties} for the {@link OfficeFloor}.
	 */
	public void addOfficeFloorProperties(Properties properties) {
		for (String name : properties.stringPropertyNames()) {
			String value = properties.getProperty(name);
			this.addOption(
					PropertiesOfficeFloorCommandParameter.PARAMETER_PROPERTY,
					name + "=" + value);
		}
	}

	/**
	 * Adds the {@link ManagedProcess} name.
	 * 
	 * @param processName
	 *            {@link ManagedProcess} name.
	 */
	public void addProcessName(String processName) {
		this.addOption(
				ProcessNameOfficeFloorCommandParameter.PARAMTER_PROCESS_NAME,
				processName);
	}

	/**
	 * Adds a JVM option.
	 * 
	 * @param jvmOption
	 *            JVM option.
	 */
	public void addJvmOption(String jvmOption) {
		this.addOption(
				JvmOptionOfficeFloorCommandParameter.PARAMETER_JVM_OPTION,
				jvmOption);
	}

	/**
	 * Adds invoking a {@link Task}.
	 * 
	 * @param officeName
	 *            {@link Office} name.
	 * @param workName
	 *            {@link Work} name.
	 * @param taskName
	 *            {@link Task} name. May be <code>null</code> to use initial
	 *            {@link Task} of {@link Work}.
	 * @param parameterValue
	 *            Parameter value. May be <code>null</code>.
	 */
	public void addInvokeTask(String officeName, String workName,
			String taskName, String parameterValue) {
		this.addOption(
				OfficeNameOfficeFloorCommandParameter.PARAMETER_OFFICE_NAME,
				officeName);
		this.addOption(WorkNameOfficeFloorCommandParameter.PARAMETER_WORK_NAME,
				workName);
		this.addOption(TaskNameOfficeFloorCommandParameter.PARAMETER_TASK_NAME,
				taskName);
		this.addOption(
				ParameterOfficeFloorCommandParameter.PARAMETER_PARAMETER_VALUE,
				parameterValue);
	}

	/**
	 * Adds an {@link OfficeFloorCommandParameter} to the command line.
	 * 
	 * @param parameterName
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public void addOption(String parameterName, String value) {

		// Only include option if have value
		if ((value == null) || (value.trim().length() == 0)) {
			return; // no value, no option
		}

		// Include the option
		this.commandLine.add(OPTION_PREFIX + parameterName);
		this.commandLine.add(value);
	}

	/**
	 * Adds an {@link OfficeFloorCommand} to the command line.
	 * 
	 * @param commandName
	 *            Name of the command.
	 */
	public void addCommand(String commandName) {
		this.commandLine.add(commandName);
	}

	/**
	 * Obtains the built command line.
	 * 
	 * @return Built command line.
	 */
	public String[] getCommandLine() {
		return this.commandLine.toArray(new String[this.commandLine.size()]);
	}

}