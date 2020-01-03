package net.officefloor.web.json;

import static org.junit.Assert.fail;

import java.io.IOException;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.HttpObjectResponderServiceFactory;

/**
 * Mock {@link HttpObjectResponderServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpObjectResponder implements HttpObjectResponderServiceFactory, HttpObjectResponderFactory {

	/*
	 * ===================== HttpObjectResponderServiceFactory =====================
	 */

	@Override
	public HttpObjectResponderFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ========================= HttpObjectResponderFactory ========================
	 */

	@Override
	public String getContentType() {
		return "test/mock";
	}

	@Override
	public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
		return new HttpObjectResponder<T>() {

			@Override
			public String getContentType() {
				return MockHttpObjectResponder.this.getContentType();
			}

			@Override
			public Class<T> getObjectType() {
				return objectType;
			}

			@Override
			public void send(T object, ServerHttpConnection connection) throws IOException {
				connection.getResponse().getEntityWriter().write("MOCK");
			}
		};
	}

	@Override
	public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {
		fail("Should not be escalation");
		return null;
	}

}