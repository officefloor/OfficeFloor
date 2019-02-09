package net.officefloor.web.jwt.key;

import java.security.KeyPair;
import java.security.interfaces.RSAKey;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * {@link RSAKey} {@link AsynchronousKeyFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class Rsa256AynchronousKeyFactory implements AsynchronousKeyFactory {

	/*
	 * =============== AsynchronousKeyFactory ===============
	 */

	@Override
	public KeyPair createAsynchronousKeyPair() throws Exception {
		return Keys.keyPairFor(SignatureAlgorithm.RS256);
	}

}