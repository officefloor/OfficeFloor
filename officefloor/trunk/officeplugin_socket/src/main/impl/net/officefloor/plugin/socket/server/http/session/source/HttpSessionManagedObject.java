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
package net.officefloor.plugin.socket.server.http.session.source;

import java.util.Iterator;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.cookie.HttpCookie;
import net.officefloor.plugin.socket.server.http.cookie.HttpCookieUtil;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.HttpSessionAdministration;
import net.officefloor.plugin.socket.server.http.session.InvalidatedHttpSessionException;
import net.officefloor.plugin.socket.server.http.session.StoringHttpSessionException;
import net.officefloor.plugin.socket.server.http.session.spi.CreateHttpSessionOperation;
import net.officefloor.plugin.socket.server.http.session.spi.FreshHttpSession;
import net.officefloor.plugin.socket.server.http.session.spi.HttpSessionIdGenerator;
import net.officefloor.plugin.socket.server.http.session.spi.HttpSessionStore;
import net.officefloor.plugin.socket.server.http.session.spi.InvalidateHttpSessionOperation;
import net.officefloor.plugin.socket.server.http.session.spi.RetrieveHttpSessionOperation;
import net.officefloor.plugin.socket.server.http.session.spi.StoreHttpSessionOperation;

/**
 * {@link ManagedObject} for a {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpSessionManagedObject implements
		CoordinatingManagedObject<Indexed>, AsynchronousManagedObject {

	/**
	 * {@link HttpSession} to be provided as the object of this
	 * {@link ManagedObject}.
	 */
	private final HttpSessionImpl session = new HttpSessionImpl();

	/**
	 * Name of the {@link HttpCookie} containing the Session Id.
	 */
	private final String sessionIdCookieName;

	/**
	 * Index of the dependency {@link ServerHttpConnection}.
	 */
	private final int serverHttpConnectionIndex;

	/**
	 * Index of the dependency {@link HttpSessionIdGenerator}.
	 */
	private final int httpSessionIdGeneratorIndex;

	/**
	 * {@link HttpSessionIdGenerator}.
	 */
	private HttpSessionIdGenerator httpSessionIdGenerator;

	/**
	 * Index of the dependency {@link HttpSessionStore}.
	 */
	private final int httpSessionStoreIndex;

	/**
	 * {@link HttpSessionStore}.
	 */
	private HttpSessionStore httpSessionStore;

	/**
	 * {@link AsynchronousListener}.
	 */
	private AsynchronousListener asynchronousListener;

	/**
	 * {@link ServerHttpConnection}.
	 */
	private ServerHttpConnection connection;

	/**
	 * Flag indicating if waiting on an asynchronous operation.
	 */
	private boolean isWaiting = false;

	/**
	 * Flag indicating if the Session Id has been loaded.
	 */
	private boolean isIdLoaded = false;

	/**
	 * Flag indicating if the Session has been loaded.
	 */
	private boolean isSessionLoaded = false;

	/**
	 * Flag indicating if invalidating the {@link HttpSession}.
	 */
	private boolean isInvalidating = false;

	/**
	 * Flag indicating if storing the {@link HttpSession}.
	 */
	private boolean isStoring = false;

	/**
	 * Failure in loading the {@link HttpSession}.
	 */
	private Throwable failure = null;

	/**
	 * Session Id.
	 */
	private String sessionId = null;

	/**
	 * Flag indicating if the {@link HttpSession} is new.
	 */
	private boolean isNewSession = false;

	/**
	 * Initiate.
	 *
	 * @param sessionIdCookieName
	 *            Name of the {@link HttpCookie} containing the Session Id.
	 * @param serverHttpConnectionIndex
	 *            Index of the dependency {@link ServerHttpConnection}.
	 * @param httpSessionIdGeneratorIndex
	 *            Index of the dependency {@link HttpSessionIdGenerator}.
	 * @param httpSessionIdGenerator
	 *            {@link HttpSessionIdGenerator}. <code>null</code> to obtain
	 *            via dependency.
	 * @param httpSessionStoreIndex
	 *            Index of the dependency {@link HttpSessionStore}.
	 * @param httpSessionStore
	 *            {@link HttpSessionStore}. <code>null</code> to obtain via
	 *            dependency.
	 */
	public HttpSessionManagedObject(String sessionIdCookieName,
			int serverHttpConnectionIndex, int httpSessionIdGeneratorIndex,
			HttpSessionIdGenerator httpSessionIdGenerator,
			int httpSessionStoreIndex, HttpSessionStore httpSessionStore) {
		this.sessionIdCookieName = sessionIdCookieName;
		this.serverHttpConnectionIndex = serverHttpConnectionIndex;
		this.httpSessionIdGeneratorIndex = httpSessionIdGeneratorIndex;
		this.httpSessionIdGenerator = httpSessionIdGenerator;
		this.httpSessionStoreIndex = httpSessionStoreIndex;
		this.httpSessionStore = httpSessionStore;
	}

	/**
	 * Generates the Session Id.
	 */
	private synchronized void generateSessionId() {

		// As generating, no Session or Id loaded
		this.isIdLoaded = false;
		this.isSessionLoaded = false;

		// Flag that no longer invalidating as generating Session
		this.isInvalidating = false;

		// Trigger generating the Session Id
		this.httpSessionIdGenerator.generateSessionId(new FreshHttpSessionImpl(
				this.connection));

		// Check if Session Id immediately generated
		if (!this.isIdLoaded) {
			// Session Id not loaded so wait until loaded
			this.flagWaiting();
		}
	}

	/**
	 * Loads the Session Id and continues processing loading the
	 * {@link HttpSession}.
	 *
	 * @param sessionId
	 *            Session Id.
	 * @param isNewSession
	 *            Flag indicating if a new {@link HttpSession}.
	 */
	private synchronized void loadSessionId(String sessionId,
			boolean isNewSession) {
		this.isIdLoaded = true;
		this.sessionId = sessionId;
		this.isNewSession = isNewSession;

		// As Id just generated, no Session loaded
		this.isSessionLoaded = false;

		// Handle based on whether a new session
		if (isNewSession) {
			// Create the session within the store
			this.httpSessionStore
					.createHttpSession(new CreateHttpSessionOperationImpl(
							this.sessionId));
		} else {
			// Retrieve the session from the store
			this.httpSessionStore
					.retrieveHttpSession(new RetrieveHttpSessionOperationImpl(
							this.sessionId));
		}

		// Determine if the session loaded
		if (!this.isSessionLoaded) {
			// Wait until the session is loaded
			this.flagWaiting();
		}
	}

	/**
	 * Loads the {@link HttpSession}.
	 *
	 * @param creationTime
	 *            Creation Time.
	 * @param attributes
	 *            {@link HttpSession} Attributes.
	 */
	private synchronized void loadSession(long creationTime,
			Map<String, Object> attributes) {
		this.isSessionLoaded = true;

		// Load state of session
		this.session.loadState(this.sessionId, creationTime, this.isNewSession,
				attributes);

		// Flag completed load of the Session
		this.flagComplete();
	}

	/**
	 * Triggers storing the {@link HttpSession}.
	 *
	 * @param sessionId
	 *            Session Id.
	 * @param creationTime
	 *            Creation time.
	 * @param attributes
	 *            Attributes.
	 * @throws Throwable
	 *             If immediate failure in storing Session.
	 */
	private synchronized void storeSession(String sessionId, long creationTime,
			Map<String, Object> attributes) throws Throwable {

		// Trigger storing the session
		this.isStoring = true;
		this.httpSessionStore
				.storeHttpSession(new StoreHttpSessionOperationImpl(sessionId,
						creationTime, attributes));

		// Determine if stored immediately
		if (this.isStoring) {
			// Not stored immediately so wait until stored
			this.flagWaiting();
		}

		// Propagate immediate failure to store
		if (this.failure != null) {
			throw this.failure;
		}
	}

	/**
	 * Flags the storing of the {@link HttpSession} is complete.
	 */
	private synchronized void storeComplete() {
		// Flag no longer storing Session
		this.isStoring = false;
		this.flagComplete();
	}

	/**
	 * Triggers invalidating the {@link HttpSession}.
	 *
	 * @param isRequireNewSession
	 *            Flag indicating if requires a new {@link HttpSession}.
	 */
	private synchronized void invalidateSession(boolean isRequireNewSession) {
		// No longer loaded
		this.isIdLoaded = false;
		this.isSessionLoaded = false;

		// Flag invalid if not creating another session
		if (!isRequireNewSession) {
			this.session.invalidate(null);
		}

		// Trigger invalidating the session
		this.isInvalidating = true;
		this.httpSessionStore
				.invalidateHttpSession(new InvalidateHttpSessionOperationImpl(
						this.sessionId, isRequireNewSession));

		// Determine if invalidated immediately
		if (this.isInvalidating) {
			// Not invalidated immediately so wait until invalidated
			this.flagWaiting();
		}
	}

	/**
	 * Flags the invalidation of the {@link HttpSession} is complete.
	 */
	private synchronized void invalidateComplete() {
		// Flag no longer invalidating Session
		this.isInvalidating = false;
		this.flagComplete();
	}

	/**
	 * Loads failure.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	private synchronized void loadFailure(Throwable cause) {
		this.failure = cause;

		// Flag loaded (as no further loading on failure)
		this.isIdLoaded = true;
		this.isSessionLoaded = true;

		// Flag session invalid
		this.session.invalidate(cause);

		// No longer invalidating due to error
		this.isInvalidating = false;

		// Flag operation complete as failed
		this.flagComplete();
	}

	/**
	 * Flags that waiting on asynchronous operation.
	 */
	private void flagWaiting() {
		// Only notify once that waiting (if no failure)
		if ((!this.isWaiting) && (this.failure == null)) {
			// Flat to wait as not yet waiting
			this.isWaiting = true;
			this.asynchronousListener.notifyStarted();
		}
	}

	/**
	 * Flags that complete operation and no longer required to wait.
	 */
	private void flagComplete() {
		// Only notify complete if waiting
		if (this.isWaiting) {
			this.asynchronousListener.notifyComplete();
			this.isWaiting = false;
		}
	}

	/**
	 * Indicates if operation is complete (in other words that can use the
	 * {@link HttpSession}).
	 *
	 * @return <code>true</code> if can complete the {@link HttpSession}.
	 * @throws Throwable
	 *             If failure in operation.
	 */
	private boolean isOperationComplete() throws Throwable {

		// Propagate potential failure
		if (this.failure != null) {
			throw this.failure;
		}

		// Not complete if waiting
		return (!this.isWaiting);
	}

	/*
	 * ================ AsynchronousManagedObject ===========================
	 */

	@Override
	public synchronized void registerAsynchronousCompletionListener(
			AsynchronousListener listener) {
		this.asynchronousListener = listener;
	}

	/*
	 * ================ CoordinatingManagedObject ===========================
	 */

	@Override
	public synchronized void loadObjects(ObjectRegistry<Indexed> registry)
			throws Throwable {

		// Obtain the HTTP request
		this.connection = (ServerHttpConnection) registry
				.getObject(this.serverHttpConnectionIndex);
		HttpRequest request = this.connection.getHttpRequest();

		// Ensure have the HTTP session Id generator
		if (this.httpSessionIdGenerator == null) {
			this.httpSessionIdGenerator = (HttpSessionIdGenerator) registry
					.getObject(this.httpSessionIdGeneratorIndex);
		}

		// Ensure have the HTTP session store
		if (this.httpSessionStore == null) {
			this.httpSessionStore = (HttpSessionStore) registry
					.getObject(this.httpSessionStoreIndex);
		}

		// Obtain the Session Id from the Session cookie
		HttpCookie sessionIdCookie = HttpCookieUtil.extractHttpCookie(
				this.sessionIdCookieName, request);
		String sessionId = (sessionIdCookie == null ? null : sessionIdCookie
				.getValue());

		// Handle based on Session Id being available
		if ((sessionId == null) || (sessionId.trim().length() == 0)) {
			// No established session so create a new session
			this.generateSessionId();
		} else {
			// Retrieve the existing session
			this.loadSessionId(sessionId, false);
		}
	}

	/*
	 * ======================== ManagedObject ================================
	 */

	@Override
	public synchronized Object getObject() throws Throwable {

		// Propagate failure in obtaining Http Session
		if (this.failure != null) {
			throw this.failure;
		}

		// No failure so return the Http Session
		return this.session;
	}

	/**
	 * {@link HttpSession} and {@link HttpSessionAdministration} implementation.
	 */
	private class HttpSessionImpl implements HttpSession,
			HttpSessionAdministration {

		/**
		 * Session Id.
		 */
		private String sessionId;

		/**
		 * Creation time of the {@link HttpSession}.
		 */
		private long creationTime;

		/**
		 * Indicates if this {@link HttpSession} is new.
		 */
		private boolean isNew;

		/**
		 * Attributes of the {@link HttpSession}.
		 */
		private Map<String, Object> attributes;

		/**
		 * Flag indicating if this {@link HttpSession} is invalid.
		 */
		private boolean isInvalid = true;

		/**
		 * Failure in invalidating this {@link HttpSession}.
		 */
		private Throwable invalidateFailure = null;

		/**
		 * Loads the state of this {@link HttpSession}.
		 *
		 * @param sessionId
		 *            Session Id.
		 * @param creationTime
		 *            Creation time.
		 * @param isNew
		 *            If a new {@link HttpSession}.
		 * @param attributes
		 *            Attributes.
		 */
		void loadState(String sessionId, long creationTime, boolean isNew,
				Map<String, Object> attributes) {
			// Load state
			this.sessionId = sessionId;
			this.creationTime = creationTime;
			this.isNew = isNew;
			this.attributes = attributes;

			// Now valid HTTP session
			this.isInvalid = false;
		}

		/**
		 * Flags this {@link HttpSession} as invalid.
		 *
		 * @param failure
		 *            Potential failure invalidating this {@link HttpSession}.
		 *            May be <code>null</code>.
		 */
		void invalidate(Throwable failure) {
			this.isInvalid = true;
			this.invalidateFailure = failure;
		}

		/**
		 * Ensures {@link HttpSession} is valid for use.
		 *
		 * @throws InvalidatedHttpSessionException
		 *             If {@link HttpSession} is not valid for use.
		 */
		private void ensureValid() throws InvalidatedHttpSessionException {
			// Not valid if:
			// - invalid
			// - currently invalidating
			if (this.isInvalid || HttpSessionManagedObject.this.isInvalidating) {
				throw new InvalidatedHttpSessionException(
						this.invalidateFailure);
			}
		}

		/**
		 * Ensures can alter the {@link HttpSession}.
		 *
		 * @throws StoringHttpSessionException
		 *             If not able to alter the {@link HttpSession}.
		 */
		private void ensureCanAlter() throws StoringHttpSessionException {
			if (HttpSessionManagedObject.this.isStoring) {
				throw new StoringHttpSessionException();
			}
		}

		/*
		 * ================ HttpSession ===============================
		 */

		@Override
		public String getSessionId() {
			synchronized (HttpSessionManagedObject.this) {
				this.ensureValid();
				return this.sessionId;
			}
		}

		@Override
		public long getCreationTime() {
			synchronized (HttpSessionManagedObject.this) {
				this.ensureValid();
				return this.creationTime;
			}
		}

		@Override
		public boolean isNew() {
			synchronized (HttpSessionManagedObject.this) {
				this.ensureValid();
				return this.isNew;
			}
		}

		@Override
		public Object getAttribute(String name) {
			synchronized (HttpSessionManagedObject.this) {
				this.ensureValid();
				return this.attributes.get(name);
			}
		}

		@Override
		public Iterator<String> getAttributeNames() {
			synchronized (HttpSessionManagedObject.this) {
				this.ensureValid();
				return this.attributes.keySet().iterator();
			}
		}

		@Override
		public void setAttribute(String name, Object object) {
			synchronized (HttpSessionManagedObject.this) {
				this.ensureValid();
				this.ensureCanAlter();
				this.attributes.put(name, object);
			}
		}

		@Override
		public void removeAttribute(String name) {
			synchronized (HttpSessionManagedObject.this) {
				this.ensureValid();
				this.ensureCanAlter();
				this.attributes.remove(name);
			}
		}

		@Override
		public HttpSessionAdministration getHttpSessionAdministration() {
			return this;
		}

		/*
		 * ================== HttpSessionAdministration =======================
		 */

		@Override
		public void invalidate(boolean isRequireNewSession) throws Throwable {
			HttpSessionManagedObject.this
					.invalidateSession(isRequireNewSession);
		}

		@Override
		public void store() throws Throwable {
			HttpSessionManagedObject.this.storeSession(this.sessionId,
					this.creationTime, this.attributes);
		}

		@Override
		public boolean isOperationComplete() throws Throwable {
			return HttpSessionManagedObject.this.isOperationComplete();
		}
	}

	/**
	 * {@link FreshHttpSession} implementation.
	 */
	private class FreshHttpSessionImpl implements FreshHttpSession {

		/**
		 * {@link ServerHttpConnection}.
		 */
		private final ServerHttpConnection connection;

		/**
		 * Initiate.
		 *
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 */
		public FreshHttpSessionImpl(ServerHttpConnection connection) {
			this.connection = connection;
		}

		/*
		 * ================= FreshHttpSession ===========================
		 */

		@Override
		public ServerHttpConnection getConnection() {
			return this.connection;
		}

		@Override
		public void setSessionId(String sessionId) {
			HttpSessionManagedObject.this.loadSessionId(sessionId, true);
		}

		@Override
		public void failedToGenerateSessionId(Throwable failure) {
			HttpSessionManagedObject.this.loadFailure(failure);
		}
	}

	/**
	 * {@link CreateHttpSessionOperation} implementation.
	 */
	private class CreateHttpSessionOperationImpl implements
			CreateHttpSessionOperation {

		/**
		 * Session Id.
		 */
		private final String sessionId;

		/**
		 * Initiate.
		 *
		 * @param sessionId
		 *            Session Id.
		 */
		public CreateHttpSessionOperationImpl(String sessionId) {
			this.sessionId = sessionId;
		}

		/*
		 * ============= CreateHttpSessionOperation ====================
		 */

		@Override
		public String getSessionId() {
			return this.sessionId;
		}

		@Override
		public void sessionCreated(long creationTime,
				Map<String, Object> attributes) {
			HttpSessionManagedObject.this.loadSession(creationTime, attributes);
		}

		@Override
		public void sessionIdCollision() {
			// Generate a new Session Id
			HttpSessionManagedObject.this.generateSessionId();
		}

		@Override
		public void failedToCreateSession(Throwable cause) {
			HttpSessionManagedObject.this.loadFailure(cause);
		}
	}

	/**
	 * {@link RetrieveHttpSessionOperation} implementation.
	 */
	private class RetrieveHttpSessionOperationImpl implements
			RetrieveHttpSessionOperation {

		/**
		 * Session Id.
		 */
		private final String sessionId;

		/**
		 * Initiate.
		 *
		 * @param sessionId
		 *            Session Id.
		 */
		public RetrieveHttpSessionOperationImpl(String sessionId) {
			this.sessionId = sessionId;
		}

		/*
		 * ================ RetrieveHttpSessionOperation ===================
		 */

		@Override
		public String getSessionId() {
			return this.sessionId;
		}

		@Override
		public void sessionRetrieved(long creationTime,
				Map<String, Object> attributes) {
			HttpSessionManagedObject.this.loadSession(creationTime, attributes);
		}

		@Override
		public void sessionNotAvailable() {
			// Session not available so generate new Session
			HttpSessionManagedObject.this.generateSessionId();
		}

		@Override
		public void failedToRetreiveSession(Throwable cause) {
			HttpSessionManagedObject.this.loadFailure(cause);
		}
	}

	/**
	 * {@link StoreHttpSessionOperation} implementation.
	 */
	private class StoreHttpSessionOperationImpl implements
			StoreHttpSessionOperation {

		/**
		 * Session Id.
		 */
		private final String sessionId;

		/**
		 * Creation time.
		 */
		private final long creationTime;

		/**
		 * Attributes.
		 */
		private final Map<String, Object> attributes;

		/**
		 * Initiate.
		 *
		 * @param sessionId
		 *            Session Id.
		 * @param creationTime
		 *            Creation time.
		 * @param attributes
		 *            Attributes.
		 */
		public StoreHttpSessionOperationImpl(String sessionId,
				long creationTime, Map<String, Object> attributes) {
			this.sessionId = sessionId;
			this.creationTime = creationTime;
			this.attributes = attributes;
		}

		/*
		 * ============= StoreHttpSessionOperation ==========================
		 */

		@Override
		public String getSessionId() {
			return this.sessionId;
		}

		@Override
		public long getCreationTime() {
			return this.creationTime;
		}

		@Override
		public Map<String, Object> getAttributes() {
			return this.attributes;
		}

		@Override
		public void sessionStored() {
			HttpSessionManagedObject.this.storeComplete();
		}

		@Override
		public void failedToStoreSession(Throwable cause) {
			HttpSessionManagedObject.this.loadFailure(cause);
		}
	}

	/**
	 * {@link InvalidateHttpSessionOperation} implementation.
	 */
	private class InvalidateHttpSessionOperationImpl implements
			InvalidateHttpSessionOperation {

		/**
		 * Session Id.
		 */
		private final String sessionId;

		/**
		 * Flag indicating if require a new {@link HttpSession}.
		 */
		private final boolean isRequireNewSession;

		/**
		 * Initiate.
		 *
		 * @param sessionId
		 *            Session Id.
		 * @param isRequireNewSession
		 *            Flag indicating if require a new {@link HttpSession}.
		 */
		public InvalidateHttpSessionOperationImpl(String sessionId,
				boolean isRequireNewSession) {
			this.sessionId = sessionId;
			this.isRequireNewSession = isRequireNewSession;
		}

		/*
		 * ============== InvalidateHttpSessionOperation =====================
		 */

		@Override
		public String getSessionId() {
			return this.sessionId;
		}

		@Override
		public void sessionInvalidated() {
			if (this.isRequireNewSession) {
				// Generate a new Session as required
				HttpSessionManagedObject.this.generateSessionId();
			} else {
				// Flag invalidate complete, leaving Session invalid
				HttpSessionManagedObject.this.invalidateComplete();
			}
		}

		@Override
		public void failedToInvalidateSession(Throwable cause) {
			HttpSessionManagedObject.this.loadFailure(cause);
		}
	}

}