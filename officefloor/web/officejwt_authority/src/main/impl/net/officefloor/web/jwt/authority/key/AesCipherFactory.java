package net.officefloor.web.jwt.authority.key;

import javax.crypto.Cipher;

import net.officefloor.web.jwt.authority.key.CipherFactory;

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