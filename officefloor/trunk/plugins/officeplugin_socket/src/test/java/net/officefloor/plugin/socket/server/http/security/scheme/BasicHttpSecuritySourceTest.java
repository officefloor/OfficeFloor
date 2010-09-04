/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.socket.server.http.security.scheme;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.security.scheme.BasicHttpSecuritySource.Dependencies;
import net.officefloor.plugin.socket.server.http.security.store.CredentialStore;

/**
 * Tests the {@link BasicHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class BasicHttpSecuritySourceTest extends
		AbstractHttpSecuritySourceTest<Dependencies> {

	/**
	 * Realm for testing.
	 */
	private static final String REALM = "WallyWorld";

	/**
	 * {@link CredentialStore}.
	 */
	private final CredentialStore store = this
			.createMock(CredentialStore.class);

	/**
	 * Initiate.
	 */
	public BasicHttpSecuritySourceTest() {
		super(BasicHttpSecuritySource.class, "Basic");

		// Provide values for testing
		this.recordReturn(this.context, this.context
				.getProperty(BasicHttpSecuritySource.PROPERTY_REALM), REALM);
		this.context.requireDependency(Dependencies.CREDENTIAL_STORE,
				CredentialStore.class);
	}

	@Override
	protected void loadDependencies(Map<Dependencies, Object> dependencies) {
		dependencies.put(Dependencies.CREDENTIAL_STORE, this.store);
	}

	/**
	 * Tests creating the challenge.
	 */
	public void testChallenge() throws Exception {
		this.doChallenge("realm=\"" + REALM + "\"");
	}

	/**
	 * Ensure can authenticate.
	 */
	public void testSimpleAuthenticate() throws Exception {
		this.recordReturn(this.store, this.store.retrieveCredentials("Aladdin",
				REALM), "open sesame".getBytes(HttpRequestParserImpl.US_ASCII));
		this.recordReturn(this.store, this.store
				.retrieveRoles("Aladdin", REALM), new HashSet<String>(Arrays
				.asList("prince")));
		this
				.doAuthenticate("QWxhZGRpbjpvcGVuIHNlc2FtZQ==", "Aladdin",
						"prince");
	}

	/**
	 * Ensure can extra spacing.
	 */
	public void testExtraSpacing() throws Exception {
		this.recordReturn(this.store, this.store.retrieveCredentials("Aladdin",
				REALM), "open sesame".getBytes(HttpRequestParserImpl.US_ASCII));
		this.recordReturn(this.store, this.store
				.retrieveRoles("Aladdin", REALM), new HashSet<String>(Arrays
				.asList("prince")));
		this.doAuthenticate("    QWxhZGRpbjpvcGVuIHNlc2FtZQ==   ", "Aladdin",
				"prince");
	}

	/**
	 * Ensure not authenticated if not provide credentials.
	 */
	public void testNoCredentials() throws Exception {
		HttpSecurity security = this.doAuthenticate("");
		assertNull("Should not be authenticated", security);
	}

	/**
	 * Ensure handle invalid Base64 encoding.
	 */
	public void testInvalidBase64() throws Exception {
		HttpSecurity security = this.doAuthenticate("wrong");
		assertNull("Should not be authenticated", security);
	}

}