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
package net.officefloor.building.command.parameters;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.building.command.OfficeFloorCommandParameter;

import org.apache.maven.artifact.Artifact;

/**
 * {@link OfficeFloorCommandParameter} for multiple {@link Artifact} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MultipleArtifactsOfficeFloorCommandParameter extends
		AbstractOfficeFloorCommandParameter {

	/**
	 * Obtains the command line argument value for an {@link ArtifactArgument}.
	 * 
	 * @param groupId
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Version.
	 * @param type
	 *            Type. May be <code>null</code>.
	 * @param classifier
	 *            Classifier. May be <code>null</code>.
	 * @return Command line argument value for an {@link ArtifactArgument}.
	 */
	public static String getArtifactArgumentValue(String groupId,
			String artifactId, String version, String type, String classifier) {
		StringBuilder value = new StringBuilder();
		value.append(groupId);
		value.append(":");
		value.append(artifactId);
		if (type != null) {
			value.append(":");
			value.append(type);
		}
		if (classifier != null) {
			value.append(":");
			value.append(classifier);
		}
		value.append(":");
		value.append(version);
		return value.toString();
	}

	/**
	 * Listing of the {@link ArtifactArgument} instances.
	 */
	private final List<ArtifactArgument> artifacts = new LinkedList<ArtifactArgument>();

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param shortName
	 *            Short name.
	 */
	public MultipleArtifactsOfficeFloorCommandParameter(String name,
			String shortName) {
		super(name, shortName);
	}

	/**
	 * Obtains the {@link ArtifactArgument} instances.
	 * 
	 * @return {@link ArtifactArgument} instances.
	 */
	public ArtifactArgument[] getArtifacts() {
		return this.artifacts.toArray(new ArtifactArgument[this.artifacts
				.size()]);
	}

	/**
	 * Obtains the indexed value.
	 * 
	 * @param values
	 *            Values.
	 * @param index
	 *            Index of value to obtain.
	 * @return Indexed value or <code>null</code> if no value at index.
	 */
	private String getIndexedValue(String[] values, int index) {
		return (values.length > index ? values[index] : null);
	}

	/*
	 * ==================== OfficeFloorCommandParameter =================
	 */

	@Override
	public void addValue(String value) {

		// Parse out the details of the artifact
		String[] parts = value.split(":");
		String groupId = this.getIndexedValue(parts, 0);
		String artifactId = this.getIndexedValue(parts, 1);
		String type = this.getIndexedValue(parts, 2);
		String classifier = this.getIndexedValue(parts, 3);
		String version = this.getIndexedValue(parts, 4);

		// Handle the optional parameters
		// groupId:artifactId[:type][:classifier]:version
		if (parts.length == 3) {
			// No type nor classifier
			version = type;
			type = null;
		} else if (parts.length == 4) {
			// No classifier
			version = classifier;
			classifier = null;
		}

		// Register the artifact
		this.artifacts.add(new ArtifactArgument(groupId, artifactId, version,
				type, classifier));
	}

}