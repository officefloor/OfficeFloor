/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorials.performance.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Request.
 * 
 * @author Daniel Sagenschneider
 */
public class Request {

	/**
	 * Request URI.
	 */
	private final String requestUri;

	/**
	 * {@link ByteBuffer} containing the request for this {@link Load}.
	 */
	private final ByteBuffer data;

	/**
	 * Expected content of the response.
	 */
	private final String expectedResponseContent;

	/**
	 * Number of times to repeat this {@link Request} in the sequence.
	 */
	private final int repeatCount;

	/**
	 * Servicing times of the responses within an run interval.
	 */
	private final List<Long> serviceTimes = new ArrayList<Long>(250000);

	/**
	 * Initiate.
	 * 
	 * @param requestUri
	 *            Request URI.
	 * @param expectedResponseContent
	 *            Expected content of the response.
	 * @param repeatCount
	 *            Number of times to repeat this {@link Request} in the
	 *            sequence.
	 * @throws IOException
	 *             If fails to create request.
	 */
	public Request(String requestUri, String expectedResponseContent,
			int repeatCount) throws IOException {
		this.expectedResponseContent = expectedResponseContent;
		this.repeatCount = repeatCount;

		// Ensure uri begins with slash
		this.requestUri = (requestUri.startsWith("/") ? requestUri : "/"
				+ requestUri);

		// Create the request data
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(content, Connection.CHARSET);
		writer.write("GET " + this.requestUri
				+ " HTTP/1.1\r\nHost: test\r\n\r\n");
		writer.flush();
		this.data = ByteBuffer.allocateDirect(1024);
		this.data.put(content.toByteArray());
		this.data.flip();
	}

	/**
	 * Resets for next run interval.
	 */
	void reset() {
		this.serviceTimes.clear();
	}

	/**
	 * Obtains the request URI for this load.
	 * 
	 * @return Request URI for this load.
	 */
	String getRequestUri() {
		return this.requestUri;
	}

	/**
	 * Obtains the number of times to repeat this {@link Request} within the
	 * sequence.
	 * 
	 * @return Number of times to repeat this {@link Request} within the
	 *         sequence.
	 */
	int getRepeatCount() {
		return this.repeatCount;
	}

	/**
	 * Obtains the number of requests serviced.
	 * 
	 * @return Number of requests serviced.
	 */
	int getNumberOfRequestsServiced() {
		return this.serviceTimes.size();
	}

	/**
	 * Obtains the minimum servicing time that input percentage of requests were
	 * serviced within.
	 * 
	 * @param percentage
	 *            Percentage.
	 * @return Minimum servicing time that input percentage of requests were
	 *         serviced within.
	 */
	long getPercentileServiceTime(double percentage) {

		// Sort servicing times
		Collections.sort(this.serviceTimes);

		// Return the percentile service time
		if (this.serviceTimes.size() == 0) {
			return -1;
		} else {
			// Determine the index of service time
			int serviceTimeIndex = (int) (this.serviceTimes.size() * percentage);
			if (serviceTimeIndex >= this.serviceTimes.size()) {
				serviceTimeIndex = (serviceTimeIndex - 1); // last index
			}
			return this.serviceTimes.get(serviceTimeIndex);
		}
	}

	/**
	 * Obtains the {@link ByteBuffer} containing the request.
	 * 
	 * @return {@link ByteBuffer} containing the request.
	 */
	ByteBuffer getData() {
		return this.data;
	}

	/**
	 * Obtains the expected response content.
	 * 
	 * @return Expected response content.
	 */
	String getExpectedResponseContent() {
		return this.expectedResponseContent;
	}

	/**
	 * Records the servicing of a request.
	 * 
	 * @param requestStart
	 *            Time in nanoseconds of request start.
	 * @param requestEnd
	 *            Time in nanoseconds of request end.
	 * @throws IOException
	 *             If invalid time.
	 */
	void requestServiced(long requestStart, long requestEnd) throws IOException {

		// Determine the service time
		long serviceTime;
		if (requestStart > requestEnd) {
			throw new IOException("Time recording invalid [" + requestStart
					+ ", " + requestEnd + "]");
		}
		if (requestStart < 0) {
			if (requestEnd < 0) {
				// Both start and end time negative
				serviceTime = Math.abs(requestStart) - Math.abs(requestEnd);
			} else {
				// Start negative but end time positive
				serviceTime = Math.abs(requestStart) + requestEnd;
			}
		} else {
			// Both start and end time positive
			serviceTime = requestEnd - requestStart;
		}

		// Add the service time
		this.serviceTimes.add(Long.valueOf(serviceTime));
	}

}