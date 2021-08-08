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

package net.officefloor.identity.google.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.identity.google.GoogleIdTokenVerifierManagedObjectSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.compile.WebCompileOfficeFloor;

/**
 * Tests the {@link GoogleIdTokenRule}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractGoogleIdTokenRuleTestCase {

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Obtains the {@link AbstractGoogleIdTokenJUnit}.
	 * 
	 * @return {@link AbstractGoogleIdTokenJUnit}.
	 */
	protected abstract AbstractGoogleIdTokenJUnit getGoogleIdToken();

	/**
	 * Ensure can use with {@link MockHttpServer}.
	 */
	public void mockHttpServer() throws Throwable {

		// Start the Server
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		Closure<MockHttpServer> server = new Closure<>();
		compiler.mockHttpServer((mockServer) -> server.value = mockServer);
		compiler.web((context) -> {

			// Add servicing
			context.link(false, "/", MockService.class);

			// Add the Google ID token verifier
			OfficeManagedObjectSource verifier = context.getOfficeArchitect().addOfficeManagedObjectSource("VERIFIER",
					GoogleIdTokenVerifierManagedObjectSource.class.getName());
			verifier.addProperty(GoogleIdTokenVerifierManagedObjectSource.PROPERTY_CLIENT_ID, "client@google");
			verifier.addOfficeManagedObject("VERIFIER", ManagedObjectScope.THREAD);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Validate Google token
			server.value
					.send(MockHttpServer.mockRequest("/?token=" + this.getGoogleIdToken().getMockIdToken("MOCK_ID",
							"test@officefloor.net", "photoUrl", "https://test.com/photo.png")))
					.assertResponse(200, mapper.writeValueAsString(
							new Token("MOCK_ID", "test@officefloor.net", true, "https://test.com/photo.png")));
		}
	}

	public static class MockService {
		public void service(@HttpQueryParameter("token") String idToken, GoogleIdTokenVerifier verifier,
				ServerHttpConnection connection) throws Exception {
			GoogleIdToken token = verifier.verify(idToken);
			mapper.writeValue(connection.getResponse().getEntityWriter(), new Token(token));
		}
	}

	public static class Token {
		public String subject;
		public String email;
		public boolean isEmailVerified;
		public String photoUrl;

		public Token(String subject, String email, boolean isEmailVerified, String photoUrl) {
			this.subject = subject;
			this.email = email;
			this.isEmailVerified = isEmailVerified;
			this.photoUrl = photoUrl;
		}

		public Token(GoogleIdToken token) {
			Payload payload = token.getPayload();
			this.subject = payload.getSubject();
			this.email = payload.getEmail();
			this.isEmailVerified = payload.getEmailVerified();
			this.photoUrl = (String) payload.getOrDefault("photoUrl", null);
		}
	}

}
