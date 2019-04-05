/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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