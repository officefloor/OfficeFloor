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

package net.officefloor.web.jwt.authority.jwks;

import java.security.Key;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link JwksKeyWriterServiceFactory} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockJwksKeyWriterServiceFactory
		implements JwksKeyWriterServiceFactory, JwksKeyWriter<MockJwksKeyWriterServiceFactory.MockKey> {

	/**
	 * {@link MockKey}.
	 */
	public static MockKey MOCK_KEY = new MockKey();

	/**
	 * Mock {@link Key}.
	 */
	public static class MockKey implements Key {
		private static final long serialVersionUID = 1L;

		@Override
		public String getAlgorithm() {
			return "MOCK";
		}

		@Override
		public String getFormat() {
			return "MOCK";
		}

		@Override
		public byte[] getEncoded() {
			return new byte[0];
		}
	}

	/*
	 * ==================== JwksKeyWriterServiceFactory =====================
	 */

	@Override
	public JwksKeyWriter<MockKey> createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ============================ JwksKeyWriter ===========================
	 */

	@Override
	public boolean canWriteKey(Key key) {
		return key instanceof MockKey;
	}

	@Override
	public void writeKey(JwksKeyWriterContext<MockKey> context) throws Exception {
		context.setKty("MOCK");
		context.setString("mock", "mocked");
	}

}
