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
package net.officefloor.plugin.socket.server.http.session.store;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.spi.CreateHttpSessionOperation;
import net.officefloor.plugin.socket.server.http.session.spi.HttpSessionStore;
import net.officefloor.plugin.socket.server.http.session.spi.InvalidateHttpSessionOperation;
import net.officefloor.plugin.socket.server.http.session.spi.RetrieveHttpSessionOperation;
import net.officefloor.plugin.socket.server.http.session.spi.StoreHttpSessionOperation;

/**
 * <p>
 * {@link HttpSessionStore} that contains state of the {@link HttpSession}
 * within memory.
 * <p>
 * This is a very basic implementation that keeps all {@link HttpSession}
 * instances in memory. Though this may provide a very fast solution it suffers
 * from issues such as:
 * <ol>
 * <li>{@link OutOfMemoryError} if too many/large {@link HttpSession} instances</li>
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
	private long nextExpireTime;

	/**
	 * Initiate.
	 *
	 * @param maxIdleTime
	 *            Maximum idle time in seconds before expiring the
	 *            {@link HttpSession}.
	 */
	public MemoryHttpSessionStore(int maxIdleTime) {
		this.maxIdleTime = (maxIdleTime * 1000);
		this.nextExpireTime = (System.currentTimeMillis() + this.maxIdleTime);
	}

	/**
	 * Expires idle {@link SessionState} instances based on current time.
	 *
	 * @param currentTime
	 *            Current time.
	 */
	private void expireIdleSessions(long currentTime) {

		// Determine if time for another expire run
		if (currentTime >= this.nextExpireTime) {

			// Expire all idle session
			for (Iterator<Entry<String, SessionState>> iterator = this.sessions
					.entrySet().iterator(); iterator.hasNext();) {
				SessionState session = iterator.next().getValue();

				// Determine if session is idle and requires expiring
				if (currentTime >= session.expireTime) {
					// Expire the session
					iterator.remove();
				}
			}

			// Indicate time of next expire run
			this.nextExpireTime = (currentTime + this.maxIdleTime);
		}
	}

	/*
	 * =================== HttpSessionStore =============================
	 */

	@Override
	public void createHttpSession(CreateHttpSessionOperation operation) {

		// Create the new Session in anticipation of adding.
		// Improves concurrency performance doing outside locks.
		long currentTime = System.currentTimeMillis();
		SessionState session = new SessionState(currentTime,
				new HashMap<String, Object>(), (currentTime + this.maxIdleTime));

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
			operation.sessionCreated(session.creationTime, session.attributes);
		}
	}

	@Override
	public void retrieveHttpSession(RetrieveHttpSessionOperation operation) {

		// Obtain the Session State
		String sessionId = operation.getSessionId();

		// Obtain session (minimum operation in lock)
		SessionState session;
		synchronized (this.sessions) {
			session = this.sessions.get(sessionId);
		}

		// Determine if available
		if (session == null) {
			// Session not available
			operation.sessionNotAvailable();
		} else {
			// Have Session State so return for Session
			operation
					.sessionRetrieved(session.creationTime, session.attributes);
		}
	}

	@Override
	public void storeHttpSession(StoreHttpSessionOperation operation) {

		// Create new Session State for storing.
		// (Handles if expired while using Session)
		String sessionId = operation.getSessionId();
		long creationTime = operation.getCreationTime();
		Map<String, Object> attributes = operation.getAttributes();
		long expireTime = System.currentTimeMillis() + this.maxIdleTime;
		SessionState session = new SessionState(creationTime, attributes,
				expireTime);

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
		public final long creationTime;

		/**
		 * Attributes.
		 */
		public final Map<String, Object> attributes;

		/**
		 * Time that this {@link SessionState} will be expired.
		 */
		public final long expireTime;

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
		public SessionState(long creationTime, Map<String, Object> attributes,
				long expireTime) {
			this.creationTime = creationTime;
			this.attributes = attributes;
			this.expireTime = expireTime;
		}
	}

}