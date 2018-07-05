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
package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.state.HttpRequestState;

/**
 * Challenge momento to reinstate the {@link ServerHttpConnection} and
 * {@link HttpRequestState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChallengeMomento implements Serializable {

	/**
	 * Momento for the {@link ServerHttpConnection}.
	 */
	private final Serializable connection;

	/**
	 * Momento for the {@link HttpRequestState}.
	 */
	private final Serializable requestState;

	/**
	 * Instantiate.
	 * 
	 * @param connection
	 *            Momento for the {@link ServerHttpConnection}.
	 * @param requestState
	 *            Momento for the {@link HttpRequestState}.
	 */
	public ChallengeMomento(Serializable connection, Serializable requestState) {
		this.connection = connection;
		this.requestState = requestState;
	}

	/**
	 * Obtains the momento for the {@link ServerHttpConnection}.
	 * 
	 * @return Momento for the {@link ServerHttpConnection}.
	 */
	public Serializable getServerHttpConnectionMomento() {
		return this.connection;
	}

	/**
	 * Obtains the momento for the {@link HttpRequestState}.
	 * 
	 * @return Momento for the {@link HttpRequestState}.
	 */
	public Serializable getHttpRequestStateMomento() {
		return this.requestState;
	}

}