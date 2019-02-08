package net.officefloor.web.jwt.authority;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the {@link JwtAuthority} implementation for refresh tokens.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtAuthorityRefreshTokenTest extends AbstractJwtAuthorityTokenTest {

	/**
	 * Ensure the encrypt/decrypt works as expected.
	 */
	public void testAes() throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		String message = mapper.writeValueAsString(new MockClaims());

		// Generate the random values
		String initVector = JwtAuthorityManagedObjectSource.randomString(16, 16);
		String startSalt = JwtAuthorityManagedObjectSource.randomString(5, 25);
		String endSalt = JwtAuthorityManagedObjectSource.randomString(5, 25);
		String lace = JwtAuthorityManagedObjectSource.randomString(80, 100);
		System.out.println("Init Vector: " + initVector + " (" + initVector.length() + "), Start Salt: " + startSalt
				+ ", Lace: " + lace + " (" + lace.length() + ")," + "(" + startSalt.length() + "), End Salt: " + endSalt
				+ "(" + endSalt.length() + ")");

		// Encrypt and decrypt
		String encrypted = JwtAuthorityManagedObjectSource.encrypt(refreshKey, initVector, startSalt, lace, endSalt,
				message);
		String decrypted = JwtAuthorityManagedObjectSource.decrypt(refreshKey, initVector, startSalt, endSalt,
				encrypted);

		// Indicate values
		System.out.println("encrypted: " + encrypted + "\ndecrypted: " + decrypted);
		assertEquals("Should decrypt to plain text", message, decrypted);
	}

	/**
	 * Ensure able to generate refresh token.
	 */
	public void testCreateRefreshToken() {
		String refreshToken = this.createRefreshToken();
		this.identity.assertRefreshToken(refreshToken, this.mockRefreshKeys.get(0));
	}

}