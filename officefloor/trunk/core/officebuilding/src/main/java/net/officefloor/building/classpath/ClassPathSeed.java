/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.building.classpath;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Seed for a class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathSeed implements Serializable {

	/**
	 * {@link Inclusion} instances.
	 */
	private final List<Inclusion> inclusions = new ArrayList<Inclusion>();

	/**
	 * Includes Artifact in seeding.
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
	 *            Classifier. May be <code>null</code>.
	 */
	public void includeArtifact(String groupId, String artifactId,
			String version, String type, String classifier) {
		this.inclusions.add(new ArtifactInclusion(groupId, artifactId, version,
				type, classifier));
	}

	/**
	 * Includes the Jar file and its dependencies..
	 * 
	 * @param jarFile
	 *            Jar file.
	 */
	public void includeJar(File jarFile) {
		this.includeJar(jarFile, true);
	}

	/**
	 * Includes the Jar file.
	 * 
	 * @param jarFile
	 *            Jar file.
	 * @param isIncludeDependencies
	 *            Flag to include dependencies for the Jar.
	 */
	public void includeJar(File jarFile, boolean isIncludeDependencies) {
		this.inclusions.add(new JarInclusion(jarFile, isIncludeDependencies));
	}

	/**
	 * Includes the directory.
	 * 
	 * @param directory
	 *            Directory.
	 */
	public void includeDirectory(File directory) {
		this.inclusions.add(new DirectoryInclusion(directory));
	}

	/**
	 * Includes the seed into the {@link ClassPathBuilder}.
	 * 
	 * @param builder
	 *            {@link ClassPathBuilder}.
	 * @throws Exception
	 *             If fails to include.
	 */
	void include(ClassPathBuilder builder) throws Exception {
		for (Inclusion inclusion : this.inclusions) {
			inclusion.include(builder);
		}
	}

	/**
	 * Interface to include.
	 */
	private static interface Inclusion extends Serializable {

		/**
		 * Includes into the {@link ClassPathBuilder}.
		 * 
		 * @param builder
		 *            {@link ClassPathBuilder}.
		 * @throws Exception
		 *             If fails to include.
		 */
		void include(ClassPathBuilder builder) throws Exception;
	}

	/**
	 * Artifact {@link Inclusion}.
	 */
	private static class ArtifactInclusion implements Inclusion {

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
		public ArtifactInclusion(String groupId, String artifactId,
				String version, String type, String classifier) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
			this.type = type;
			this.classifier = classifier;
		}

		/*
		 * ===================== Inclusion ===========================
		 */

		@Override
		public void include(ClassPathBuilder builder) throws Exception {
			builder.includeArtifact(this.groupId, this.artifactId,
					this.version, this.type, this.classifier);
		}
	}

	/**
	 * Jar {@link Inclusion}.
	 */
	private static class JarInclusion implements Inclusion {

		/**
		 * Jar location.
		 */
		private final String jarLocation;

		/**
		 * Flag to include dependencies for the Jar.
		 */
		private final boolean isIncludeDependencies;

		/**
		 * Initiate.
		 * 
		 * @param jarFile
		 *            Jar file.
		 * @param isIncludeDependencies
		 *            Flag to include dependencies for the Jar.
		 */
		public JarInclusion(File jarFile, boolean isIncludeDependencies) {
			this.jarLocation = jarFile.getPath();
			this.isIncludeDependencies = isIncludeDependencies;
		}

		/*
		 * ===================== Inclusion ===========================
		 */

		@Override
		public void include(ClassPathBuilder builder) throws Exception {
			builder.includeJar(new File(this.jarLocation),
					this.isIncludeDependencies);
		}
	}

	/**
	 * Directory {@link Inclusion}.
	 */
	private static class DirectoryInclusion implements Inclusion {

		/**
		 * Directory location.
		 */
		private final String directoryLocation;

		/**
		 * Initiate.
		 * 
		 * @param directory
		 *            Directory.
		 */
		public DirectoryInclusion(File directory) {
			this.directoryLocation = directory.getPath();
		}

		/*
		 * ===================== Inclusion ===========================
		 */

		@Override
		public void include(ClassPathBuilder builder) throws Exception {
			builder.includeDirectory(new File(this.directoryLocation));
		}
	}

}