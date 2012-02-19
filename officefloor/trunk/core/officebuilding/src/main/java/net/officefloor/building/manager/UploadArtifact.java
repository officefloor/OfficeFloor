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

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Artifact that is uploaded to the {@link OfficeBuildingManagerMBean} and made
 * available on the {@link OfficeFloor} class path.
 * 
 * @author Daniel Sagenschneider
 */
public final class UploadArtifact implements Serializable {

	/**
	 * Name of the artifact.
	 */
	private final String name;

	/**
	 * Content of the artifact to upload.
	 */
	private final byte[] content;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of the artifact.
	 * @param content
	 *            Content of the artifact to upload.
	 */
	public UploadArtifact(String name, byte[] content) {
		this.name = name;
		this.content = content;
	}

}