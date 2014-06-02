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
package net.officefloor.plugin.jndi.ldap;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;

/**
 * Validates the LDAP functionality for using as a Credential Store.
 * 
 * @author Daniel Sagenschneider
 */
public class CredentialStoreTest extends AbstractLdapTest {

	@Override
	protected void setUp() throws Exception {
		this.setupLdap();
	}

	/**
	 * Ensure able to obtain credentials.
	 */
	public void testObtainCredentials() throws Exception {

		final Charset ASCII = Charset.forName("ASCII");

		// Calculate the expected credential
		String expectedRaw = "daniel:officefloor:password";
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(expectedRaw.getBytes(ASCII));
		byte[] expectedBytes = digest.digest();
		String expectedCredentials = Base64.encodeBase64String(expectedBytes)
				.trim();

		// Obtain the context
		DirContext context = this.ldap.getDirContext();

		// Obtain the People context
		DirContext people = (DirContext) context
				.lookup("ou=People,dc=officefloor,dc=net");
		assertNotNull("Should have People context", people);

		// Search for person
		NamingEnumeration<SearchResult> results = people.search("",
				"(&(objectClass=inetOrgPerson)(uid=daniel))", null);
		assertTrue("Expecting to find daniel entry", results.hasMore());
		SearchResult result = results.next();
		assertFalse("Should only have the daniel entry", results.hasMore());

		// Obtain the digest MD5 credentials for Daniel
		String digestMd5Credential = null;
		Attributes attributes = result.getAttributes();
		Attribute passwordAttribute = attributes.get("userPassword");
		for (NamingEnumeration<?> enumeration = passwordAttribute.getAll(); enumeration
				.hasMore();) {
			byte[] credentials = (byte[]) enumeration.next();
			String text = new String(credentials, Charset.forName("ASCII"));

			// Determine if MD5 credential
			if (text.toUpperCase().startsWith("{MD5}")) {
				// Found MD5 credential
				digestMd5Credential = text.substring("{MD5}".length());
			}
		}
		assertNotNull("Must have digest MD5 credential", digestMd5Credential);

		// Ensure correct credentials
		assertEquals("Incorrect DIGEST MD5 credentials", expectedCredentials,
				digestMd5Credential);
	}

	/**
	 * Ensure able to obtain the roles.
	 */
	public void testObtainRoles() throws Exception {

		// Obtain the context
		DirContext context = this.ldap.getDirContext();

		// Obtain the People context
		DirContext people = (DirContext) context
				.lookup("ou=People,dc=officefloor,dc=net");
		assertNotNull("Should have People context", people);

		// Search for person
		NamingEnumeration<SearchResult> personResults = people.search("",
				"(&(objectClass=inetOrgPerson)(uid=daniel))", null);
		assertTrue("Expecting to find daniel entry", personResults.hasMore());
		SearchResult daniel = personResults.next();
		assertFalse("Should only have the daniel entry", personResults
				.hasMore());

		// Obtain the Groups context
		DirContext groups = (DirContext) context
				.lookup("ou=Groups,dc=officefloor,dc=net");
		assertNotNull("Should have Groups context", groups);

		// Search for groups containing daniel
		String danielDn = daniel.getNameInNamespace();
		NamingEnumeration<SearchResult> groupResults = groups.search("",
				"(&(objectClass=groupOfNames)(member=" + danielDn + "))", null);

		// Obtain the listing of roles for daniel
		List<String> roles = new ArrayList<String>(2);
		for (; groupResults.hasMore();) {
			SearchResult group = groupResults.next();

			// Obtain the role from the group
			String role = (String) group.getAttributes().get("ou").get();

			// Add role to listing
			roles.add(role);
		}

		// Ensure the correct roles
		assertEquals("Incorrect number of roles", 2, roles.size());
		assertTrue("Missing user role", roles.contains("developer"));
		assertTrue("Missing developer role", roles.contains("committer"));
	}

}