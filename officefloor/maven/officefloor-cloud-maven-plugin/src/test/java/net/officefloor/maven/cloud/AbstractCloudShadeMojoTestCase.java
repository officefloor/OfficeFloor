package net.officefloor.maven.cloud;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.jupiter.api.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.cabinet.Cabinet;
import net.officefloor.cabinet.dynamo.DynamoOfficeStore;
import net.officefloor.cabinet.dynamo.DynamoOfficeStoreServiceFactory;
import net.officefloor.cabinet.firestore.FirestoreOfficeStore;
import net.officefloor.cabinet.firestore.FirestoreOfficeStoreServiceFactory;
import net.officefloor.cabinet.source.CabinetManagerManagedObjectSource;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.woof.WoofLoaderImpl;

/**
 * Ensures the two cloud jars are created.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractCloudShadeMojoTestCase {

	protected static final Class<?>[] OFFICEFLOOR_CLASSES = new Class[] { OfficeFloorMain.class };

	protected static final Class<?>[] WOOF_CLASSES = new Class[] { WoofLoaderImpl.class };

	protected static final Class<?>[] CABINET_CLASSES = new Class[] { Cabinet.class, CabinetManager.class };

	protected static final Class<?>[] CABINET_IMPL_CLASSES = new Class[] { CabinetManagerManagedObjectSource.class };

	protected static final Class<?>[] AWS_CLASSES = new Class[] { DynamoOfficeStoreServiceFactory.class, DynamoOfficeStore.class };

	protected static final Class<?>[] GOOGLE_CLASSES = new Class[] { FirestoreOfficeStoreServiceFactory.class, FirestoreOfficeStore.class };

	protected class JarClasses {
		public boolean isCreate = true;
		public final List<Class<?>> required = new LinkedList<>();

		public void required(Class<?>[]... classLists) {
			for (Class<?>[] classList : classLists) {
				this.required.addAll(Arrays.asList(classList));
			}
		}

		public final List<Class<?>> notIncluded = new LinkedList<>();

		public void notIncluded(Class<?>[]... classLists) {
			for (Class<?>[] classList : classLists) {
				this.notIncluded.addAll(Arrays.asList(classList));
			}
		}
	}

	protected final JarClasses JAR = new JarClasses();

	protected final JarClasses AWS = new JarClasses();

	protected final JarClasses GOOGLE = new JarClasses();

	public AbstractCloudShadeMojoTestCase() {
		JAR.notIncluded(OFFICEFLOOR_CLASSES, WOOF_CLASSES, CABINET_CLASSES, CABINET_IMPL_CLASSES, AWS_CLASSES, GOOGLE_CLASSES);
		AWS.notIncluded(GOOGLE_CLASSES);
		GOOGLE.notIncluded(AWS_CLASSES);
	}

	/**
	 * Ensure original shade JAR contains necessary classes.
	 */
	@Test
	public void jar() throws Exception {
		this.assertJar(null, JAR.isCreate, JAR.required, JAR.notIncluded);
	}

	/**
	 * Ensure additional AWS classes.
	 */
	@Test
	public void aws() throws Exception {
		this.assertJar("aws", AWS.isCreate, AWS.required, AWS.notIncluded);
	}

	/**
	 * Ensure additional Google classes.
	 */
	@Test
	public void google() throws Exception {
		this.assertJar("google", GOOGLE.isCreate, GOOGLE.required, GOOGLE.notIncluded);
	}

	private void assertJar(String classifier, boolean isCreateJar, List<Class<?>> requiredClasses,
			List<Class<?>> notIncludedClasses) throws Exception {

		// Determine classifier
		String classifierRegexPart = (classifier != null) ? "-" + classifier : "";

		// Obtain test directory name
		String testDirectoryName = this.getClass().getSimpleName();
		testDirectoryName = testDirectoryName.substring(0, testDirectoryName.length() - "IT".length());

		// Search for created jar
		File targetDir = new File("./target/it/" + testDirectoryName + "/target");
		assertTrue(targetDir.exists(), "Can not find test directory " + targetDir.getPath());
		File cloudJar = null;
		for (File checkFile : targetDir.listFiles()) {
			String fileName = checkFile.getName();
			if (fileName.matches("^Cloud-(\\d)+\\.(\\d)+\\.(\\d)+" + classifierRegexPart + "\\.jar")) {
				cloudJar = checkFile;
			}
		}
		if (isCreateJar) {
			assertNotNull(cloudJar, "No JAR created for " + classifier + ".\nFiles were:\n\t"
					+ String.join("\n\t", targetDir.list()) + "\n");
		} else {
			assertNull(cloudJar, "Should not create JAR for " + classifier);
			return; // nothing further to test
		}

		// Check the necessary classes are included
		try (JarFile jar = new JarFile(cloudJar)) {

			// Ensure all the required classes are included
			for (Class<?> clazz : requiredClasses) {
				assertNotNull(getJarEntry(jar, clazz),
						"Missing required class " + clazz.getName() + " in JAR " + cloudJar.getAbsolutePath());
			}

			// Ensure all not included classes are not included
			for (Class<?> clazz : notIncludedClasses) {
				assertNull(getJarEntry(jar, clazz),
						"Should not include class " + clazz.getName() + " in JAR " + cloudJar.getAbsolutePath());
			}
		}
	}

	private static JarEntry getJarEntry(JarFile jar, Class<?> clazz) {
		return jar.getJarEntry(clazz.getName().replace('.', '/') + ".class");
	}

}