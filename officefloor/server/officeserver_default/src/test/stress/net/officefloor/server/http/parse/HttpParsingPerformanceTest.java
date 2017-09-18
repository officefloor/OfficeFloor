/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http.parse;

import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.UsAsciiUtil;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Performance test the {@link HttpRequestParser}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParsingPerformanceTest extends OfficeFrameTestCase {

	/**
	 * Number of iterations
	 */
	private static final int ITERATIONS = 1000000;

	/**
	 * Tests a simple GET.
	 */
	public void testSimpleGet() {
		this.doPerformance(1, "GET / HTTP/1.1\n\n");
	}

	/**
	 * Tests a simple POST.
	 */
	public void testSimplePost() {
		this.doPerformance(1, "POST / HTTP/1.1\nContent-Length: 4\n\nTEST");
	}

	/**
	 * More realistic real world GET request
	 */
	public void testRealWorldGet() {
		this.doPerformance(1,
				"GET /plaintext HTTP/1.1\n" + "Host: server\n"
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
		MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocateDirect(data.length));
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
		System.out.println(String.format(format, this.getName()) + String.format(format, numberOfRequests)
				+ " requests " + String.format(format, runTime) + " milliseconds "
				+ String.format(format, requestsPerSecond) + " / second");
	}

}