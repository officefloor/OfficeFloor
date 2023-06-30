/*-
 * #%L
 * Identity for Google Logins
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.identity.google;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.runners.model.Statement;

import com.google.api.client.auth.openidconnect.IdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.identity.google.mock.GoogleIdTokenRule;
import net.officefloor.plugin.managedobject.singleton.Singleton;
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
		ManagedObjectLoaderUtil.validateSpecification(GoogleIdTokenVerifierManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testTypeViaProperty() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setInput(true);
		type.setObjectClass(GoogleIdTokenVerifier.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, GoogleIdTokenVerifierManagedObjectSource.class,
				GoogleIdTokenVerifierManagedObjectSource.PROPERTY_CLIENT_ID, "testId");
	}

	/**
	 * Ensure correct type.
	 */
	public void testTypeViaFactory() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setInput(true);
		type.setObjectClass(GoogleIdTokenVerifier.class);
		type.addFunctionDependency(GoogleIdTokenVerifierFactory.class.getSimpleName(),
				GoogleIdTokenVerifierFactory.class, null);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, GoogleIdTokenVerifierManagedObjectSource.class);
	}

	/**
	 * Ensure can configure via {@link Property}.
	 */
	public void testConfigureViaProperty() throws Throwable {
		String audienceId = "test@google";
		IdTokenVerifier verifier = this.doConfigureTest((mos, context) -> mos
				.addProperty(GoogleIdTokenVerifierManagedObjectSource.PROPERTY_CLIENT_ID, audienceId));
		assertEquals("Should configure from property", audienceId, verifier.getAudience().iterator().next());
	}

	/**
	 * Ensure can configure via {@link GoogleIdTokenVerifierFactory}.
	 */
	public void testConfigureViaFactory() throws Throwable {
		GoogleIdTokenVerifier expected = new GoogleIdTokenVerifier(new NetHttpTransport(),
				GsonFactory.getDefaultInstance());
		GoogleIdTokenVerifierFactory factory = () -> expected;
		GoogleIdTokenVerifier actual = this.doConfigureTest((mos, context) -> Singleton
				.load(context.getOfficeArchitect(), factory, new AutoWire(GoogleIdTokenVerifierFactory.class)));
		assertSame("Should load via factory", expected, actual);
	}

	/**
	 * Undertakes configuring {@link GoogleIdTokenVerifier}.
	 * 
	 * @return {@link IdTokenVerifier}.
	 */
	private GoogleIdTokenVerifier doConfigureTest(BiConsumer<OfficeManagedObjectSource, CompileOfficeContext> configure)
			throws Throwable {

		// Clear verifier
		CaptureSection.googleIdTokenVerifier = null;

		// Load verifier
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Add the Google ID token verifier
			OfficeManagedObjectSource verifier = context.getOfficeArchitect().addOfficeManagedObjectSource("VERIFIER",
					GoogleIdTokenVerifierManagedObjectSource.class.getName());
			configure.accept(verifier, context);
			verifier.addOfficeManagedObject("VERIFIER", ManagedObjectScope.THREAD);

			// Add section to handle
			context.addSection("VERIFY", CaptureSection.class);
		});
		GoogleIdTokenTest.this.officeFloor = compiler.compileAndOpenOfficeFloor();

		// Load the verifier
		CompileOfficeFloor.invokeProcess(this.officeFloor, "VERIFY.service", null);

		// Ensure created verifier
		assertNotNull("Should create verifier", CaptureSection.googleIdTokenVerifier);
		return CaptureSection.googleIdTokenVerifier;
	}

	public static class CaptureSection {

		private static GoogleIdTokenVerifier googleIdTokenVerifier;

		public void service(@Parameter String token, GoogleIdTokenVerifier verifier) throws Exception {
			googleIdTokenVerifier = verifier;
		}
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
					verifier.addOfficeManagedObject("VERIFIER", ManagedObjectScope.THREAD);

					// Add section to handle
					context.addSection("VERIFY", VerifySection.class);
				});
				GoogleIdTokenTest.this.officeFloor = compiler.compileAndOpenOfficeFloor();

				// Undertake verifying the token
				CompileOfficeFloor.invokeProcess(GoogleIdTokenTest.this.officeFloor, "VERIFY.service", token);
				assertNotNull("Ensure have token", VerifySection.googleToken);
				validator.accept(VerifySection.googleToken);
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
