package net.officefloor.web.jwt.authority.jwks.writer;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriter;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriterContext;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriterServiceFactory;

/**
 * {@link SecretKeySpec} {@link JwksKeyWriterServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class SecretJwksKeyWriterServiceFactory implements JwksKeyWriterServiceFactory, JwksKeyWriter<SecretKeySpec> {

	/*
	 * ==================== JwksKeyWriterServiceFactory =====================
	 */

	@Override
	public JwksKeyWriter<SecretKeySpec> createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ============================ JwksKeyWriter ===========================
	 */

	@Override
	public boolean canWriteKey(Key key) {
		return key instanceof SecretKeySpec;
	}

	@Override
	public void writeKey(JwksKeyWriterContext<SecretKeySpec> context) throws Exception {

		// Write the key details
		SecretKeySpec key = context.getKey();
		context.setKty("oct");
		context.setString("alg", key.getAlgorithm());
		context.setBase64("k", key.getEncoded());
	}

}