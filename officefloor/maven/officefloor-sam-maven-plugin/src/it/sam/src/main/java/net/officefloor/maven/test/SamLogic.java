package net.officefloor.maven.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;

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

	public void file(ServerHttpConnection connection) throws IOException {
		FileChannel file = FileChannel.open(Paths.get("./src/test/resources/file.txt"));
		connection.getResponse().getEntity().write(file, null);
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