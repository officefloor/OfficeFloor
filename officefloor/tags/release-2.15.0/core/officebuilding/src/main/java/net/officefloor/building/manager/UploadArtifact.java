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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

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

	/**
	 * Convenience constructor to upload a {@link File} as the artifact.
	 * 
	 * @param file
	 *            {@link File} to upload as the artifact.
	 * @throws IOException
	 *             If fails to obtain the {@link File} for upload.
	 */
	public UploadArtifact(File file) throws IOException {

		// Use the file's name
		this.name = file.getName();

		// Load the file contents
		FileInputStream input = new FileInputStream(file);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int value = input.read(); value != -1; value = input.read()) {
			buffer.write(value);
		}
		input.close();

		// Reference contents for upload
		this.content = buffer.toByteArray();
	}

	/**
	 * Obtains the name of the artifact.
	 * 
	 * @return Name of the artifact.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the content of the artifact.
	 * 
	 * @return Content of the artifact.
	 */
	public byte[] getContent() {
		return this.content;
	}

}