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
package net.officefloor.maven.classpath;

import java.io.Serializable;

/**
 * Reference to an Artifact.
 * 
 * @author Daniel Sagenschneider
 */
public class ArtifactReference implements Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Group Id.
	 */
	private final String groupId;

	/**
	 * Artifact Id.
	 */
	private final String artifactId;

	/**
	 * Version.
	 */
	private final String version;

	/**
	 * Type.
	 */
	private final String type;

	/**
	 * Classifier.
	 */
	private final String classifier;

	/**
	 * Initiate.
	 * 
	 * @param groupId
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Version.
	 * @param type
	 *            Type.
	 * @param classifier
	 *            Classifier.
	 */
	public ArtifactReference(String groupId, String artifactId, String version,
			String type, String classifier) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.type = type;
		this.classifier = classifier;
	}

	/**
	 * Obtains the Artifact identifier.
	 * 
	 * @return Artifact identifier.
	 */
	public String getId() {
		return this.groupId + ":" + this.artifactId
				+ (this.type != null ? ":" + this.type : "")
				+ (this.classifier != null ? ":" + this.classifier : "") + ":"
				+ this.version;
	}

	/**
	 * Obtains the Group Id.
	 * 
	 * @return Group Id.
	 */
	public String getGroupId() {
		return this.groupId;
	}

	/**
	 * Obtains the Artifact Id.
	 * 
	 * @return Artifact Id.
	 */
	public String getArtifactId() {
		return this.artifactId;
	}

	/**
	 * Obtains the Version.
	 * 
	 * @return Version.
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * Obtains the Type.
	 * 
	 * @return Type.
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Obtains the Classifier.
	 * 
	 * @return Classifier.
	 */
	public String getClassifier() {
		return this.classifier;
	}

}