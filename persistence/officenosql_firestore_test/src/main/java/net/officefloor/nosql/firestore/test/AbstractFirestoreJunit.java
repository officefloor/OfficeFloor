/*-
 * #%L
 * Firestore Persistence Testing
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.nosql.firestore.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.google.cloud.firestore.Firestore;

import net.officefloor.docker.test.DockerContainerInstance;
import net.officefloor.docker.test.OfficeFloorDockerUtil;
import net.officefloor.test.JUnitAgnosticAssert;
import net.officefloor.test.SkipUtil;

/**
 * Abstract JUnit {@link Firestore} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFirestoreJunit<T extends AbstractFirestoreJunit<T>>
		extends AbstractFirestoreConnectJunit {

	/**
	 * Docker image name for the {@link Firestore} emulator.
	 */
	public static final String FIRESTORE_IMAGE_NAME = "officefloor-firestore";

	/**
	 * Docker tag name for the {@link Firestore} emulator.
	 */
	public static final String FIRESTORE_TAG_NAME = "emulator";

	/**
	 * {@link DockerContainerInstance} for {@link Firestore}.
	 */
	private DockerContainerInstance firestoreContainer;

	/**
	 * Flags to wait for {@link Firestore} to start.
	 */
	private boolean isWaitForFirestore = true;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public AbstractFirestoreJunit() {
		super();
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public AbstractFirestoreJunit(Configuration configuration) {
		super(configuration);
	}

	/**
	 * Sets up to wait for {@link Firestore} to be available.
	 * 
	 * @param isWaitForFirestore Indicates if wait for {@link Firestore} to be
	 *                           available.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public T waitForFirestore(boolean isWaitForFirestore) {
		this.isWaitForFirestore = isWaitForFirestore;
		return (T) this;
	}

	/**
	 * Obtains the emulator host.
	 * 
	 * @return Emulator host.
	 */
	public String getEmulatorHost() {
		return "localhost:" + this.getFirestorePort();
	}

	/*
	 * =================== AbstractFirestoreConnectJunit ================
	 */

	@Override
	protected void extendStart() throws Exception {

		// Avoid starting up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			System.out.println("Docker not available. Unable to start Firestore.");
			return;
		}

		// Ensure have Firestore emulator image
		OfficeFloorDockerUtil.ensureImageAvailable(FIRESTORE_IMAGE_NAME, FIRESTORE_TAG_NAME, () -> {

			// Ensure files are available
			File targetDir = new File(".", "target/firestore");
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			this.ensureFileInTargetDirectory("Dockerfile", targetDir);
			this.ensureFileInTargetDirectory("firestore.sh", targetDir);

			// Build from target directory
			return targetDir;
		});

		// Start Firestore
		this.firestoreContainer = this.ensureFirestoreAvailable();

		// Determine if wait for Firestore on start
		if (this.isWaitForFirestore) {

			// Allow some time for start up
			Thread.sleep(100);

			try {
				// Wait for Firestore to be available
				this.getFirestore();

			} catch (FirestoreStartTimeoutException ex) {
				// Give firestore another chance to start correctly
				this.firestoreContainer.close();
				this.firestoreContainer = this.ensureFirestoreAvailable();
				
				// Wait again for Firestore to be available
				this.getFirestore();
			}
		}
	}
	
	private DockerContainerInstance ensureFirestoreAvailable() throws Exception {
		final String CONTAINER_NAME = "officefloor-firestore";
		return OfficeFloorDockerUtil.ensureContainerAvailable(CONTAINER_NAME, FIRESTORE_IMAGE_NAME,
				FIRESTORE_TAG_NAME, (docker, imageName) -> {
					final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(new PortBinding(
							Binding.bindIpAndPort("0.0.0.0", this.getFirestorePort()), ExposedPort.tcp(8080)));
					return docker.createContainerCmd(imageName).withName(CONTAINER_NAME).withHostConfig(hostConfig);
				});
	}

	@Override
	public Firestore getFirestore() {
		return this.firestoreContainer.connectToDockerInstance(() -> super.getFirestore());
	}

	@Override
	protected void extendStop() throws Exception {

		// Avoid stopping up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			return;
		}

		// Stop Firestore
		if (this.firestoreContainer != null) {
			this.firestoreContainer.close();
		}
	}

	/**
	 * Ensures the file is in the target directory.
	 * 
	 * @param fileName  Name of file to copy into target directory.
	 * @param targetDir Target directory.
	 * @throws IOException If fails to copy in the file.
	 */
	private void ensureFileInTargetDirectory(String fileName, File targetDir) throws IOException {

		// Obtain contents of file
		String contents;
		String packageFolderPath = this.getClass().getPackage().getName().replace('.', '/');
		try (InputStream fileInput = this.getClass().getClassLoader()
				.getResourceAsStream(packageFolderPath + "/" + fileName)) {
			JUnitAgnosticAssert.assertNotNull(fileInput, "Unable to find file " + fileName);
			contents = this.readContents(fileInput);
		}

		// Determine if file already exists
		File targetFile = new File(targetDir, fileName);
		if (targetFile.exists()) {

			// Determine if have to overwrite contents (as not as expected)
			String targetContents = this.readContents(new FileInputStream(targetFile));
			if (targetContents.equals(contents)) {
				// File exists as required
				return;
			}
		}

		// Write file to target directory
		try (Writer output = new FileWriter(targetFile)) {
			output.write(contents);
		}
	}

	/**
	 * Reads the contents.
	 * 
	 * @param input {@link InputStream}.
	 * @return Contents.
	 * @throws IOException If fails to read {@link InputStream}.
	 */
	private String readContents(InputStream input) throws IOException {

		// Obtain the contents
		StringWriter contents = new StringWriter();
		Reader fileReader = new InputStreamReader(input);
		for (int character = fileReader.read(); character != -1; character = fileReader.read()) {
			contents.write(character);
		}

		// Return the contents
		return contents.toString();
	}

}
