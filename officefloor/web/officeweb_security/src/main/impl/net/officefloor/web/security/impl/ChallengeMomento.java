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
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

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
	 * @param connection   Momento for the {@link ServerHttpConnection}.
	 * @param requestState Momento for the {@link HttpRequestState}.
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