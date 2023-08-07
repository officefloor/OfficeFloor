package net.officefloor.nosql.firestore.test;

import java.util.HashMap;
import java.util.Map;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import net.officefloor.nosql.firestore.FirestoreConnect;

/**
 * Abstract JUnit connect {@link Firestore} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractFirestoreConnectJunit {

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
	private final Configuration configuration;

	/**
	 * {@link Firestore}.
	 */
	private Firestore firestore = null;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public AbstractFirestoreConnectJunit() {
		this(new Configuration());
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public AbstractFirestoreConnectJunit(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Obtains port {@link Firestore} running on.
	 * 
	 * @return Port {@link Firestore} running on.
	 */
	protected int getFirestorePort() {
		return this.configuration.port;
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
	 * Start {@link Firestore} connection.
	 * 
	 * @throws Exception If fails to start.
	 */
	protected void startFirestore() throws Exception {

		// Provide Firestore factory to connect
		FirestoreConnect.setFirestoreFactory(() -> this.getFirestore());

		// Extend the start
		this.extendStart();
	}

	/**
	 * Extends starting {@link Firestore}.
	 * 
	 * @throws Exception If fails extending start.
	 */
	protected void extendStart() throws Exception {
		// Do nothing
	}

	/**
	 * Stops connection to {@link Firestore}.
	 * 
	 * @throws Exception If fails to stop.
	 */
	protected void stopFirestore() throws Exception {
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
			// Extend stop
			this.extendStop();
		}
	}

	/**
	 * Extends stopping {@link Firestore}.
	 * 
	 * @throws Exception If fails extending stop.
	 */
	protected void extendStop() throws Exception {
		// Do nothing
	}

}