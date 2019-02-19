package net.officefloor.web.jwt.authority.key;

import java.security.Key;

import javax.crypto.KeyGenerator;

import net.officefloor.web.jwt.authority.key.SynchronousKeyFactory;

/**
 * AES {@link SynchronousKeyFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class AesSynchronousKeyFactory implements SynchronousKeyFactory {

	@Override
	public Key createSynchronousKey() throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(256);
		return kgen.generateKey();
	}

}