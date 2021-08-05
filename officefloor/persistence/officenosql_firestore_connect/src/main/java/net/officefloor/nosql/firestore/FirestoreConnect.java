package net.officefloor.nosql.firestore;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link Firestore} connect functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreConnect {

	/**
	 * {@link Property} name to specify using emulator.
	 */
	public static final String FIRESTORE_EMULATOR_HOST = "FIRESTORE_EMULATOR_HOST";

	/**
	 * <p>
	 * Sets using the {@link FirestoreFactory}.
	 * <p>
	 * This is typically used for testing to allow overriding the
	 * {@link FirestoreFactory} being used.
	 * 
	 * @param firestoreFactory {@link FirestoreFactory}. May be <code>null</code> to
	 *                         not override.
	 */
	public static void setFirestoreFactory(FirestoreFactory firestoreFactory) {
		if (firestoreFactory != null) {
			// Undertake override
			threadLocalFirestoreFactoryOverride.set(firestoreFactory);
		} else {
			// Clear the override
			threadLocalFirestoreFactoryOverride.remove();
		}
	}

	/**
	 * {@link ThreadLocal} for the {@link FirestoreFactory}.
	 */
	private static ThreadLocal<FirestoreFactory> threadLocalFirestoreFactoryOverride = new ThreadLocal<>();

	/**
	 * <p>
	 * Connects to {@link Firestore}.
	 * <p>
	 * Note that the {@link Firestore} instance is not managed. It will need to be
	 * manually shutdown once use is complete.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link Firestore}.
	 * @throws Exception If fails to connect.
	 */
	public static Firestore connect(SourceContext context) throws Exception {

		// Determine if thread local instance available to use
		FirestoreFactory factory = threadLocalFirestoreFactoryOverride.get();
		if (factory == null) {

			// No thread local, so see if configured factory
			factory = context.loadOptionalService(FirestoreServiceFactory.class);
		}
		if (factory != null) {
			// Configured factory available, so use
			return factory.createFirestore();
		}

		// Determine if emulator configured
		String emulatorHost = context.getProperty(FIRESTORE_EMULATOR_HOST, null);
		if (emulatorHost != null) {

			// Connect to emulator
			FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder().setEmulatorHost(emulatorHost).build();
			return firestoreOptions.getService();
		}

		// No factory, so provide default connection
		return FirestoreOptions.getDefaultInstance().getService();
	}

	/**
	 * All access via static methods.
	 */
	private FirestoreConnect() {
	}

}