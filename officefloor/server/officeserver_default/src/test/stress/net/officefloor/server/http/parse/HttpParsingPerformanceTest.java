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
import net.officefloor.server.http.mock.MockBufferPool;
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
	 * Tests a simple POST.
	 */
	public void testSimplePost() {
		this.doPerformance(1, "POST / HTTP/1.1\nContent-Length: 4\n\nTEST");
	}

	/**
	 * Tests the performance of parsing {@link HttpRequest}.
	 */
	public void doPerformance(int requestCount, String wireContent) {

		// Create data to parse
		byte[] data = UsAsciiUtil.convertToHttp(wireContent);

		// Create a buffer with the content
		MockBufferPool pool = new MockBufferPool(() -> ByteBuffer.allocateDirect(data.length));
		StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer();
		buffer.write(data);

		// Create another buffer (so treats as new data)
		StreamBuffer<ByteBuffer> separator = pool.getPooledStreamBuffer();

		// Create the parser
		HttpRequestParser parser = new HttpRequestParser(new HttpRequestParserMetaData(100, 100, 100));

		// Run warm up
		for (int i = 0; i < 1000000; i++) {
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
		long numberOfRequests = ITERATIONS * requestCount;
		long runTime = endTime - startTime;
		System.out.println(
				this.getName() + " completed " + numberOfRequests + " requests in " + runTime + " milliseconds");
	}

}