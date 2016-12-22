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
package net.officefloor.building.manager;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Configuration for opening an {@link OfficeFloor} by the
 * {@link OfficeBuildingManagerMBean}.
 * 
 * @author Daniel Sagenschneider
 */
public final class OpenOfficeFloorConfiguration implements Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the {@link Process} to run the {@link OfficeFloor}.
	 */
	private String processName = null;

	/**
	 * {@link OfficeFloorSource} class name.
	 */
	private String officeFloorSourceClassName;

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private String officeFloorLocation;

	/**
	 * {@link Properties} for the {@link OfficeFloor}.
	 */
	private Properties officeFloorProperties = new Properties();

	/**
	 * Listing of {@link UploadArtifact} instances.
	 */
	private List<UploadArtifact> uploadArtifacts = new LinkedList<UploadArtifact>();

	/**
	 * Class path entries.
	 */
	private List<String> classPathEntries = new LinkedList<String>();

	/**
	 * JVM options for the {@link Process} running the {@link OfficeFloor}.
	 */
	private List<String> jvmOptions = new LinkedList<String>();

	/**
	 * Name of the {@link Office} containing the {@link Work} to run.
	 */
	private String officeName = null;

	/**
	 * Name of the {@link Work} to run.
	 */
	private String workName = null;

	/**
	 * Name of the {@link ManagedFunction} to run.
	 */
	private String taskName = null;

	/**
	 * Parameter for the {@link ManagedFunction} to run.
	 */
	private String parameter = null;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 */
	public OpenOfficeFloorConfiguration(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;
	}

	/**
	 * Obtains the name of the {@link Process}.
	 * 
	 * @return Name of the {@link Process}.
	 */
	public String getProcessName() {
		return this.processName;
	}

	/**
	 * Specifies the name of the {@link Process}.
	 * 
	 * @param processName
	 *            Name of the {@link Process}.
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	/**
	 * Adds an {@link UploadArtifact}.
	 * 
	 * @param uploadArtifact
	 *            {@link UploadArtifact}.
	 */
	public void addUploadArtifact(UploadArtifact uploadArtifact) {
		this.uploadArtifacts.add(uploadArtifact);
	}

	/**
	 * Obtains the listing of {@link UploadArtifact} instances.
	 * 
	 * @return Listing of {@link UploadArtifact} instances.
	 */
	public UploadArtifact[] getUploadArtifacts() {
		return this.uploadArtifacts.toArray(new UploadArtifact[this.uploadArtifacts.size()]);
	}

	/**
	 * <p>
	 * Adds a configured class path entry.
	 * <p>
	 * Please be aware that the {@link OfficeBuildingManager} may disallow
	 * opening {@link OfficeFloor} instances with configured class path entries.
	 * <p>
	 * This should never be used in Production. It is made available for the
	 * maven plug-ins to be able to run {@link OfficeFloor} instances from
	 * project configuration.
	 * 
	 * @param classPathEntry
	 *            Class path entry.
	 */
	public void addClassPathEntry(String classPathEntry) {
		this.classPathEntries.add(classPathEntry);
	}

	/**
	 * Obtains configured class path entries.
	 * 
	 * @return Class path entries.
	 */
	public String[] getClassPathEntries() {
		return this.classPathEntries.toArray(new String[this.classPathEntries.size()]);
	}

	/**
	 * Adds a JVM option for the {@link Process}.
	 * 
	 * @param jvmOption
	 *            JVM option for the {@link Process}.
	 */
	public void addJvmOption(String jvmOption) {
		this.jvmOptions.add(jvmOption);
	}

	/**
	 * Obtains the JVM options.
	 * 
	 * @return JVM options.
	 */
	public String[] getJvmOptions() {
		return this.jvmOptions.toArray(new String[this.jvmOptions.size()]);
	}

	/**
	 * Specifies the {@link OfficeFloorSource} class name.
	 * 
	 * @param officeFloorSourceClassName
	 *            {@link OfficeFloorSource} class name.
	 */
	public void setOfficeFloorSourceClassName(String officeFloorSourceClassName) {
		this.officeFloorSourceClassName = officeFloorSourceClassName;
	}

	/**
	 * Obtains the {@link OfficeFloorSource} class name.
	 * 
	 * @return {@link OfficeFloorSource} class name.
	 */
	public String getOfficeFloorSourceClassName() {
		return this.officeFloorSourceClassName;
	}

	/**
	 * Specifies the {@link OfficeFloor} location.
	 * 
	 * @param officeFloorLocation
	 *            {@link OfficeFloor} location.
	 */
	public void setOfficeFloorLocation(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;
	}

	/**
	 * Obtains the {@link OfficeFloor} location.
	 * 
	 * @return {@link OfficeFloor} location.
	 */
	public String getOfficeFloorLocation() {
		return this.officeFloorLocation;
	}

	/**
	 * Adds an {@link OfficeFloor} property.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public void addOfficeFloorProperty(String name, String value) {
		this.officeFloorProperties.setProperty(name, value);
	}

	/**
	 * Obtains the {@link OfficeFloor} properties.
	 * 
	 * @return {@link OfficeFloor} properties.
	 */
	public Properties getOfficeFloorProperties() {
		return this.officeFloorProperties;
	}

	/**
	 * Specifies the {@link ManagedFunction} to trigger on opening the {@link OfficeFloor}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office} containing the {@link Work}.
	 * @param workName
	 *            Name of {@link Work}.
	 * @param taskName
	 *            Name of {@link ManagedFunction}.
	 * @param parameter
	 *            Parameter to the {@link ManagedFunction}.
	 */
	public void setOpenTask(String officeName, String workName, String taskName, String parameter) {
		this.officeName = officeName;
		this.workName = workName;
		this.taskName = taskName;
		this.parameter = parameter;
	}

	/**
	 * Obtains the name of the {@link Office} containing the {@link Work} to be
	 * run.
	 * 
	 * @return Name of the {@link Office} containing the {@link Work} to be run.
	 */
	public String getOfficeName() {
		return this.officeName;
	}

	/**
	 * Obtains the name of the {@link Work} to be run.
	 * 
	 * @return Name of the {@link Work} to be run.
	 */
	public String getWorkName() {
		return this.workName;
	}

	/**
	 * Obtains the name of the {@link ManagedFunction} to be run.
	 * 
	 * @return Name of the {@link ManagedFunction} to be run.
	 */
	public String getTaskName() {
		return this.taskName;
	}

	/**
	 * Obtains the parameter for the {@link ManagedFunction} to be run.
	 * 
	 * @return Parameter for the {@link ManagedFunction} to be run.
	 */
	public String getParameter() {
		return this.parameter;
	}

}