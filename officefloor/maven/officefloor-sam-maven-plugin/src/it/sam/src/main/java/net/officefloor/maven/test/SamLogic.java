/*-
 * #%L
 * OfficeFloor SAM Maven Plugin
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.maven.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponseCookies;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Logic of SAM application.
 * 
 * @author Daniel Sagenschneider
 */
public class SamLogic {

	public static final String PROPERTY_ENV = "OFFICEFLOOR_TEST";

	public static void get(ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write("GET");
	}

	public static void post(ServerHttpConnection connection) throws IOException {
		try (Reader reader = new InputStreamReader(connection.getRequest().getEntity())) {
			try (Writer writer = connection.getResponse().getEntityWriter()) {
				for (int character = reader.read(); character != -1; character = reader.read()) {
					writer.write(character);
				}
			}
		}
	}

	public void headers(ServerHttpConnection connection) {
		for (HttpHeader header : connection.getRequest().getHeaders()) {
			connection.getResponse().getHeaders().addHeader(header.getName(), header.getValue());
		}
	}

	public void cookie(ServerHttpConnection connection) {
		HttpResponseCookies cookies = connection.getResponse().getCookies();
		cookies.setCookie("ONE", "1");
	}

	public void buffer(ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntity().write(ByteBuffer.wrap("BUFFER".getBytes(Charset.forName("UTF-8"))));
	}

	public void async(AsynchronousFlow async, ServerHttpConnection connection) {
		new Thread(() -> {

			try {
				Thread.sleep(1); // ensure less chance of immediate return
			} catch (InterruptedException ex) {
				// carry on
			}

			async.complete(() -> connection.getResponse().getEntityWriter().write("ASYNC"));
		}).start();
	}

	public void pathParameters(@HttpPathParameter("paramOne") String paramOne,
			@HttpPathParameter("paramTwo") String paramTwo, ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write(paramOne + "-" + paramTwo);
	}

	public void queryParameters(@HttpQueryParameter("one") String paramOne, @HttpQueryParameter("two") String paramTwo,
			ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write(paramOne + "-" + paramTwo);
	}

	public void json(ObjectResponse<Message> response) {
		response.send(new Message("TEST"));
	}

	public void env(ServerHttpConnection connection) throws IOException {
		String envValue = System.getenv(PROPERTY_ENV);
		connection.getResponse().getEntityWriter().write(envValue);
	}

	public void dynamoPost(@HttpPathParameter("id") String id, Message message, DynamoDBMapper mapper,
			ServerHttpConnection connection) {
		mapper.save(new MessageEntity(id, message.getMessage()));
		connection.getResponse().setStatus(HttpStatus.CREATED);
	}

	public void dynamoGet(@HttpPathParameter("id") String id, DynamoDBMapper mapper, ObjectResponse<Message> response) {
		MessageEntity entity = mapper.load(MessageEntity.class, id);
		response.send(new Message(entity.getMessage()));
	}

}
