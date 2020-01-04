/*-
 * #%L
 * Web Plug-in
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
