/*-
 * #%L
 * JSON default for Web
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.frame.api.escalate.Escalation;
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
	 * Obtains the entity for the {@link Escalation}.
	 * 
	 * @param escalation {@link Throwable} {@link Escalation}.
	 * @param mapper     {@link ObjectMapper} to write entity.
	 * @return Entity for the {@link Escalation}.
	 * @throws IOException If fails to write {@link Escalation}.
	 */
	public static String getEntity(Throwable escalation, ObjectMapper mapper) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		writeError(buffer, escalation, initiateObjectMapper(mapper));
		return buffer.toString();
	}

	/**
	 * Initialises the {@link ObjectMapper}.
	 * 
	 * @param mapper {@link ObjectMapper}.
	 * @return Initialised {@link ObjectMapper}.
	 */
	private static ObjectMapper initiateObjectMapper(ObjectMapper mapper) {

		// Always disable close on finish
		mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

		// Return the mapper for use
		return mapper;
	}

	/**
	 * Writes the {@link Escalation}.
	 * 
	 * @param output     {@link OutputStream} to write the {@link Escalation}.
	 * @param escalation {@link Throwable} {@link Escalation}.
	 * @throws IOException If fails to write.
	 */
	private static void writeError(OutputStream output, Throwable escalation, ObjectMapper mapper) throws IOException {
		output.write(ERROR_MESSAGE_PREFIX);

		// Write the error detail
		String message = escalation.getMessage();
		if ((message == null) || (message.trim().length() == 0)) {
			message = escalation.getClass().getSimpleName();
		}
		mapper.writeValue(output, message);

		output.write(ERROR_MESSAGE_SUFFIX);
	}

	/**
	 * {@link ObjectMapper}.
	 */
	private final ObjectMapper mapper;

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

	/**
	 * Initiate with the {@link ObjectMapper}.
	 * 
	 * @param mapper {@link ObjectMapper}.
	 */
	public JacksonHttpObjectResponderFactory(ObjectMapper mapper) {
		this.mapper = initiateObjectMapper(mapper);
	}

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
		JavaType javaType = this.mapper.constructType(objectType);

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
				JacksonHttpObjectResponderFactory.this.mapper.writeValue(response.getEntity(), object);
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
				writeError(output, escalation, JacksonHttpObjectResponderFactory.this.mapper);
			}
		};
	}

}
