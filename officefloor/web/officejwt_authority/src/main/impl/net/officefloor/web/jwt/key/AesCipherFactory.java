package net.officefloor.web.jwt.key;

import javax.crypto.Cipher;

/**
 * AES {@link CipherFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class AesCipherFactory implements CipherFactory {

	@Override
	public Cipher createCipher() throws Exception {
		return Cipher.getInstance("AES/CBC/PKCS5PADDING");
	}

}