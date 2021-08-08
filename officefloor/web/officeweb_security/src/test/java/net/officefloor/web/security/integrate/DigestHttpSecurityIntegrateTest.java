/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.integrate;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Properties;

import org.apache.commons.codec.binary.Hex;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.scheme.DigestHttpSecuritySource;
import net.officefloor.web.security.store.PasswordFileManagedObjectSource;

/**
 * Integrate the {@link DigestHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DigestHttpSecurityIntegrateTest extends AbstractHttpSecurityIntegrateTestCase {

	/**
	 * Realm.
	 */
	private static final String REALM = "TestRealm";

	@Override
	protected HttpSecurityBuilder configureHttpSecurity(CompileWebContext context,
			HttpSecurityArchitect securityArchitect) {

		// Configure the HTTP Security
		HttpSecurityBuilder security = securityArchitect.addHttpSecurity("SECURITY",
				DigestHttpSecuritySource.class.getName());
		security.addProperty(DigestHttpSecuritySource.PROPERTY_REALM, REALM);
		security.addProperty(DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY, "PrivateKey");

		// Obtain the password file
		String passwordFilePath;
		try {
			passwordFilePath = this.findFile(this.getClass(), "digest-password-file.txt").getAbsolutePath();
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Password File Credential Store
		OfficeManagedObjectSource passwordFileMos = context.getOfficeArchitect()
				.addOfficeManagedObjectSource("CREDENTIAL_STORE", PasswordFileManagedObjectSource.class.getName());
		passwordFileMos.addProperty(PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH, passwordFilePath);
		passwordFileMos.addOfficeManagedObject("CREDENTIAL_STORE", ManagedObjectScope.PROCESS);

		// Return the HTTP Security
		return security;
	}

	/**
	 * Ensure can integrate.
	 */
	public void testIntegration() throws Exception {

		// Should not authenticate (without credentials)
		this.doRequest("/service", 401, "");

		// Should authenticate with credentials
		MockHttpResponse init = this.doInit("/service");
		MockHttpResponse complete = this.doComplete("/service", init, "daniel", "password");
		complete.assertResponse(200, "Serviced for daniel");
	}

	/**
	 * Ensure can logout.
	 */
	public void testLogout() throws Exception {

		// Authenticate with credentials
		MockHttpResponse init = this.doInit("/service");
		MockHttpResponse complete = this.doComplete("/service", init, "daniel", "password");
		complete.assertResponse(200, "Serviced for daniel");

		// Request again to ensure stay logged in
		this.doRequest("/service", init, 200, "Serviced for daniel");

		// Logout
		this.doRequest("/logout", init, 200, "LOGOUT");

		// Should require to log in (after the log out)
		this.doRequest("/service", init, 401, "");
	}

	/**
	 * Undertakes the initial digest request.
	 * 
	 * @param path
	 *            Path.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse doInit(String path) {

		// Undertake the request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest(path));

		// Ensure challenge
		assertEquals("Should be challenge", 401, response.getStatus().getStatusCode());

		// Return response
		return response;
	}

	/**
	 * Completes the digest request.
	 * 
	 * @param path
	 *            Path.
	 * @param init
	 *            Initialise digest {@link MockHttpResponse}.
	 * @param username
	 *            User name.
	 * @param password
	 *            Password.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse doComplete(String path, MockHttpResponse init, String username, String password)
			throws Exception {

		// Obtain the init details
		String wwwAuthenticate = init.getHeader("www-authenticate").getValue();
		String propertiesText = wwwAuthenticate.replace(",", "");
		propertiesText = propertiesText.replaceAll("\"", "");
		propertiesText = propertiesText.replace(" ", "\n");
		Properties properties = new Properties();
		properties.load(new StringReader(propertiesText));

		// Ensure digest properties available
		assertEquals("Incorrect realm", "TestRealm", properties.getProperty("realm"));
		assertEquals("Incorrect algorithm", "MD5", properties.getProperty("algorithm"));
		assertEquals("Incorrect qop (with above , replacement)", "authauth-int", properties.getProperty("qop"));
		assertNotNull("Should have nonce", properties.getProperty("nonce"));
		assertNotNull("Should have opaque", properties.getProperty("opaque"));

		// Generate the authentication header
		String realm = properties.getProperty("realm");
		String nonce = properties.getProperty("nonce");
		String uri = path;
		String qop = "auth";
		String nc = "00000001";
		String cnonce = "0a4f113b";
		String opaque = properties.getProperty("opaque");

		// Calculate authentication details
		Charset charset = Charset.forName("UTF-8");
		MessageDigest digest = MessageDigest.getInstance("MD5");
		String ha1 = Hex.encodeHexString(digest.digest((username + ":" + realm + ":" + password).getBytes(charset)));
		String ha2 = Hex.encodeHexString(digest.digest(("GET:" + uri).getBytes(charset)));
		String response = Hex.encodeHexString(
				digest.digest((ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":auth:" + ha2).getBytes(charset)));

		// Undertake the request
		String authetication = "Digest username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce
				+ "\", uri=\"" + uri + "\", qop=\"" + qop + "\", nc=\"" + nc + "\", cnonce=\"" + cnonce
				+ "\", response=\"" + response + "\", opaque=\"" + opaque + "\"";
		MockHttpResponse httpResponse = this.server
				.send(MockHttpServer.mockRequest(path).header("authorization", authetication).cookies(init));

		// Return the response
		return httpResponse;
	}

}
