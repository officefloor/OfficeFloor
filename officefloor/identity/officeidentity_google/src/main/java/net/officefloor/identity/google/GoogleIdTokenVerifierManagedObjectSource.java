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
	 * Name of {@link Property} for the Google client id.
	 */
	private static final String PROPERTY_CLIENT_ID = "google.client.id";

	/**
	 * {@link JsonFactory}.
	 */
	private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

	/**
	 * {@link HttpTransport}.
	 */
	private static final HttpTransport transport = new NetHttpTransport();

	/**
	 * Default {@link GoogleIdTokenVerifierFactory}.
	 */
	private static final GoogleIdTokenVerifierFactory DEFAULT_FACTORY = (
			audienceId) -> new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
					.setAudience(Collections.singletonList(audienceId)).build();

	/**
	 * {@link GoogleIdTokenVerifierFactory} to create the
	 * {@link GoogleIdTokenVerifier}.
	 */
	private static volatile GoogleIdTokenVerifierFactory verifierFactory = DEFAULT_FACTORY;

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
		 */
		GoogleIdTokenVerifier create(String audienceId);
	}

	/**
	 * Specifies the {@link GoogleIdTokenVerifierFactory}.
	 * 
	 * @param factory {@link GoogleIdTokenVerifierFactory}.
	 */
	public static void setGoogleIdTokenVerifierFactory(GoogleIdTokenVerifierFactory factory) {
		verifierFactory = (factory != null) ? factory : DEFAULT_FACTORY;
	}

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

		// Load the verifier
		String audienceId = context.getManagedObjectSourceContext().getProperty(PROPERTY_CLIENT_ID);
		this.verifier = verifierFactory.create(audienceId);
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