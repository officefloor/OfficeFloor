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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;

import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ClassPathBuilder} (and also the
 * {@link ClassPathBuilderFactory}).
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathBuilderTest extends OfficeFrameTestCase {

	/**
	 * Artifacts in order of typical class path.
	 */
	private static String[] ARTIFACTS = new String[] { "officecompiler",
			"officeframe", "officexml", "officemodelgen" };

	/**
	 * {@link ClassPathBuilder} to test.
	 */
	private ClassPathBuilder builder;

	/**
	 * {@link OfficeFloor} version.
	 */
	private String officeFloorVersion;

	@Override
	protected void setUp() throws Exception {
		this.officeFloorVersion = OfficeBuildingTestUtil
				.getOfficeFloorVersion();
		this.builder = OfficeBuildingTestUtil.getClassPathBuilderFactory()
				.createClassPathBuilder();
	}

	@Override
	protected void tearDown() throws Exception {
		// Ensure clean up
		this.builder.close();
	}

	/**
	 * Ensure artifact and its dependencies appear on class path.
	 */
	public void testIncludeArtifact() throws Exception {

		// Build the class path for the artifact
		this.builder.includeArtifact("net.officefloor.core", "officecompiler",
				this.officeFloorVersion, "jar", null);

		// Ensure correct class path
		this.assertClassPath(ARTIFACTS);
	}

	/**
	 * Ensure can seed with an Artifact.
	 */
	public void testSeedArtifact() throws Exception {

		// Create the seed
		ClassPathSeed seed = new ClassPathSeed();
		seed.includeArtifact("net.officefloor.core", "officecompiler",
				this.officeFloorVersion, "jar", null);

		// Build the class path for the seed
		this.builder.includeSeed(seed);

		// Ensure correct class path
		this.assertClassPath(ARTIFACTS);
	}

	/**
	 * Ensure if similar artifacts are included that duplicate class path
	 * entries are not included.
	 */
	public void testIncludeMultipleSimilarArtifacts() throws Exception {

		// Include multiple similar artifacts (in terms of dependencies)
		for (String artifactId : ARTIFACTS) {
			this.builder.includeArtifact("net.officefloor.core", artifactId,
					this.officeFloorVersion, "jar", null);
		}

		// Ensure correct class path (no duplicate artifacts)
		this.assertClassPath(ARTIFACTS);
	}

	/**
	 * Ensure if similar artifacts are seeded that duplicate class path entries
	 * are not included.
	 */
	public void testSeedMultipleSimilarArtifacts() throws Exception {

		// Seed multiple similar artifacts (in terms of dependencies)
		ClassPathSeed seed = new ClassPathSeed();
		for (String artifactId : ARTIFACTS) {
			seed.includeArtifact("net.officefloor.core", artifactId,
					this.officeFloorVersion, "jar", null);
		}

		// Build the class path for the seed
		this.builder.includeSeed(seed);

		// Ensure correct class path (no duplicate artifacts)
		this.assertClassPath(ARTIFACTS);
	}

	/**
	 * Ensure that class path entry order is dictated by the order of include
	 * calls.
	 */
	public void testIncludeClassPathEntryOrder() throws Exception {

		// Different order of artifacts
		String[] artifactIds = new String[] { "officeframe", "officexml",
				"officemodelgen", "officecompiler" };

		// Include multiple similar artifacts (in terms of dependencies)
		for (String artifactId : artifactIds) {
			this.builder.includeArtifact("net.officefloor.core", artifactId,
					this.officeFloorVersion, "jar", null);
		}

		// Ensure correct class path (no duplicate artifacts)
		this.assertClassPath(artifactIds);
	}

	/**
	 * Ensure that class path entry order is dictated by the order of seeding.
	 */
	public void testSeedClassPathEntryOrder() throws Exception {

		// Different order of artifacts
		String[] artifactIds = new String[] { "officeframe", "officexml",
				"officemodelgen", "officecompiler" };

		// Include multiple similar artifacts (in terms of dependencies)
		ClassPathSeed seed = new ClassPathSeed();
		for (String artifactId : artifactIds) {
			seed.includeArtifact("net.officefloor.core", artifactId,
					this.officeFloorVersion, "jar", null);
		}

		// Build the class path for the seed
		this.builder.includeSeed(seed);

		// Ensure correct class path (no duplicate artifacts)
		this.assertClassPath(artifactIds);
	}

	/**
	 * Ensure able to include a jar in class path.
	 */
	public void testIncludeJar() throws Exception {

		// Obtain location of jar
		File jarFile = this.getJarFile();

		// Build the class path for the jar
		this.builder.includeJar(jarFile);

		// Ensure correct class path
		this.assertClassPath(ARTIFACTS);
	}

	/**
	 * Ensure able to seed by jar.
	 */
	public void testSeedJar() throws Exception {

		// Obtain location of jar
		File jarFile = this.getJarFile();

		// Create the seed
		ClassPathSeed seed = new ClassPathSeed();
		seed.includeJar(jarFile);

		// Include seed
		this.builder.includeSeed(seed);

		// Ensure correct class path
		this.assertClassPath(ARTIFACTS);
	}

	/**
	 * Ensure able to include directory in class path.
	 */
	public void testIncludeDirectory() throws Exception {

		// Obtain location of directory
		File directory = new File(".");

		// Build the class path for the directory
		this.builder.includeDirectory(directory);

		// Ensure correct class path
		this.assertClassPath(directory);
	}

	/**
	 * Ensure able to seed directory.
	 */
	public void testSeedDirectory() throws Exception {

		// Obtain location of directory
		File directory = new File(".");

		// Include directory in seed
		ClassPathSeed seed = new ClassPathSeed();
		seed.includeDirectory(directory);

		// Include seed
		this.builder.includeSeed(seed);

		// Ensure correct class path
		this.assertClassPath(directory);
	}

	/**
	 * Ensure able to serialise the {@link ClassPathSeed} for use in calling
	 * {@link OfficeBuildingManagerMBean}.
	 */
	public void testSerialiseSeed() throws Exception {

		// Create seed with the various includes
		ClassPathSeed seed = new ClassPathSeed();
		seed.includeArtifact("net.officefloor.core", "officecompiler",
				this.officeFloorVersion, "jar", null);
		seed.includeJar(this.getJarFile());
		seed.includeDirectory(new File("."));

		// Ensure can serialise
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		assertEquals("Should be no content", 0, bytes.size());
		ObjectOutputStream output = new ObjectOutputStream(bytes);
		output.writeObject(seed);
		output.close();
		assertTrue("Should be serialised content", bytes.size() > 0);
	}

	/**
	 * Asserts the built class path is correct.
	 * 
	 * @param directory
	 *            Directory for class path.
	 * @throws Exception
	 *             If fails to determine class path.
	 */
	private void assertClassPath(File directory) throws Exception {
		// Create the expected class path
		String expectedClassPath = directory.getCanonicalPath();

		// Obtain the actual class path
		String actualClassPath = this.builder.getBuiltClassPath();

		// Ensure correct class path
		assertEquals(expectedClassPath, actualClassPath);
	}

	/**
	 * Asserts the built class path is correct.
	 * 
	 * @param officeFloorArtifactIds
	 *            {@link OfficeFloor} artifact ids.
	 * @throws Exception
	 *             If fails to determine class path.
	 */
	private void assertClassPath(String... officeFloorArtifactIds)
			throws Exception {
		// Create the expected class path
		String expectedClassPath = OfficeBuildingTestUtil
				.getOfficeFloorClassPath(officeFloorArtifactIds);

		// Obtain the actual class path
		String actualClassPath = this.builder.getBuiltClassPath();

		// Ensure correct class path
		assertEquals(expectedClassPath, actualClassPath);
	}

	/**
	 * Obtains the Jar {@link File}.
	 * 
	 * @return Jar {@link File}.
	 * @throws Exception
	 *             If fails.
	 */
	private File getJarFile() throws Exception {
		// Obtain location of jar
		String jarPath = OfficeBuildingTestUtil
				.getOfficeFloorClassPath("officecompiler");
		File jarFile = new File(jarPath);
		assertTrue("Ensure jar exists", jarFile.isFile());
		return jarFile;
	}

}