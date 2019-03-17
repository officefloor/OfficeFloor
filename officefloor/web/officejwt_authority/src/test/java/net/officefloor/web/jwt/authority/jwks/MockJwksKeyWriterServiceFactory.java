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