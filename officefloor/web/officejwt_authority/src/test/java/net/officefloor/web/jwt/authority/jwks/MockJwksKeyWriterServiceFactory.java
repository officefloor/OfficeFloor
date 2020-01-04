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
