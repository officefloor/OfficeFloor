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

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectParserServiceFactory;
import net.officefloor.web.json.JacksonJsonTest.InputObject;

/**
 * Mock {@link HttpObjectParserServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpObjectParser implements HttpObjectParserServiceFactory, HttpObjectParserFactory {

	/*
	 * ===================== HttpObjectParserServiceFactory ===================
	 */

	@Override
	public HttpObjectParserFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== HttpObjectParserFactory =======================
	 */

	@Override
	public String getContentType() {
		return "test/mock";
	}

	@Override
	public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception {
		return new HttpObjectParser<T>() {

			@Override
			public String getContentType() {
				return MockHttpObjectParser.this.getContentType();
			}

			@Override
			public Class<T> getObjectType() {
				return objectClass;
			}

			@Override
			@SuppressWarnings("unchecked")
			public T parse(ServerHttpConnection connection) throws HttpException {
				return (T) new InputObject("INPUT");
			}
		};
	}

}
