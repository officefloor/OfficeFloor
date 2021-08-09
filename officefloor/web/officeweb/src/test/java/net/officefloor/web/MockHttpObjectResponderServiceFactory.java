/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web;

import java.io.IOException;
import java.io.Writer;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.AbstractWebArchitectTest.RegisteredResponse;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.HttpObjectResponderServiceFactory;

/**
 * {@link ServiceFactory} for the {@link HttpObjectResponderFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpObjectResponderServiceFactory
		implements HttpObjectResponderServiceFactory, HttpObjectResponderFactory {

	/**
	 * Indicates whether to include;
	 */
	public static boolean isInclude = true;

	/*
	 * =========== HttpObjectResponderServiceFactory ============
	 */

	@Override
	public HttpObjectResponderFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =============== HttpObjectResponderFactory ===============
	 */

	@Override
	public String getContentType() {
		return isInclude ? "registered/response" : null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
		if (!RegisteredResponse.class.equals(objectType)) {
			return null;
		}
		return (HttpObjectResponder<T>) new HttpObjectResponder<RegisteredResponse>() {

			@Override
			public String getContentType() {
				return MockHttpObjectResponderServiceFactory.this.getContentType();
			}

			@Override
			public Class<RegisteredResponse> getObjectType() {
				return RegisteredResponse.class;
			}

			@Override
			public void send(RegisteredResponse object, ServerHttpConnection connection) throws IOException {
				HttpResponse response = connection.getResponse();
				response.setContentType(this.getContentType(), null);
				Writer writer = response.getEntityWriter();
				writer.write("{ registered: ");
				writer.write(object.getContent());
				writer.write(" }");
			}
		};
	}

	@Override
	public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {
		return new HttpObjectResponder<E>() {

			@Override
			public String getContentType() {
				return MockHttpObjectResponderServiceFactory.this.getContentType();
			}

			@Override
			public Class<E> getObjectType() {
				return escalationType;
			}

			@Override
			public void send(E object, ServerHttpConnection connection) throws IOException {
				HttpResponse response = connection.getResponse();
				response.setContentType(this.getContentType(), null);
				Writer writer = response.getEntityWriter();
				writer.write("{ registeredError: ");
				writer.write(object.getMessage());
				writer.write(" }");
			}
		};
	}

}
