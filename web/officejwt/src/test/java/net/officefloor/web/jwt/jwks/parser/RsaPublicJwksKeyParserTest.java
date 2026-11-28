/*-
 * #%L
 * JWT Security
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.web.jwt.jwks.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.officefloor.web.jwt.jwks.JwksKeyParserContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Tests the {@link RsaPublicJwksKeyParserServiceFactory}.
 * <p>
 * JWKS RSA values are unsigned big endian integers (RFC 7518, section 6.3.1).
 * A full length RSA modulus always has its top bit set, so it must not be read
 * as a signed number.
 *
 * @author Daniel Sagenschneider
 */
public class RsaPublicJwksKeyParserTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static KeyPair keyPair;

	private static RSAPublicKey publicKey;

	@BeforeAll
	public static void createKey() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		keyPair = generator.generateKeyPair();
		publicKey = (RSAPublicKey) keyPair.getPublic();
		assertTrue(publicKey.getModulus().testBit(2047), "Invalid test: modulus top bit should be set");
	}

	/**
	 * Ensure parses a JWKS key as published by standard identity providers (such
	 * as AWS Cognito): minimal unsigned bytes, unpadded.
	 */
	@Test
	public void standardUnsignedKey() throws Exception {
		Key key = parse(unsignedBase64(publicKey.getModulus()), unsignedBase64(publicKey.getPublicExponent()));
		assertValidKey(key);
	}

	/**
	 * Ensure still parses a JWKS key as written by OfficeFloor: signed bytes
	 * (leading zero byte when top bit set) and padded.
	 */
	@Test
	public void officeFloorSignedKey() throws Exception {
		Key key = parse(Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray()),
				Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray()));
		assertValidKey(key);
	}

	/**
	 * Ensure a value with its top bit set is read as positive.
	 */
	@Test
	public void topBitSetIsPositive() {
		JsonNode node = mapper.readTree("{\"v\":\"gA\"}"); // single byte 0x80
		JwksKeyParserContext context = () -> node;
		assertEquals(BigInteger.valueOf(128), context.getBase64BigInteger("v"));
	}

	/**
	 * Parses the RSA public key.
	 *
	 * @param n Base64 URL modulus.
	 * @param e Base64 URL exponent.
	 * @return Parsed {@link Key}.
	 */
	private static Key parse(String n, String e) throws Exception {
		JsonNode node = mapper.readTree("{\"kty\":\"RSA\",\"n\":\"" + n + "\",\"e\":\"" + e + "\"}");
		JwksKeyParserContext context = () -> node;
		return new RsaPublicJwksKeyParserServiceFactory().parseKey(context);
	}

	/**
	 * Asserts the parsed key is the public key, and verifies its signature.
	 *
	 * @param key Parsed {@link Key}.
	 */
	private static void assertValidKey(Key key) throws Exception {
		assertNotNull(key, "Should parse key");
		assertEquals(publicKey.getModulus(), ((RSAPublicKey) key).getModulus(), "Incorrect modulus");
		assertEquals(publicKey.getPublicExponent(), ((RSAPublicKey) key).getPublicExponent(), "Incorrect exponent");

		// Ensure verifies a signature
		byte[] data = "header.claims".getBytes(StandardCharsets.UTF_8);
		Signature signer = Signature.getInstance("SHA256withRSA");
		signer.initSign(keyPair.getPrivate());
		signer.update(data);
		byte[] signature = signer.sign();
		Signature verifier = Signature.getInstance("SHA256withRSA");
		verifier.initVerify((RSAPublicKey) key);
		verifier.update(data);
		assertTrue(verifier.verify(signature), "Should verify signature");
	}

	/**
	 * Encodes as RFC 7518 Base64urlUInt: minimal unsigned bytes, unpadded.
	 *
	 * @param value Value.
	 * @return Base64 URL value.
	 */
	private static String unsignedBase64(BigInteger value) {
		byte[] bytes = value.toByteArray();
		if ((bytes.length > 1) && (bytes[0] == 0)) {
			bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
		}
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

}
