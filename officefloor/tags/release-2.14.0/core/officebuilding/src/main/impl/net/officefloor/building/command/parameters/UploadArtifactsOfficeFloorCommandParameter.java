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
package net.officefloor.building.command.parameters;

import java.io.File;
import java.io.IOException;

import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.building.manager.UploadArtifact;

/**
 * {@link OfficeFloorCommandParameter} for a {@link UploadArtifact} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class UploadArtifactsOfficeFloorCommandParameter extends
		AbstractMultiplePathsOfficeFloorCommandParameter {

	/**
	 * Parameter name for the upload artifact location.
	 */
	public static final String PARAMETER_UPLOAD_ARTIFACT_LOCATION = "upload_artifact";

	/**
	 * Initiate.
	 */
	public UploadArtifactsOfficeFloorCommandParameter() {
		super(PARAMETER_UPLOAD_ARTIFACT_LOCATION, null, File.pathSeparator,
				"Artifact to be uploaded for inclusion on the class path");
	}

	/**
	 * Obtains the {@link UploadArtifact} instances.
	 * 
	 * @return {@link UploadArtifact} instances.
	 * @throws IOException
	 *             If fails to obtain the listing of {@link UploadArtifact}
	 *             instances.
	 */
	public UploadArtifact[] getUploadArtifacts() throws IOException {

		// Obtain the paths
		String[] paths = this.getPaths();

		// Obtain the listing of upload artifacts
		UploadArtifact[] uploadArtifacts = new UploadArtifact[paths.length];
		for (int i = 0; i < uploadArtifacts.length; i++) {
			String path = paths[i];
			uploadArtifacts[i] = new UploadArtifact(new File(path));
		}

		// Return the listing of upload artifacts
		return uploadArtifacts;
	}

}