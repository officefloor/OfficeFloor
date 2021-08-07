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
	public static final String FIRESTORE_IMAGE_NAME = "officefloor-firestore:emulator";

	/**
	 * Default local {@link Firestore} port.
	 */
	public static final int DEFAULT_LOCAL_FIRESTORE_PORT = 8002;

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
		 * Specifies the port.
		 * 
		 * @param port Port.
		 * @return <code>this</code>.
		 */
		public Configuration port(int port) {
			this.port = port;
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
			boolean isAvailable = false;
			final int MAX_SETUP_TIME = 10_000; // milliseconds
			long startTimestamp = System.currentTimeMillis();
			do {
				try {

					// Connect to firestore (will attempt to retry until available)
					if (this.firestore == null) {
						FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
								.setEmulatorHost("localhost:" + this.configuration.port).build();
						this.firestore = firestoreOptions.getService();
					}

					// Attempt to obtain document to check connection available
					this.firestore.collection("AVAILABLE").document("AVAILABLE").get().get();

					// As here, Firestore available
					isAvailable = true;

				} catch (Exception ex) {

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

			} while (!isAvailable);
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
		OfficeFloorDockerUtil.ensureImageAvailable(FIRESTORE_IMAGE_NAME, () -> {

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
				(docker) -> {
					final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(new PortBinding(
							Binding.bindIpAndPort("0.0.0.0", this.configuration.port), ExposedPort.tcp(8080)));
					return docker.createContainerCmd(FIRESTORE_IMAGE_NAME).withName(CONTAINER_NAME)
							.withHostConfig(hostConfig);
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
