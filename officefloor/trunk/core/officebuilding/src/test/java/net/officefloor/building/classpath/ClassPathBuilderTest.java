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

import junit.framework.TestCase;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Tests the {@link ClassPathBuilder} (and also the
 * {@link ClassPathBuilderFactory}).
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathBuilderTest extends TestCase {

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

		// Create the expected class path
		String expectedClassPath = OfficeBuildingTestUtil
				.getOfficeFloorClassPath("officecompiler", "officeframe",
						"officexml", "officemodelgen");

		// Build the class path for the artifact
		this.builder.includeArtifact("net.officefloor.core", "officecompiler",
				this.officeFloorVersion, "jar", null);
		String actualClassPath = this.builder.getBuiltClassPath();

		// Ensure correct class path
		assertEquals("Incorrect class path", expectedClassPath, actualClassPath);
	}

	/**
	 * Ensure if similar artifacts are included that duplicate class path
	 * entries are not included.
	 */
	public void testIncludeMultipleSimilarArtifacts() throws Exception {

		String[] artifactIds = new String[] { "officecompiler", "officeframe",
				"officexml", "officemodelgen" };

		// Create the expected class path
		String expectedClassPath = OfficeBuildingTestUtil
				.getOfficeFloorClassPath(artifactIds);

		// Include multiple similar artifacts (in terms of dependencies)
		for (String artifactId : artifactIds) {
			this.builder.includeArtifact("net.officefloor.core", artifactId,
					this.officeFloorVersion, "jar", null);
		}
		String actualClassPath = this.builder.getBuiltClassPath();

		// Ensure correct class path (no duplicate artifacts)
		assertEquals("Incorrect class path", expectedClassPath, actualClassPath);
	}

	/**
	 * Ensure that class path entry order is dictated by the order of include
	 * calls.
	 */
	public void testClassPathEntryOrder() throws Exception {

		String[] artifactIds = new String[] { "officeframe", "officexml",
				"officemodelgen", "officecompiler" };

		// Create the expected class path
		String expectedClassPath = OfficeBuildingTestUtil
				.getOfficeFloorClassPath(artifactIds);

		// Include multiple similar artifacts (in terms of dependencies)
		for (String artifactId : artifactIds) {
			this.builder.includeArtifact("net.officefloor.core", artifactId,
					this.officeFloorVersion, "jar", null);
		}
		String actualClassPath = this.builder.getBuiltClassPath();

		// Ensure correct class path (no duplicate artifacts)
		assertEquals("Incorrect class path", expectedClassPath, actualClassPath);
	}

	/**
	 * Ensure able to include a jar in class path.
	 */
	public void testIncludeJar() throws Exception {

		// Create the expected class path
		String expectedClassPath = OfficeBuildingTestUtil
				.getOfficeFloorClassPath("officecompiler", "officeframe",
						"officexml", "officemodelgen");

		// Obtain location of jar
		String jarPath = OfficeBuildingTestUtil
				.getOfficeFloorClassPath("officecompiler");
		File jarFile = new File(jarPath);
		assertTrue("Ensure jar exists", jarFile.isFile());

		// Build the class path for the jar
		this.builder.includeJar(jarFile);
		String actualClassPath = this.builder.getBuiltClassPath();

		// Ensure correct class path
		assertEquals("Incorrect class path", expectedClassPath, actualClassPath);
	}

}