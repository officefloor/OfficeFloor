/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.session.store;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.spi.CreateHttpSessionOperation;
import net.officefloor.web.session.spi.HttpSessionStore;
import net.officefloor.web.session.spi.InvalidateHttpSessionOperation;
import net.officefloor.web.session.spi.RetrieveHttpSessionOperation;
import net.officefloor.web.session.spi.StoreHttpSessionOperation;

/**
 * <p>
 * {@link HttpSessionStore} that contains state of the {@link HttpSession}
 * within memory.
 * <p>
 * This is a very basic implementation that keeps all {@link HttpSession}
 * instances in memory. Though this may provide a very fast solution it suffers
 * from issues such as:
 * <ol>
 * <li>{@link OutOfMemoryError} if too many/large {@link HttpSession}
 * instances</li>
 * <li>state is not shared with other JVMs (especially for clustered
 * environments)</li>
 * </ol>
 * <p>
 * This is useful in light load testing environments (such as unit tests).
 * 
 * @author Daniel Sagenschneider
 */
public class MemoryHttpSessionStore implements HttpSessionStore {

	/**
	 * {@link Clock} for determining {@link HttpSession} times.
	 */
	private final Clock clock;

	/**
	 * Maximum idle time measured in milliseconds before expiring the
	 * {@link HttpSession}.
	 */
	private final long maxIdleTime;

	/**
	 * {@link SessionState} instances by their Session Id.
	 */
	private final Map<String, SessionState> sessions = new HashMap<String, SessionState>();

	/**
	 * Next time to run an expire.
	 */
	private Instant nextExpireTime;

	/**
	 * Initiate.
	 * 
	 * @param clock
	 *            {@link Clock} for determining {@link HttpSession} times.
	 * @param maxIdleTime
	 *            Maximum idle time in seconds before expiring the
	 *            {@link HttpSession}.
	 */
	public MemoryHttpSessionStore(Clock clock, int maxIdleTime) {
		this.clock = clock;
		this.maxIdleTime = (maxIdleTime * 1000);
		this.nextExpireTime = this.clock.instant().plus(this.maxIdleTime, ChronoUnit.MILLIS);
	}

	/**
	 * Expires idle {@link SessionState} instances based on current time.
	 * 
	 * @param currentTime
	 *            Current time.
	 */
	private void expireIdleSessions(Instant currentTime) {

		// Determine if time for another expire run
		if (currentTime.isAfter(this.nextExpireTime)) {

			// Expire all idle session
			for (Iterator<Entry<String, SessionState>> iterator = this.sessions.entrySet().iterator(); iterator
					.hasNext();) {
				SessionState session = iterator.next().getValue();

				// Determine if session is idle and requires expiring
				if (currentTime.isAfter(session.expireTime)) {
					// Expire the session
					iterator.remove();
				}
			}

			// Indicate time of next expire run
			this.nextExpireTime = currentTime.plus(this.maxIdleTime, ChronoUnit.MILLIS);
		}
	}

	/*
	 * =================== HttpSessionStore =============================
	 */

	@Override
	public void createHttpSession(CreateHttpSessionOperation operation) {

		// Create the new Session in anticipation of adding.
		// Improves concurrency performance doing outside locks.
		Instant currentTime = this.clock.instant();
		SessionState session = new SessionState(currentTime, new HashMap<String, Serializable>(),
				currentTime.plus(this.maxIdleTime, ChronoUnit.MILLIS));

		// Obtain the Session Id
		String sessionId = operation.getSessionId();

		// Register the new Session
		synchronized (this.sessions) {
			// Expire any idle Sessions
			this.expireIdleSessions(currentTime);

			// Determine if register the session
			if (this.sessions.containsKey(sessionId)) {
				// Collision in Session Id
				session = null;
			} else {
				// Register the Session
				this.sessions.put(sessionId, session);
			}
		}

		// Handle result of registering the new Session
		if (session == null) {
			// Collision
			operation.sessionIdCollision();
		} else {
			// Session created and registered
			operation.sessionCreated(session.creationTime, session.expireTime, session.attributes);
		}
	}

	@Override
	public void retrieveHttpSession(RetrieveHttpSessionOperation operation) {

		// Determine extended expiry time as Session active
		Instant extendedExpiryTime = this.clock.instant().plus(this.maxIdleTime, ChronoUnit.MILLIS);

		// Obtain the Session State
		String sessionId = operation.getSessionId();

		// Obtain session (minimum operation in lock)
		SessionState session;
		synchronized (this.sessions) {
			session = this.sessions.get(sessionId);

			// Determine if extend expiry time
			if ((session != null) && (extendedExpiryTime.isAfter(session.expireTime))) {
				// Extend the expiry time (as active)
				session = new SessionState(session.creationTime, session.attributes, extendedExpiryTime);
				this.sessions.put(sessionId, session);
			}
		}

		// Determine if available
		if (session == null) {
			// Session not available
			operation.sessionNotAvailable();
		} else {
			// Have Session State so return for Session
			operation.sessionRetrieved(session.creationTime, session.expireTime, session.attributes);
		}
	}

	@Override
	public void storeHttpSession(StoreHttpSessionOperation operation) {

		// Create new Session State for storing.
		// (Handles if expired while using Session)
		String sessionId = operation.getSessionId();
		Instant creationTime = operation.getCreationTime();
		Instant expireTime = operation.getExpireTime();
		Map<String, Serializable> attributes = operation.getAttributes();
		SessionState session = new SessionState(creationTime, attributes, expireTime);

		// Store Session State
		synchronized (this.sessions) {
			this.sessions.put(sessionId, session);
		}

		// Indicate Session stored
		operation.sessionStored();
	}

	@Override
	public void invalidateHttpSession(InvalidateHttpSessionOperation operation) {

		// Obtain Session Id
		String sessionId = operation.getSessionId();

		// Remove the Session
		synchronized (this.sessions) {
			this.sessions.remove(sessionId);
		}

		// Indicate Session invalidated
		operation.sessionInvalidated();
	}

	/**
	 * Contains the state of a {@link HttpSession}.
	 */
	private class SessionState {

		/**
		 * Creation time.
		 */
		public final Instant creationTime;

		/**
		 * Attributes.
		 */
		public final Map<String, Serializable> attributes;

		/**
		 * Time that this {@link SessionState} will be expired.
		 */
		public final Instant expireTime;

		/**
		 * Initiate.
		 * 
		 * @param creationTime
		 *            Creation time.
		 * @param attributes
		 *            Attributes.
		 * @param expireTime
		 *            Time that this {@link SessionState} will be expired.
		 */
		public SessionState(Instant creationTime, Map<String, Serializable> attributes, Instant expireTime) {
			this.creationTime = creationTime;
			this.attributes = attributes;
			this.expireTime = expireTime;
		}
	}

}
