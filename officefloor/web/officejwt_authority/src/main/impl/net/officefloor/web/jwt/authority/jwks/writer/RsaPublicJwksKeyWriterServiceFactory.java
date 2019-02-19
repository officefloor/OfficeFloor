package net.officefloor.web.jwt.authority.jwks.writer;

import java.security.Key;
import java.security.interfaces.RSAPublicKey;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriter;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriterContext;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriterServiceFactory;

/**
 * {@link RSAPublicKey} {@link JwksKeyWriterServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class RsaPublicJwksKeyWriterServiceFactory implements JwksKeyWriterServiceFactory, JwksKeyWriter<RSAPublicKey> {

	/*
	 * ==================== JwksKeyWriterServiceFactory =====================
	 */

	@Override
	public JwksKeyWriter<RSAPublicKey> createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ============================ JwksKeyWriter ===========================
	 */

	@Override
	public boolean canWriteKey(Key key) {
		return key instanceof RSAPublicKey;
	}

	@Override
	public void writeKey(JwksKeyWriterContext<RSAPublicKey> context) throws Exception {

		// Write the key details
		RSAPublicKey key = context.getKey();
		context.setKty("RSA");
		context.setBase64("n", key.getModulus());
		context.setBase64("e", key.getPublicExponent());
	}

}