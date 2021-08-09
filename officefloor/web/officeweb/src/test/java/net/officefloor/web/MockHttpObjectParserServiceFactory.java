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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.AbstractWebArchitectTest.RegisteredObject;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectParserServiceFactory;

/**
 * Mock {@link HttpObjectParserServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpObjectParserServiceFactory<O>
		implements HttpObjectParserServiceFactory, HttpObjectParserFactory, HttpObjectParser<O> {

	/*
	 * ==================== HttpObjectParserServiceFactory ==================
	 */

	@Override
	public HttpObjectParserFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= HttpObjectParserFactory ======================
	 */

	@Override
	public String getContentType() {
		return "registered/object";
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception {
		if (!RegisteredObject.class.equals(objectClass)) {
			return null; // not handled
		}
		return (HttpObjectParser<T>) this;
	}

	/*
	 * ======================= HttpObjectParserFactory ======================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Class<O> getObjectType() {
		return (Class<O>) RegisteredObject.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public O parse(ServerHttpConnection connection) throws HttpException {
		try {
			Reader content = new InputStreamReader(connection.getClientRequest().getEntity());
			StringWriter buffer = new StringWriter();
			for (int character = content.read(); character != -1; character = content.read()) {
				buffer.write(character);
			}
			return (O) new RegisteredObject(buffer.toString());
		} catch (IOException ex) {
			throw new HttpException(ex);
		}
	}

}
