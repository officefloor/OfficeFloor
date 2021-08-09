/*-
 * #%L
 * JWT Authority
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.web.jwt.authority.jwks.writer;

import java.security.Key;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriter;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriterContext;
import net.officefloor.web.jwt.authority.jwks.JwksKeyWriterServiceFactory;

/**
 * {@link RSAPrivateKey} {@link JwksKeyWriterServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class RsaPrivateJwksKeyWriterServiceFactory
		implements JwksKeyWriterServiceFactory, JwksKeyWriter<RSAPrivateCrtKey> {

	/*
	 * ==================== JwksKeyWriterServiceFactory =====================
	 */

	@Override
	public JwksKeyWriter<RSAPrivateCrtKey> createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ============================ JwksKeyWriter ===========================
	 */

	@Override
	public boolean canWriteKey(Key key) {
		return key instanceof RSAPrivateCrtKey;
	}

	@Override
	public void writeKey(JwksKeyWriterContext<RSAPrivateCrtKey> context) throws Exception {

		// Write the key details
		RSAPrivateCrtKey key = context.getKey();
		context.setKty("RSA");
		context.setBase64("n", key.getModulus());
		context.setBase64("e", key.getPublicExponent());
		context.setBase64("d", key.getPrivateExponent());
		context.setBase64("p", key.getPrimeP());
		context.setBase64("q", key.getPrimeQ());
		context.setBase64("dp", key.getPrimeExponentP());
		context.setBase64("dq", key.getPrimeExponentQ());
		context.setBase64("qi", key.getCrtCoefficient());
	}

}
