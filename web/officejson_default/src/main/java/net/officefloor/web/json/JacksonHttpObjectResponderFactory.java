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

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.web.build.HttpEscalationResponder;
import net.officefloor.web.build.HttpEscalationResponderContext;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderContext;
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
		writeError(buffer, escalation, mapper);
		return buffer.toString();
	}

	/**
	 * Wraps an {@link OutputStream} to suppress close, preventing Jackson from closing the HTTP response stream.
	 */
	private static OutputStream nonClosing(OutputStream out) {
		return new OutputStream() {
			@Override public void write(int b) throws IOException { out.write(b); }
			@Override public void write(byte[] b, int off, int len) throws IOException { out.write(b, off, len); }
			@Override public void write(byte[] b) throws IOException { out.write(b); }
			@Override public void flush() throws IOException { out.flush(); }
			@Override public void close() {}
		};
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
		mapper.writeValue(nonClosing(output), message);

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
		this.mapper = mapper;
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

		// Return the object responder
		return new HttpObjectResponder<T>() {

			@Override
			public String getContentType() {
				return contentType.getValue();
			}

			@Override
			public void send(HttpObjectResponderContext<T> context) throws IOException {
				HttpResponse response = context.getServerHttpConnection().getResponse();
				response.setContentType(contentType, null);
				JacksonHttpObjectResponderFactory.this.mapper.writeValue(nonClosing(response.getEntity()), context.getResponseObject());
			}
		};
	}

	@Override
	public <E extends Throwable> HttpEscalationResponder<E> createHttpEscalationResponder(Class<E> escalationType,
																						  boolean isOfficeFloorEscalation) {

		// Leave OfficeFloor to handle its escalations
		if (isOfficeFloorEscalation) {
			return null;
		}

		// Return the object responder
		return new HttpEscalationResponder<E>() {

			@Override
			public String getContentType() {
				return contentType.getValue();
			}

			@Override
			public void send(HttpEscalationResponderContext<E> context) throws IOException {
				HttpResponse response = context.getServerHttpConnection().getResponse();
				response.setContentType(contentType, null);
				ServerOutputStream output = response.getEntity();
				writeError(output, context.getEscalation(), JacksonHttpObjectResponderFactory.this.mapper);
			}
		};
	}

}
