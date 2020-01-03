package net.officefloor.web.security.scheme;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.scheme.HttpCredentialsImpl;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * Tests the {@link HttpCredentialsImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpCredentialsTest extends OfficeFrameTestCase {

	/**
	 * Ensure provides values.
	 */
	public void testWithValues() {
		this.doTest("daniel", "password");
	}

	/**
	 * Ensure can handle <code>null</code> values.
	 */
	public void testWithNull() {
		this.doTest(null, null);
	}

	/**
	 * Ensure can have blank values.
	 */
	public void testWithBlanks() {
		this.doTest("", "");
	}

	/**
	 * Ensure can construct with <code>null</code> values.
	 */
	public void doTest(String username, String password) {

		// Create the HTTP credentials
		HttpCredentials credentials = new HttpCredentialsImpl(username, password);

		// Validate the credentials
		assertEquals("Incorrect username", username, credentials.getUsername());
		if (password == null) {
			assertNull("Should not have password", credentials.getPassword());
		} else {
			String actualPassword = new String(credentials.getPassword(), AbstractHttpSecuritySource.UTF_8);
			assertEquals("Incorrect password", password, actualPassword);
		}
	}

}