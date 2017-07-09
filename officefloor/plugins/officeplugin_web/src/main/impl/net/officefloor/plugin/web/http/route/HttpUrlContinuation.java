/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.route;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * HTTP URL continuation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuation {

	/**
	 * Saves the request state into the {@link HttpSession} to allow being
	 * reinstated later (possibly by a new {@link ProcessState}).
	 * 
	 * @param attributeKey
	 *            Key to store the state with the {@link HttpSession}.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param requestState
	 *            {@link HttpRequestState}.
	 * @param session
	 *            {@link HttpSession}.
	 * @throws IOException
	 *             If fails to save the request state.
	 */
	public static void saveRequest(String attributeKey, ServerHttpConnection connection, HttpRequestState requestState,
			HttpSession session) throws IOException {

		// Obtain the request state momento
		Serializable connectionMomento = connection.exportState();
		Serializable requestStateMomento = requestState.exportState();
		RequestStateMomento redirectMomento = new RequestStateMomento(connectionMomento, requestStateMomento);

		// Store the request state momento
		session.setAttribute(attributeKey, redirectMomento);
	}

	/**
	 * Reinstates the request.
	 * 
	 * @param attributeKey
	 *            Key to the request state with the {@link HttpSession}.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param requestState
	 *            {@link HttpRequestState}.
	 * @param session
	 *            {@link HttpSession}.
	 * @return <code>true</code> should the request be reinstated.
	 *         <code>false</code> indicating the state could not be found within
	 *         the {@link HttpSession}.
	 * @throws IOException
	 *             If fails to reinstate the request.
	 */
	public static boolean reinstateRequest(String attributeKey, ServerHttpConnection connection,
			HttpRequestState requestState, HttpSession session) throws IOException {

		// Obtain the request state momento
		RequestStateMomento requestMomento = (RequestStateMomento) session.getAttribute(attributeKey);

		// Attempt to reinstate request
		boolean isReinstated = false;
		if (requestMomento != null) {
			// Import redirect state
			connection.importState(requestMomento.connectionMomento);
			requestState.importState(requestMomento.requestStateMomento);

			// Request reinstated
			isReinstated = true;
		}

		// Return whether reinstated
		return isReinstated;
	}

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * Indicates if secure. May be <code>null</code> to indicate service either
	 * way.
	 */
	private final Boolean isSecure;

	/**
	 * Initiate.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param isSecure
	 *            Indicates if secure. May be <code>null</code> to indicate
	 *            service either way.
	 */
	public HttpUrlContinuation(String functionName, Boolean isSecure) {
		this.functionName = functionName;
		this.isSecure = isSecure;
	}

	/**
	 * Obtains the name of the {@link ManagedFunction} to service the URL
	 * continuation.
	 * 
	 * @return {@link ManagedFunction} name.
	 */
	public String getFunctionName() {
		return this.functionName;
	}

	/**
	 * Indicates if the URL continuation requires a secure
	 * {@link ServerHttpConnection}.
	 * 
	 * @return <code>true</code> should a secure {@link ServerHttpConnection} be
	 *         required for the URL continuation. May return <code>null</code>
	 *         to service regardless.
	 */
	public Boolean isSecure() {
		return this.isSecure;
	}

	/**
	 * Momento containing state of the request.
	 */
	private static class RequestStateMomento implements Serializable {

		/**
		 * {@link ServerHttpConnection} momento.
		 */
		private final Serializable connectionMomento;

		/**
		 * {@link HttpRequestState} momento.
		 */
		private final Serializable requestStateMomento;

		/**
		 * Initiate.
		 * 
		 * @param connectionMomento
		 *            {@link ServerHttpConnection} momento.
		 * @param requestStateMomento
		 *            {@link HttpRequestState} momento.
		 */
		public RequestStateMomento(Serializable connectionMomento, Serializable requestStateMomento) {
			this.connectionMomento = connectionMomento;
			this.requestStateMomento = requestStateMomento;
		}
	}

}