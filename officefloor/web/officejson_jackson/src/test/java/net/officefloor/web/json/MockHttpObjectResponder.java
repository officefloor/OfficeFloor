/*-
 * #%L
 * JSON Jackson Plug-in for Web
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
