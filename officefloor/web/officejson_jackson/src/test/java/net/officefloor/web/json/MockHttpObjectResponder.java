/*-
 * #%L
 * JSON Jackson Plug-in for Web
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
