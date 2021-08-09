/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server.http.parse;

import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.UsAsciiUtil;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.test.StressTest;

/**
 * Performance test the {@link HttpRequestParser}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParsingPerformanceTest {

	/**
	 * Number of iterations
	 */
	private static final int ITERATIONS = 1000000;

	/**
	 * Name of test.
	 */
	private String testName;

	@BeforeEach
	public void setup(TestInfo testInfo) {
		this.testName = testInfo.getDisplayName();
	}

	/**
	 * Tests a simple GET.
	 */
	@StressTest
	public void simpleGet() {
		this.doPerformance(1, "GET / HTTP/1.1\n\n");
	}

	/**
	 * Tests a simple POST.
	 */
	@StressTest
	public void simplePost() {
		this.doPerformance(1, "POST / HTTP/1.1\nContent-Length: 4\n\nTEST");
	}

	/**
	 * More realistic real world GET request
	 */
	@StressTest
	public void realWorldGet() {
		this.doPerformance(1, "GET /plaintext HTTP/1.1\n" + "Host: server\n"
				+ "User-Agent: Mozilla/5.0 (X11; Linux x86_64) Gecko/20130501 Firefox/30.0 AppleWebKit/600.00 Chrome/30.0.0000.0 Trident/10.0 Safari/600.00\n"
				+ "Cookie: uid=12345678901234567890; __utma=1.1234567890.1234567890.1234567890.1234567890.12; wd=2560x1600\n"
				+ "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n"
				+ "Accept-Language: en-US,en;q=0.5\n" + "Connection: keep-alive" + "\n\n");
	}

	/**
	 * Tests the performance of parsing {@link HttpRequest}.
	 */
	public void doPerformance(int requestCount, String wireContent) {

		// Create data to parse
		byte[] data = UsAsciiUtil.convertToHttp(wireContent);

		// Create a buffer with the content
		try (MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocateDirect(data.length))) {
			StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer();
			buffer.write(data);

			// Create another buffer (so treats as new data)
			StreamBuffer<ByteBuffer> separator = pool.getPooledStreamBuffer();

			// Create the parser
			HttpRequestParser parser = new HttpRequestParser(new HttpRequestParserMetaData(100, 100, 100));

			// Run warm up
			for (int i = 0; i < (ITERATIONS / 100); i++) {
				parser.appendStreamBuffer(buffer);
				for (int j = 0; j < requestCount; j++) {
					if (!parser.parse()) {
						fail("Failed to parse request on iteration " + i + " request " + j);
					}
				}
				parser.appendStreamBuffer(separator);
			}

			// Run test
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < ITERATIONS; i++) {
				parser.appendStreamBuffer(buffer);
				for (int j = 0; j < requestCount; j++) {
					if (!parser.parse()) {
						fail("Failed to parse request on iteration " + i + " request " + j);
					}
				}
				parser.appendStreamBuffer(separator);
			}
			long endTime = System.currentTimeMillis();

			// Log time
			final String format = "%1$20s";
			long numberOfRequests = ITERATIONS * requestCount;
			long runTime = Math.max(1, endTime - startTime);
			long requestsPerSecond = (long) (((double) numberOfRequests) / ((double) (runTime) / (double) 1000.0));
			System.out.println(String.format(format, this.testName) + String.format(format, numberOfRequests)
					+ " requests " + String.format(format, runTime) + " milliseconds "
					+ String.format(format, requestsPerSecond) + " / second");
		}
	}

}
