/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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

import java.io.IOException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;

/**
 * Jackson {@link HttpObjectParserFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class JacksonHttpObjectParserFactory implements HttpObjectParserFactory {

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/*
	 * ================= HttpObjectParserFactory ===============
	 */

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public <T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception {

		// Create the type for efficient execution
		JavaType javaType = mapper.constructType(objectClass);

		// Determine if can deserialise type
		if (!mapper.canDeserialize(javaType)) {
			return null;
		}

		// Can deserialise, so provide parser
		return new HttpObjectParser<T>() {

			@Override
			public String getContentType() {
				return JacksonHttpObjectParserFactory.this.getContentType();
			}

			@Override
			public Class<T> getObjectType() {
				return objectClass;
			}

			@Override
			@SuppressWarnings("unchecked")
			public T parse(ServerHttpConnection connection) throws HttpException {
				try {
					return (T) mapper.readValue(connection.getRequest().getEntity(), javaType);
				} catch (IOException ex) {
					throw new HttpException(ex);
				}
			}
		};
	}

}