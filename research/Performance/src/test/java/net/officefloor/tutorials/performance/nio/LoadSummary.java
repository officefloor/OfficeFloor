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
 * Summary of the {@link Load}
 * 
 * @author Daniel Sagenschneider
 */
public class LoadSummary {

	/**
	 * Description of the {@link Load}.
	 */
	private final String description;

	/**
	 * Number of {@link Connection} instances.
	 */
	private final int connectionCount;

	/**
	 * Number of failed {@link Connection} instances.
	 */
	private final int failedConnectionCount;

	/**
	 * {@link RequestSummary} instances for this {@link LoadSummary}.
	 */
	private final List<RequestSummary> requestSummaries = new ArrayList<RequestSummary>(
			2);

	/**
	 * Initiate.
	 * 
	 * @param description
	 *            Description of the {@link Load}.
	 */
	LoadSummary(String description, int connectionCount,
			int failedConnectionCount) {
		this.description = description;
		this.connectionCount = connectionCount;
		this.failedConnectionCount = failedConnectionCount;
	}

	/**
	 * Obtains the description of the {@link Load}.
	 * 
	 * @return Description of the {@link Load}.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Obtains the number of {@link Connection} instances.
	 * 
	 * @return Number of {@link Connection} instances.
	 */
	public int getConnectionCount() {
		return this.connectionCount;
	}

	/**
	 * Obtains the number of failed {@link Connection} instances.
	 * 
	 * @return Number of failed {@link Connection} instances.
	 */
	public int getFailedConnectionCount() {
		return this.failedConnectionCount;
	}

	/**
	 * Adds a {@link RequestSummary} for this {@link LoadSummary}.
	 * 
	 * @param requestUri
	 *            URI of the {@link Request}.
	 * @param servicedRequestCount
	 *            Number of {@link Request} instances serviced.
	 * @return {@link RequestSummary} added.
	 */
	public RequestSummary addRequest(String requestUri, int servicedRequestCount) {
		RequestSummary requestSummary = new RequestSummary(requestUri,
				servicedRequestCount);
		this.requestSummaries.add(requestSummary);
		return requestSummary;
	}

	/**
	 * Returns the listing of {@link RequestSummary} instances for this
	 * {@link LoadSummary}.
	 * 
	 * @return Listing of {@link RequestSummary} instances for this
	 *         {@link LoadSummary}.
	 */
	public List<RequestSummary> getRequestSummaries() {
		return this.requestSummaries;
	}

}