package net.officefloor.identity.google;

import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link GoogleIdTokenVerifier}.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleIdTokenVerifierManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * Factory {@link FunctionalInterface} to create the
	 * {@link GoogleIdTokenVerifier}.
	 */
	@FunctionalInterface
	public static interface GoogleIdTokenVerifierFactory {

		/**
		 * Creates the {@link GoogleIdTokenVerifier}.
		 *
		 * @param audienceId Audience identifier.
		 * @return {@link GoogleIdTokenVerifier}.
		 * @throws Exception If fails to create the {@link GoogleIdTokenVerifier}.
		 */
		GoogleIdTokenVerifier create(String audienceId) throws Exception;
	}

	/**
	 * Context {@link Runnable}.
	 */
	@FunctionalInterface
	public static interface ContextRunnable<T extends Throwable> {

		/**
		 * {@link Runnable} logic.
		 * 
		 * @throws T Potential failure.
		 */
		void run() throws T;
	}

	/**
	 * Runs the {@link ContextRunnable} with the
	 * {@link GoogleIdTokenVerifierFactory}.
	 * 
	 * @param factory  {@link GoogleIdTokenVerifierFactory}.
	 * @param runnable {@link ContextRunnable}.
	 * @throws T Possible failure from {@link ContextRunnable}.
	 */
	public static <T extends Throwable> void runWithFactory(GoogleIdTokenVerifierFactory factory,
			ContextRunnable<T> runnable) throws T {
		threadLocalVerifierFactory.set(factory);
		try {
			runnable.run();
		} finally {
			threadLocalVerifierFactory.remove();
		}
	}

	/**
	 * Name of {@link Property} for the Google client id.
	 */
	public static final String PROPERTY_CLIENT_ID = "google.client.id";

	/**
	 * {@link GoogleIdTokenVerifierFactory} to create the
	 * {@link GoogleIdTokenVerifier}.
	 */
	private static ThreadLocal<GoogleIdTokenVerifierFactory> threadLocalVerifierFactory = new ThreadLocal<>();

	/**
	 * {@link GoogleIdTokenVerifier}.
	 */
	private GoogleIdTokenVerifier verifier;

	/*
	 * ==================== ManagedObjectSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_CLIENT_ID, "Client ID");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(GoogleIdTokenVerifier.class);

		// Obtain the verifier factory
		GoogleIdTokenVerifierFactory factory = threadLocalVerifierFactory.get();
		if (factory == null) {

			// Default verifier factory
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			HttpTransport transport = new NetHttpTransport();
			factory = (audienceId) -> new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
					.setAudience(Collections.singletonList(audienceId)).build();
		}

		// Load the verifier
		String audienceId = context.getManagedObjectSourceContext().getProperty(PROPERTY_CLIENT_ID);
		this.verifier = factory.create(audienceId);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================= ManagedObject ===============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.verifier;
	}

}