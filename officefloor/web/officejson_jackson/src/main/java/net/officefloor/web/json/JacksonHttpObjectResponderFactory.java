/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.web.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;

/**
 * Jackson {@link HttpObjectResponderFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class JacksonHttpObjectResponderFactory implements HttpObjectResponderFactory {

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
	}

	/**
	 * <code>Content-Type</code>.
	 */
	private static final HttpHeaderValue contentType = new HttpHeaderValue("application/json");

	/**
	 * Error message prefix.
	 */
	private static final byte[] ERROR_MESSAGE_PREFIX = "{\"error\":"
			.getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

	/**
	 * Error message suffix.
	 */
	private static final byte[] ERROR_MESSAGE_SUFFIX = "}".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

	/*
	 * ============== HttpObjectResponderFactory ===============
	 */

	@Override
	public String getContentType() {
		return contentType.getValue();
	}

	@Override
	public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {

		// Create the type for efficient execution
		JavaType javaType = mapper.constructType(objectType);

		// Determine if can deserialise type
		if (!mapper.canDeserialize(javaType)) {
			return null;
		}

		// Return the object responder
		return new HttpObjectResponder<T>() {

			@Override
			public String getContentType() {
				return contentType.getValue();
			}

			@Override
			public Class<T> getObjectType() {
				return objectType;
			}

			@Override
			public void send(T object, ServerHttpConnection connection) throws IOException {
				HttpResponse response = connection.getResponse();
				response.setContentType(contentType, null);
				mapper.writeValue(response.getEntity(), object);
			}
		};
	}

	@Override
	public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {

		// Return the object responder
		return new HttpObjectResponder<E>() {

			@Override
			public String getContentType() {
				return contentType.getValue();
			}

			@Override
			public Class<E> getObjectType() {
				return escalationType;
			}

			@Override
			public void send(E escalation, ServerHttpConnection connection) throws IOException {
				HttpResponse response = connection.getResponse();
				response.setContentType(contentType, null);
				ServerOutputStream output = response.getEntity();
				output.write(ERROR_MESSAGE_PREFIX);

				// Write the error detail
				String message = escalation.getMessage();
				if ((message == null) || (message.trim().length() == 0)) {
					message = escalation.getClass().getSimpleName();
				}
				mapper.writeValue(output, message);

				output.write(ERROR_MESSAGE_SUFFIX);
			}
		};
	}

}