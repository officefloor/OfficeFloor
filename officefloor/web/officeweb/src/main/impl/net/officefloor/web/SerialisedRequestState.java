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