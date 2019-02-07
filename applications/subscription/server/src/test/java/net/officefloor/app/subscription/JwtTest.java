package net.officefloor.app.subscription;

import static org.junit.Assert.assertEquals;

import java.security.Key;
import java.security.KeyPair;

import org.junit.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Ensure able to create JWT.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtTest {

	@Test
	public void symetricKey() {

		// Create the JWT
		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		String jws = Jwts.builder().setSubject("Daniel").signWith(key).compact();
		System.out.println("JWS: " + jws);

		// Secure the JWT
		String subject = Jwts.parser().setSigningKey(key).parseClaimsJws(jws).getBody().getSubject();
		assertEquals("Incorrect subject", "Daniel", subject);
	}

	@Test
	public void asymetricKey() {
		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
		String jws = Jwts.builder().setSubject("Daniel").signWith(keyPair.getPrivate()).compact();
		System.out.println("JWS: " + jws);

		// Secure the JWT
		String subject = Jwts.parser().setSigningKey(keyPair.getPublic()).parseClaimsJws(jws).getBody().getSubject();
		assertEquals("Incorrect subject", "Daniel", subject);
	}

}