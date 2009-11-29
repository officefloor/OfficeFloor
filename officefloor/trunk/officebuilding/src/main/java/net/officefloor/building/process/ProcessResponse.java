/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.process;

import java.io.Serializable;

/**
 * Response from the {@link Process}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessResponse implements Serializable {

	/**
	 * Correlating {@link ProcessRequest} id.
	 */
	private final long correlatingRequestId;

	/**
	 * Command response.
	 */
	private final Object response;

	/**
	 * Command failure.
	 */
	private final Throwable failure;

	/**
	 * Initiate.
	 * 
	 * @param correlatingRequestId
	 *            Correlating {@link ProcessRequest} id.
	 * @param response
	 *            Command response.
	 * @param failure
	 *            Command failure.
	 */
	public ProcessResponse(long correlatingRequestId, Object response,
			Throwable failure) {
		this.correlatingRequestId = correlatingRequestId;
		this.response = response;
		this.failure = failure;
	}

	/**
	 * Obtains the correlating {@link ProcessRequest} id.
	 * 
	 * @return Correlating {@link ProcessRequest} id.
	 */
	public long getCorrelatingRequestId() {
		return this.correlatingRequestId;
	}

	/**
	 * Obtains the command response.
	 * 
	 * @return Command response.
	 */
	public Object getResponse() {
		return this.response;
	}

	/**
	 * Obtains the command failure.
	 * 
	 * @return Command failure.
	 */
	public Throwable getFailure() {
		return this.failure;
	}

}