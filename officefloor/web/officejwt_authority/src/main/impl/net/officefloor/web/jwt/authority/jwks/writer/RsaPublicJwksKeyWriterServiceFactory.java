/*-
 * #%L
 * JWT Authority
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
