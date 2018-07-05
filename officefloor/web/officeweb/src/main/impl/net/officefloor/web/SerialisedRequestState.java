/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link Serializable} {@link HttpRequest} state.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisedRequestState implements Serializable {

	/**
	 * Generates identifier for {@link SerialisedRequestState}.
	 */
	private static final AtomicInteger identity = new AtomicInteger(0);

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Identifier for this {@link SerialisedRequestState}.
	 */
	public final int identifier;

	/**
	 * {@link HttpRequestState} momento.
	 */
	public final Serializable momento;

	/**
	 * Instantiate.
	 * 
	 * @param momento
	 *            Momento.
	 */
	public SerialisedRequestState(Serializable momento) {
		this.momento = momento;

		// Generate identifier
		this.identifier = identity.incrementAndGet();
	}

}