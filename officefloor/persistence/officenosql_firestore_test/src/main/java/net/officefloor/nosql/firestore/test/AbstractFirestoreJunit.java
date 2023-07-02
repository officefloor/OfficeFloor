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
import java.util.HashMap;
import java.util.Map;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import net.officefloor.docker.test.DockerContainerInstance;
import net.officefloor.docker.test.OfficeFloorDockerUtil;
import net.officefloor.nosql.firestore.FirestoreConnect;
import net.officefloor.test.JUnitAgnosticAssert;
import net.officefloor.test.SkipUtil;

/**
 * Abstract JUnit {@link Firestore} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFirestoreJunit<T extends AbstractFirestoreJunit<T>> {

	/**
	 * Docker image name for the {@link Firestore} emulator.
	 */
	public static final String FIRESTORE_IMAGE_NAME = "officefloor-firestore";

	/**
	 * Docker tag name for the {@link Firestore} emulator.
	 */
	public static final String FIRESTORE_TAG_NAME = "emulator";

	/**
	 * Default local {@link Firestore} port.
	 */
	public static final int DEFAULT_LOCAL_FIRESTORE_PORT = 8002;

	/**
	 * Default timeout for starting the emulator.
	 */
	public static final int DEFAULT_EMULATOR_START_TIMEOUT = 30;

	/**
	 * Default project id.
	 */
	public static final String DEFAULT_PROJECT_ID = "officefloor-test";

	/**
	 * <p>
	 * Configuration of {@link Firestore}.
	 * <p>
	 * Follows builder pattern to allow configuring and passing to
	 * {@link AbstractFirestoreJunit} constructor.
	 */
	public static class Configuration {

		/**
		 * Port.
		 */
		private int port = DEFAULT_LOCAL_FIRESTORE_PORT;

		/**
		 * Project Id.
		 */
		private String projectId = DEFAULT_PROJECT_ID;

		/**
		 * Timeout for starting the emulator.
		 */
		private int startTimeout = DEFAULT_EMULATOR_START_TIMEOUT;

		/**
		 * Specifies the port.
		 * 
		 * @param port Port.
		 * @return <code>this</code>.
		 */
		public Configuration port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Specifies the project id.
		 * 
		 * @param projectId Project Id.
		 * @return <code>this</code>.
		 */
		public Configuration projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		/**
		 * Specifies the start timeout.
		 * 
		 * @param startTimeout Start timeout.
		 * @return <code>this</code>.
		 */
		public Configuration startTimeout(int startTimeout) {
			this.startTimeout = startTimeout;
			return this;
		}
	}

	/**
	 * {@link Configuration}.
	 */
	protected final Configuration configuration;

	/**
	 * {@link DockerContainerInstance} for {@link Firestore}.
	 */
	private DockerContainerInstance firestoreContainer;

	/**
	 * {@link Firestore}.
	 */
	private Firestore firestore = null;

	/**
	 * Flags to wait for {@link Firestore} to start.
	 */
	private boolean isWaitForFirestore = false;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public AbstractFirestoreJunit() {
		this(new Configuration());
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public AbstractFirestoreJunit(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Sets up to wait for {@link Firestore} to be available.
	 * 
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public T waitForFirestore() {
		this.isWaitForFirestore = true;
		return (T) this;
	}

	/**
	 * Obtains the {@link Firestore}.
	 * 
	 * @return {@link Firestore}.
	 */
	public Firestore getFirestore() {
		if (this.firestore == null) {

			// Wait until available
			Firestore firestore = null;
			final int MAX_SETUP_TIME = this.configuration.startTimeout * 1000; // milliseconds
			long startTimestamp = System.currentTimeMillis();
			do {
				try {

					// Connect to firestore (will attempt to retry until available)
					FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
							.setProjectId(this.configuration.projectId)
							.setEmulatorHost("localhost:" + this.configuration.port).build();
					firestore = firestoreOptions.getService();

					// Ensure can communicate with Firestore
					Map<String, Object> checkData = new HashMap<>();
					checkData.put("AVAILABLE", true);
					DocumentReference docRef = firestore.collection("_officefloor_check_available_").document();
					docRef.create(checkData).get();

					// Clean up
					docRef.delete().get();

				} catch (Exception ex) {

					// Failed, so clean up firestore
					if (firestore != null) {
						try {
							try {
								firestore.shutdownNow();
							} finally {
								firestore.close();
							}
						} catch (Exception ignore) {
							// Ignore clean up failure
						}

						// Unset to try again
						firestore = null;
					}

					// Failed connect, determine if try again
					long currentTimestamp = System.currentTimeMillis();
					if (currentTimestamp > (startTimestamp + MAX_SETUP_TIME)) {

						// Propagate failure to connect
						throw new RuntimeException("Timed out setting up Firestore ("
								+ (currentTimestamp - startTimestamp) + " milliseconds)", ex);

					} else {
						// Try again in a little
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// Ignore
						}
					}
				}

			} while (firestore == null);
			this.firestore = firestore;
		}
		return this.firestore;
	}

	/**
	 * Obtains the emulator host.
	 * 
	 * @return Emulator host.
	 */
	public String getEmulatorHost() {
		return "localhost:" + this.configuration.port;
	}

	/**
	 * Start {@link Firestore} locally.
	 * 
	 * @throws Exception If fails to start.
	 */
	protected void startFirestore() throws Exception {

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
		final String CONTAINER_NAME = "officefloor-firestore";
		this.firestoreContainer = OfficeFloorDockerUtil.ensureContainerAvailable(CONTAINER_NAME, FIRESTORE_IMAGE_NAME,
				FIRESTORE_TAG_NAME, (docker, imageName) -> {
					final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(new PortBinding(
							Binding.bindIpAndPort("0.0.0.0", this.configuration.port), ExposedPort.tcp(8080)));
					return docker.createContainerCmd(imageName).withName(CONTAINER_NAME).withHostConfig(hostConfig);
				});

		// Provide Firestore factory to connect
		FirestoreConnect.setFirestoreFactory(() -> this.getFirestore());

		// Determine if wait for Firestore on start
		if (this.isWaitForFirestore) {

			// Allow some time for start up
			Thread.sleep(100);

			// Wait for Firestore to be available
			this.getFirestore();
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

	/**
	 * Stops locally running {@link Firestore}.
	 * 
	 * @throws Exception If fails to stop.
	 */
	protected void stopFirestore() throws Exception {

		// Avoid stopping up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			return;
		}

		try {
			try {
				// Ensure client closed
				try {
					if (this.firestore != null) {
						this.firestore.close();
					}

				} finally {
					// Ensure clear connection factory
					FirestoreConnect.setFirestoreFactory(null);
				}
			} finally {
				// Ensure release client
				this.firestore = null;
			}
		} finally {
			// Stop Firestore
			if (this.firestoreContainer != null) {
				this.firestoreContainer.close();
			}
		}
	}

}
