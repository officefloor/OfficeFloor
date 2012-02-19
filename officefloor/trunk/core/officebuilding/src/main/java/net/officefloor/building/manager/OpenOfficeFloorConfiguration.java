/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.util.List;
import java.util.Properties;

import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.frame.api.execute.Task;
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
	private List<UploadArtifact> uploadArtifacts;

	/**
	 * Listing of {@link ArtifactReference} instances.
	 */
	private List<ArtifactReference> artifactReferences;

	/**
	 * Name of the {@link Office} containing the {@link Work} to run.
	 */
	private String officeName;

	/**
	 * Name of the {@link Work} to run.
	 */
	private String workName;

	/**
	 * Name of the {@link Task} to run.
	 */
	private String taskName;

	/**
	 * Parameter for the {@link Task} to run.
	 */
	private Object parameter;

}