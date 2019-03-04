package net.officefloor.identity.google;

import java.util.function.Consumer;

import org.junit.runners.model.Statement;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.identity.google.mock.GoogleIdTokenRule;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * <p>
 * Tests the {@link GoogleIdTokenVerifierManagedObjectSource}.
 * <p>
 * Note: can not make calls to Google for unit tests, so use
 * {@link GoogleIdTokenRule} to mock the transport and certificate verification.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleIdTokenTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.close();
		}
	}

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(GoogleIdTokenVerifierManagedObjectSource.class,
				"google.client.id", "Client ID");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(GoogleIdTokenVerifier.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, GoogleIdTokenVerifierManagedObjectSource.class,
				GoogleIdTokenVerifierManagedObjectSource.PROPERTY_CLIENT_ID, "testId");
	}

	/**
	 * Ensure can authenticate with {@link GoogleIdTokenVerifier}.
	 */
	public void testAuthenticate() throws Throwable {

		// Clear token
		VerifySection.googleToken = null;

		// Validates the token
		Consumer<GoogleIdToken> validator = (token) -> {
			assertEquals("Incorrect identifier", "12345", token.getPayload().getSubject());
			assertEquals("Incorrect email", "daniel@officefloor.net", token.getPayload().getEmail());
			assertEquals("Incorrect name", "value", token.getPayload().get("name"));
		};

		// Ensure mock google token
		// (trust Google has it working for live connections)
		GoogleIdTokenRule rule = new GoogleIdTokenRule();
		rule.apply(new Statement() {
			@Override
			public void evaluate() throws Throwable {

				// Create token
				String token = rule.getMockIdToken("12345", "daniel@officefloor.net", "name", "value");

				// Ensure can decode token
				GoogleIdToken googleToken = rule.getGoogleIdTokenVerifier().verify(token);
				validator.accept(googleToken);

				// Ensure can use token verifier
				CompileOfficeFloor compiler = new CompileOfficeFloor();
				compiler.office((context) -> {

					// Add the Google ID token verifier
					OfficeManagedObjectSource verifier = context.getOfficeArchitect().addOfficeManagedObjectSource(
							"VERIFIER", GoogleIdTokenVerifierManagedObjectSource.class.getName());
					verifier.addProperty(GoogleIdTokenVerifierManagedObjectSource.PROPERTY_CLIENT_ID, "client@google");
					verifier.addOfficeManagedObject("VERIFIER", ManagedObjectScope.THREAD);

					// Add section to handle
					context.addSection("VERIFY", VerifySection.class);
				});
				GoogleIdTokenTest.this.officeFloor = compiler.compileAndOpenOfficeFloor();

				// Undertake verifying the token
				CompileOfficeFloor.invokeProcess(GoogleIdTokenTest.this.officeFloor, "VERIFY.service", token);

				// Ensure correct client id
				assertEquals("Incorrect client id", "client@google", rule.getGoogleClientId());
			}
		}, null).evaluate();

		// Ensure correct google id token loaded
		validator.accept(VerifySection.googleToken);
	}

	public static class VerifySection {

		private static GoogleIdToken googleToken;

		public void service(@Parameter String token, GoogleIdTokenVerifier verifier) throws Exception {
			googleToken = verifier.verify(token);
		}
	}

}