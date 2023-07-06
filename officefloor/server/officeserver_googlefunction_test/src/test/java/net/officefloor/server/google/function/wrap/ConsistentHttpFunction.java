package net.officefloor.server.google.function.wrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import net.officefloor.server.http.HttpStatus;

/**
 * Ensures consistent execution of {@link HttpFunction}.
 */
public class ConsistentHttpFunction implements HttpFunction {

	/*
	 * ================== HttpFunction =====================
	 */

	@Override
	public void service(HttpRequest request, HttpResponse response) throws Exception {

		// Obtain how to handle
		String testType = request.getFirstQueryParameter("test").orElse(null);
		if (testType == null) {

			// Validate optional values
			WriteLine writer = new WriteLine(response.getWriter());
			writer.write("Query", request.getQuery().orElse("NULL"));
			writer.write("Content-Type", request.getContentType().orElse("NULL"));
			writer.write("Character Encoding", request.getCharacterEncoding().orElse("NULL"));
			writer.write("Content-Length", String.valueOf(request.getContentLength()));
			writer.write("Entity", request.getReader());

		} else {
			switch (testType) {

			case "request":
				WriteLine writer = new WriteLine(response.getWriter());
				writer.write("URI", request.getUri());
				writer.write("Method", request.getMethod());
				writer.write("Query", request.getQuery().orElse("N/A"));
				writer.write("Path", request.getPath());
				writer.write("Headers", request.getHeaders(), "Accept-Encoding", "Connection", "Host", "User-Agent");
				writer.write("Content-Type", request.getContentType().orElse("NULL"));
				writer.write("Character Encoding", request.getCharacterEncoding().orElse("NULL"));
				writer.write("Content-Length", String.valueOf(request.getContentLength()));
				writer.write("Entity", request.getReader());
				break;

			case "request_inputstream":
				InputStream input = request.getInputStream();
				OutputStream output = response.getOutputStream();
				for (int byteValue = input.read(); byteValue != -1; byteValue = input.read()) {
					output.write(byteValue);
				}
				break;

			case "response_status":
				response.setStatusCode(HttpStatus.EXPECTATION_FAILED.getStatusCode());
				break;

			case "response_statusMessage":
				response.setStatusCode(HttpStatus.ACCEPTED.getStatusCode(), "Test Status Message");
				break;

			case "response_contentType":
				assertTrue(response.getContentType().isEmpty(), "Should be no content-type");
				final String contentType = "application/json";
				response.setContentType(contentType);
				assertEquals(contentType, response.getContentType().get(), "Should have content-type");
				response.getWriter().append("{}");
				break;

			case "response_header":
				response.appendHeader("Test-Header", "Test-Value");
				String testHeaderName = response.getHeaders().keySet().stream()
						.filter((name) -> "Test-Header".equalsIgnoreCase(name)).findFirst().get();
				assertEquals("Test-Value", response.getHeaders().get(testHeaderName).get(0), "Should have header");
				response.getWriter().append("Avoid 204 status");
				break;

			case "response_multipleHeaders":
				for (String headerName : new String[] { "Header-One", "Header-Two", "Header-Three" }) {
					for (String headerValue : new String[] { "0", "1", "2" }) {
						response.appendHeader(headerName, headerValue);
					}
				}
				String headerOneName = response.getHeaders().keySet().stream()
						.filter((name) -> "Header-One".equalsIgnoreCase(name)).findFirst().get();
				for (int i = 0; i < 3; i++) {
					assertEquals(String.valueOf(i), response.getHeaders().get(headerOneName).get(i),
							"Incorrect header " + i);
				}
				response.getWriter().append("Avoid 204 status");
				break;

			case "response_entity":
				response.getWriter().append("Hello via function");
				break;

			default:
				throw new IllegalStateException("Unknown test type: " + testType);
			}
		}
	}

	/**
	 * Wrapper to write line.
	 */
	private static class WriteLine {

		/**
		 * {@link Writer}.
		 */
		private final Writer writer;

		/**
		 * Instantiate.
		 * 
		 * @param writer {@link Writer}.
		 */
		private WriteLine(Writer writer) {
			this.writer = writer;
		}

		/**
		 * Write line.
		 * 
		 * @param line Text line to write.
		 * @throws IOException If fails to write line.
		 */
		private void writeLine(String line) throws IOException {
			writer.append(line);
			writer.append("\n");
		}

		/**
		 * Writes the name/value.
		 * 
		 * @param name  Name.
		 * @param value Value.
		 * @throws IOException If fails to write name/value.
		 */
		private void write(String name, String value) throws IOException {
			writer.append(name);
			writer.append(": ");
			writer.append(value);
			writer.append("\n");
		}

		/**
		 * Writes the name/values.
		 * 
		 * @param heading    Heading to name/values.
		 * @param nameValues Name/values.
		 * @throws IOException If fails to write name/values.
		 */
		private void write(String heading, Map<String, List<String>> nameValues, String... ignoreNames)
				throws IOException {
			this.writeLine("------- " + heading + " -------");
			Set<String> ignoreNameSet = new HashSet<>(Arrays.asList(ignoreNames));
			List<String> orderedNames = new ArrayList<>(nameValues.keySet());
			Collections.sort(orderedNames, String.CASE_INSENSITIVE_ORDER);
			NEXT_NAME: for (String name : orderedNames) {
				if (ignoreNameSet.contains(name)) {
					continue NEXT_NAME;
				}
				List<String> values = nameValues.get(name);
				for (String value : values) {
					this.write(name.toLowerCase(), value);
				}
			}
			this.writeLine("-------------------");
		}

		/**
		 * Writes the content of {@link Reader}.
		 * 
		 * @param name   Name.
		 * @param reader {@link Reader} for value.
		 * @throws IOException If fails to write.
		 */
		private void write(String name, Reader reader) throws IOException {
			StringWriter content = new StringWriter();
			for (int character = reader.read(); character != -1; character = reader.read()) {
				content.write(character);
			}
			this.write(name, content.toString());
		}
	}

}