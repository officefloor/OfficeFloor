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

import java.util.ArrayList;
import java.util.List;

/**
 * Summary of a {@link Request} for a {@link Load}.
 * 
 * @author Daniel Sagenschneider
 */
public class RequestSummary {

	/**
	 * URI of the {@link Request}.
	 */
	private final String requestUri;

	/**
	 * Number of {@link Request} instances serviced.
	 */
	private final int servicedRequestCount;

	/**
	 * Percentile service times for this {@link Request}.
	 */
	private final List<Long> percentileServiceTimes = new ArrayList<Long>(6);

	/**
	 * Initiate.
	 * 
	 * @param requestUri
	 *            URI of the {@link Request}.
	 * @param servicedRequestCount
	 *            Number of {@link Request} instances serviced.
	 */
	public RequestSummary(String requestUri, int servicedRequestCount) {
		this.requestUri = requestUri;
		this.servicedRequestCount = servicedRequestCount;
	}

	/**
	 * <p>
	 * Adds a percentile service time.
	 * <p>
	 * These are to be added in the order as per
	 * {@link Runner#getPecentileServiceTimes()}.
	 * 
	 * @param percentileServiceTime
	 *            Next percentile service time.
	 */
	void addPercentileServiceTime(long percentileServiceTime) {
		this.percentileServiceTimes.add(Long.valueOf(percentileServiceTime));
	}

	/**
	 * Obtains the URI of the {@link Request}.
	 * 
	 * @return URI of the {@link Request}.
	 */
	public String getRequestUri() {
		return this.requestUri;
	}

	/**
	 * Obtains the number of {@link Request} instances serviced.
	 * 
	 * @return Number of {@link Request} instances serviced.
	 */
	public int getServicedRequestCount() {
		return this.servicedRequestCount;
	}

	/**
	 * Obtains the percentile service times.
	 * 
	 * @return Percentile service times.
	 */
	public List<Long> getPercentileServiceTimes() {
		return this.percentileServiceTimes;
	}

}