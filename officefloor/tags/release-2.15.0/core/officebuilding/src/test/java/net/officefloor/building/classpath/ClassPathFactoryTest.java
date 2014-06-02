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
package net.officefloor.building.classpath;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.building.util.OfficeBuildingTestUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

/**
 * Tests the {@link ClassPathFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathFactoryTest extends OfficeFrameTestCase {

	/**
	 * Group Id for test artifacts.
	 */
	public static final String TEST_GROUP_ID = "test.officefloor.test";

	/**
	 * Version for the test artifacts.
	 */
	public static final String TEST_ARTIFACT_VERSION = "1.0.0";

	/**
	 * Artifact Id of jar with no dependencies.
	 */
	public static final String TEST_JAR_ARTIFACT_ID = "JarArtifact";

	/**
	 * Artifact Id of jar with dependencies.
	 */
	public static final String TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID = "JarWithDependenciesArtifact";

	/**
	 * Obtains the remote repository.
	 * 
	 * @return Remote repository.
	 */
	public static File getTestRemoteRepository() {
		File directory = new File(".", "target/test-classes/remoteRepository");
		TestCase.assertTrue("Remote repository directory not available: "
				+ directory.getAbsolutePath(), directory.exists());
		return directory;
	}

	/**
	 * Obtain the path to the test artifact jar.
	 * 
	 * @param isLocal
	 *            Indicates if the artifact is within local repository.
	 * @param artifactId
	 *            Test artifact id.
	 * @return Path to the test artifact jar.
	 */
	private File getTestArtifactJar(boolean isLocal, String artifactId)
			throws Exception {

		// Use appropriate repository
		File repository = (isLocal ? this.localRepository
				: getTestRemoteRepository());

		// Return artifact file
		return OfficeBuildingTestUtil.getArtifactFile(repository,
				TEST_GROUP_ID, artifactId, TEST_ARTIFACT_VERSION, "jar");
	}

	/**
	 * {@link ClassPathFactory} to test.
	 */
	private ClassPathFactory classPathFactory;

	/**
	 * Local repository.
	 */
	private File localRepository;

	@Override
	protected void setUp() throws Exception {

		// Obtain the local repository
		this.localRepository = OfficeBuildingTestUtil.getTestLocalRepository();

		// Create the class path factory to test
		File remoteRepository = new File(".",
				"src/test/resources/remoteRepository");
		this.classPathFactory = new ClassPathFactoryImpl(this.localRepository,
				new RemoteRepository[] { new RemoteRepository(remoteRepository
						.toURI().toURL().toString()) });
	}

	/**
	 * Ensure able to transform to class path.
	 */
	public void testTransformToClassPath() {

		// Transform to class path
		String classPath = ClassPathFactoryImpl
				.transformClassPathEntriesToClassPath("ONE", "TWO", "THREE");

		// Ensure correct class path
		assertEquals("Incorrect class path", "ONE" + File.pathSeparator + "TWO"
				+ File.pathSeparator + "THREE", classPath);
	}

	/**
	 * Ensure able to include a jar in class path.
	 */
	public void testIncludeJar() throws Exception {

		// Obtain path to jar
		final File JAR = this.getTestArtifactJar(false, TEST_JAR_ARTIFACT_ID);

		// Obtain class path
		String[] classPath = this.classPathFactory.createArtifactClassPath(JAR
				.getCanonicalPath());

		// Ensure jar on class path
		assertClassPath(classPath, JAR);
	}

	/**
	 * Ensure able to include a jar with dependencies in class path.
	 */
	public void testIncludeJarWithDependencies() throws Exception {

		// Obtain paths to jars
		final File JAR_WITH_DEPENDENCIES = this.getTestArtifactJar(false,
				TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID);

		// Obtain class path
		String[] classPath = this.classPathFactory
				.createArtifactClassPath(JAR_WITH_DEPENDENCIES
						.getCanonicalPath());

		// Obtain the expected class path entries
		final File CLASSPATH_JAR_WITH_DEPENDENCIES = this.getTestArtifactJar(
				true, TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID);
		final File CLASSPATH_JAR = this.getTestArtifactJar(true,
				TEST_JAR_ARTIFACT_ID);

		// Ensure jars on class path
		assertClassPath(classPath, JAR_WITH_DEPENDENCIES,
				CLASSPATH_JAR_WITH_DEPENDENCIES, CLASSPATH_JAR);
	}

	/**
	 * Ensure can include a directory in class path.
	 */
	public void testIncludeDir() throws Exception {

		// Obtain path to directory
		final File DIR = this.findFile(this.getClass(), "DirArtifact/Test.txt")
				.getParentFile();

		// Obtain class path
		String[] classPath = this.classPathFactory.createArtifactClassPath(DIR
				.getCanonicalPath());

		// Ensure directory on class path with no warnings
		assertClassPath(classPath, DIR);
	}

	/**
	 * Ensure can include artifact in class path.
	 */
	public void testIncludeArtifact() throws Exception {

		// Obtain class path
		String[] classPath = this.classPathFactory.createArtifactClassPath(
				TEST_GROUP_ID, TEST_JAR_ARTIFACT_ID, TEST_ARTIFACT_VERSION,
				null, null);

		// Obtain path to artifact
		final File JAR = getTestArtifactJar(true, TEST_JAR_ARTIFACT_ID);

		// Ensure jar on class path
		assertClassPath(classPath, JAR);
	}

	/**
	 * Ensure able to include a artifact with dependencies in class path.
	 */
	public void testIncludeArtifactWithDependencies() throws Exception {

		// Include artifact
		String[] classPath = this.classPathFactory.createArtifactClassPath(
				TEST_GROUP_ID, TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID,
				TEST_ARTIFACT_VERSION, null, null);

		// Obtain paths to jars
		final File JAR_WITH_DEPENDENCIES = getTestArtifactJar(true,
				TEST_JAR_WITH_DEPENDENCIES_ARTIFACT_ID);
		final File JAR = getTestArtifactJar(true, TEST_JAR_ARTIFACT_ID);

		// Ensure jars on class path
		assertClassPath(classPath, JAR_WITH_DEPENDENCIES, JAR);
	}

	/**
	 * Ensure able to resolve the {@link Dependency} instances for
	 * <code>pom.xml</code> {@link File}.
	 */
	public void testGetMavenProject() throws Exception {

		// Obtain the pom.xml file
		File pomFile = this.findFile(this.getClass(), "pom.test.xml");

		// Obtain the Maven Project (using build local repository)
		ClassPathFactoryImpl factory = new ClassPathFactoryImpl(null,
				new RemoteRepository[0]);
		MavenProject project = factory.getMavenProject(pomFile);

		// Ensure contains appropriate dependencies
		List<Dependency> dependencies = project.getDependencies();
		assertTrue("Should have dependencies for project",
				dependencies.size() > 0);
		Dependency gwtUserDependency = null;
		for (Dependency dependency : dependencies) {
			if (("com.google.gwt".equals(dependency.getGroupId()))
					&& ("gwt-user".equals(dependency.getArtifactId()))) {
				assertNull("Should only be the one dependency",
						gwtUserDependency);
				gwtUserDependency = dependency;
			}
		}

		// Ensure able to resolve version
		assertNotNull("Must be able to resolve version",
				gwtUserDependency.getVersion());
	}

	/**
	 * Asserts the built class path is correct.
	 * 
	 * @param classPath
	 *            Class path to be validated.
	 * @param expectedClassPathEntries
	 *            Expected class path entries.
	 */
	private static void assertClassPath(String[] classPath,
			File... expectedClassPathEntries) throws Exception {

		// Incorrect number of class path entries
		assertEquals("Incorrect number of class path entries",
				expectedClassPathEntries.length, classPath.length);

		// Validate the class path
		for (int i = 0; i < expectedClassPathEntries.length; i++) {
			File expectedClassPathEntry = expectedClassPathEntries[i];
			File actualClassPathEntry = new File(classPath[i]);

			// Validate the class path entry
			assertEquals("Incorrect class path entry " + i,
					expectedClassPathEntry.getCanonicalPath(),
					actualClassPathEntry.getCanonicalPath());
		}
	}

}