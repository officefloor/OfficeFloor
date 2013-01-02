/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.security.store;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.web.http.security.store.CredentialEntry;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.store.JndiLdapCredentialStore;

import org.apache.commons.codec.binary.Base64;

/**
 * Tests the {@link JndiLdapCredentialStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiLdapCredentialStoreTest extends OfficeFrameTestCase {

	/**
	 * Algorithm.
	 */
	private final String ALGORITHM = "MD5";

	/**
	 * {@link Charset} for storing credentials.
	 */
	private final Charset US_ASCII = HttpRequestParserImpl.US_ASCII;

	/**
	 * {@link Context}.
	 */
	private final DirContext context = this.createMock(DirContext.class);

	/**
	 * {@link CredentialStore} to test.
	 */
	private final CredentialStore store = new JndiLdapCredentialStore(
			ALGORITHM, this.context, "ou=People,dc=officefloor,dc=net",
			"ou=Groups,dc=officefloor,dc=net");

	/**
	 * Ensure correct algorithm.
	 */
	public void testAlgorithm() {
		assertEquals("Incorrect algorithm", ALGORITHM, this.store
				.getAlgorithm());
	}

	/**
	 * Ensure correct credentials.
	 */
	@SuppressWarnings("unchecked")
	public void testCredentials() throws Exception {

		// Create the expected credentials
		final String expectedRaw = "daniel:officefloor:password";
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(expectedRaw.getBytes(US_ASCII));
		final byte[] expectedCredentials = digest.digest();

		// Obtain the encoded credentials
		final String encodedCredentials = Base64.encodeBase64String(
				expectedCredentials).trim();
		assertEquals("Incorrect encoded credentials",
				"msu723GSLovbwuaPnaLcnQ==", encodedCredentials);

		// Mocks
		final NamingEnumeration<SearchResult> searchResults = this
				.createMock(NamingEnumeration.class);
		final Attributes attributes = this.createMock(Attributes.class);
		final Attribute attribute = this.createMock(Attribute.class);
		final NamingEnumeration<?> userPasswords = this
				.createMock(NamingEnumeration.class);

		// Objects
		final SearchResult searchResult = new SearchResult("uid=daniel", null,
				attributes);
		searchResult
				.setNameInNamespace("uid=daniel,ou=People,dc=officefloor,dc=net");

		// Record
		this.recordReturn(this.context, this.context.search(
				"ou=People,dc=officefloor,dc=net",
				"(&(objectClass=inetOrgPerson)(uid=daniel))", null),
				searchResults);
		this.recordReturn(searchResults, searchResults.hasMore(), true);
		this.recordReturn(searchResults, searchResults.next(), searchResult);
		this.recordReturn(this.context, this.context
				.getAttributes("uid=daniel,ou=People,dc=officefloor,dc=net"),
				attributes);
		this
				.recordReturn(attributes, attributes.get("userPassword"),
						attribute);
		this.recordReturn(attribute, attribute.getAll(), userPasswords);
		this.recordReturn(userPasswords, userPasswords.hasMore(), true);
		this.recordReturn(userPasswords, userPasswords.next(),
				"Plain Text Password".getBytes(US_ASCII));
		this.recordReturn(userPasswords, userPasswords.hasMore(), true);
		this.recordReturn(userPasswords, userPasswords.next(),
				("{MD5}" + encodedCredentials).getBytes(US_ASCII));

		// Test
		this.replayMockObjects();
		CredentialEntry entry = this.store.retrieveCredentialEntry("daniel",
				"REALM");
		byte[] actualCredentials = entry.retrieveCredentials();
		this.verifyMockObjects();

		// Validate correct value
		assertEquals("Incorrect credential byte length",
				expectedCredentials.length, actualCredentials.length);
		for (int i = 0; i < expectedCredentials.length; i++) {
			assertEquals("Incorrect credential byte " + i,
					expectedCredentials[i], actualCredentials[i]);
		}
	}

	/**
	 * Ensure correct roles.
	 */
	@SuppressWarnings("unchecked")
	public void testRoles() throws Exception {

		// Mocks
		final NamingEnumeration<SearchResult> searchResults = this
				.createMock(NamingEnumeration.class);
		final Attributes attributes = this.createMock(Attributes.class);
		final Attribute attribute = this.createMock(Attribute.class);

		// Objects
		final SearchResult searchResult = new SearchResult("uid=daniel", null,
				attributes);
		searchResult
				.setNameInNamespace("uid=daniel,ou=People,dc=officefloor,dc=net");

		// Record obtaining the Credential Entry
		this.recordReturn(this.context, this.context.search(
				"ou=People,dc=officefloor,dc=net",
				"(&(objectClass=inetOrgPerson)(uid=daniel))", null),
				searchResults);
		this.recordReturn(searchResults, searchResults.hasMore(), true);
		this.recordReturn(searchResults, searchResults.next(), searchResult);

		// Record obtaining the Groups
		this
				.recordReturn(
						this.context,
						this.context
								.search(
										"ou=Groups,dc=officefloor,dc=net",
										"(&(objectClass=groupOfNames)"
												+ "(member=uid=daniel,ou=People,dc=officefloor,dc=net))",
										null), searchResults);
		this.recordReturn(searchResults, searchResults.hasMore(), true);
		this.recordReturn(searchResults, searchResults.next(),
				new SearchResult("cn=developers", null, attributes));
		this.recordReturn(attributes, attributes.get("ou"), attribute);
		this.recordReturn(attribute, attribute.get(), "developer");
		this.recordReturn(searchResults, searchResults.hasMore(), true);
		this.recordReturn(searchResults, searchResults.next(),
				new SearchResult("cn=founders", null, attributes));
		this.recordReturn(attributes, attributes.get("ou"), attribute);
		this.recordReturn(attribute, attribute.get(), "founder");
		this.recordReturn(searchResults, searchResults.hasMore(), false);

		// Test
		this.replayMockObjects();
		CredentialEntry entry = this.store.retrieveCredentialEntry("daniel",
				"REALM");
		Set<String> roles = entry.retrieveRoles();
		this.verifyMockObjects();

		// Ensure correct roles
		assertEquals("Incorrect number of roles", 2, roles.size());
		assertTrue("Must have developer role", roles.contains("developer"));
		assertTrue("Must have founder role", roles.contains("founder"));
	}

}