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

package net.officefloor.web.session;

import java.io.Serializable;
import java.net.HttpCookie;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.spi.CreateHttpSessionOperation;
import net.officefloor.web.session.spi.FreshHttpSession;
import net.officefloor.web.session.spi.HttpSessionIdGenerator;
import net.officefloor.web.session.spi.HttpSessionStore;
import net.officefloor.web.session.spi.InvalidateHttpSessionOperation;
import net.officefloor.web.session.spi.RetrieveHttpSessionOperation;
import net.officefloor.web.session.spi.StoreHttpSessionOperation;

/**
 * {@link ManagedObject} for a {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionManagedObject
		implements CoordinatingManagedObject<Indexed>, AsynchronousManagedObject, ContextAwareManagedObject {

	/**
	 * Use epoch to expire cookies.
	 */
	private static final Instant EXPIRE_COOKIE_INSTANT = Instant.EPOCH;

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
	 * {@link AsynchronousContext}.
	 */
	private AsynchronousContext asynchronousContext;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private ManagedObjectContext managedContextContext;

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
	 * @param sessionIdCookieName         Name of the {@link HttpCookie} containing
	 *                                    the Session Id.
	 * @param serverHttpConnectionIndex   Index of the dependency
	 *                                    {@link ServerHttpConnection}.
	 * @param httpSessionIdGeneratorIndex Index of the dependency
	 *                                    {@link HttpSessionIdGenerator}.
	 * @param httpSessionIdGenerator      {@link HttpSessionIdGenerator}.
	 *                                    <code>null</code> to obtain via
	 *                                    dependency.
	 * @param httpSessionStoreIndex       Index of the dependency
	 *                                    {@link HttpSessionStore}.
	 * @param httpSessionStore            {@link HttpSessionStore}.
	 *                                    <code>null</code> to obtain via
	 *                                    dependency.
	 */
	public HttpSessionManagedObject(String sessionIdCookieName, int serverHttpConnectionIndex,
			int httpSessionIdGeneratorIndex, HttpSessionIdGenerator httpSessionIdGenerator, int httpSessionStoreIndex,
			HttpSessionStore httpSessionStore) {
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
	private void generateSessionId() {

		// As generating, no Session or Id loaded
		this.isIdLoaded = false;
		this.isSessionLoaded = false;

		// Flag that no longer invalidating as generating Session
		this.isInvalidating = false;

		// Trigger generating the Session Id
		this.httpSessionIdGenerator.generateSessionId(new FreshHttpSessionImpl(this.connection));

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
	 * @param sessionId    Session Id.
	 * @param isNewSession Flag indicating if a new {@link HttpSession}.
	 */
	private void loadSessionId(String sessionId, boolean isNewSession) {
		this.isIdLoaded = true;
		this.sessionId = sessionId;
		this.isNewSession = isNewSession;

		// As Id just generated, no Session loaded
		this.isSessionLoaded = false;

		// Handle based on whether a new session
		if (isNewSession) {
			// Create the session within the store
			this.httpSessionStore.createHttpSession(new CreateHttpSessionOperationImpl(this.sessionId));
		} else {
			// Retrieve the session from the store
			this.httpSessionStore.retrieveHttpSession(new RetrieveHttpSessionOperationImpl(this.sessionId));
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
	 * @param creationTime Creation Time.
	 * @param expireTime   Time to expire the {@link HttpSession} should it be idle.
	 * @param attributes   {@link HttpSession} Attributes.
	 */
	private void loadSession(Instant creationTime, Instant expireTime, Map<String, Serializable> attributes) {
		this.isSessionLoaded = true;

		// Load state of session
		this.session.loadState(this.sessionId, creationTime, expireTime, this.isNewSession, attributes);

		// Add cookie to maintain Session Id by client
		this.setSessionIdCookieToHttpResponse(this.sessionId, expireTime);

		// Flag completed load of the Session
		this.flagComplete();
	}

	/**
	 * Triggers storing the {@link HttpSession}.
	 * 
	 * @param sessionId    Session Id.
	 * @param creationTime Creation time.
	 * @param expireTime   Time to expire the {@link HttpSession} should it be idle.
	 * @param attributes   Attributes.
	 * @throws Throwable If immediate failure in storing Session.
	 */
	private void storeSession(String sessionId, Instant creationTime, Instant expireTime,
			Map<String, Serializable> attributes) throws Throwable {

		// Trigger storing the session
		this.isStoring = true;
		this.httpSessionStore
				.storeHttpSession(new StoreHttpSessionOperationImpl(sessionId, creationTime, expireTime, attributes));

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
	private void storeComplete() {
		// Flag no longer storing Session
		this.isStoring = false;
		this.flagComplete();
	}

	/**
	 * Triggers invalidating the {@link HttpSession}.
	 * 
	 * @param isRequireNewSession Flag indicating if requires a new
	 *                            {@link HttpSession}.
	 */
	private void invalidateSession(boolean isRequireNewSession) {
		// No longer loaded
		this.isIdLoaded = false;
		this.isSessionLoaded = false;

		// Flag invalid if not creating another session
		if (!isRequireNewSession) {
			// Capture the session
			String sessionId = this.session.sessionId;

			// Invalidate the Session
			this.session.invalidate(null);

			// Add expired cookie to remove Session Id.
			// (If creating new Session will add appropriate Cookie)
			this.setSessionIdCookieToHttpResponse(sessionId, EXPIRE_COOKIE_INSTANT);
		}

		// Trigger invalidating the session
		this.isInvalidating = true;
		this.httpSessionStore
				.invalidateHttpSession(new InvalidateHttpSessionOperationImpl(this.sessionId, isRequireNewSession));

		// Determine if invalidated immediately
		if (this.isInvalidating) {
			// Not invalidated immediately so wait until invalidated
			this.flagWaiting();
		}
	}

	/**
	 * Flags the invalidation of the {@link HttpSession} is complete.
	 */
	private void invalidateComplete() {
		// Flag no longer invalidating Session
		this.isInvalidating = false;
		this.flagComplete();
	}

	/**
	 * Loads failure.
	 * 
	 * @param cause Cause of the failure.
	 */
	private void loadFailure(Throwable cause) {
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
			this.asynchronousContext.start(null);
		}
	}

	/**
	 * Flags that complete operation and no longer required to wait.
	 */
	private void flagComplete() {
		// Only notify complete if waiting
		if (this.isWaiting) {
			this.asynchronousContext.complete(null);
			this.isWaiting = false;
		}
	}

	/**
	 * Indicates if operation is complete (in other words that can use the
	 * {@link HttpSession}).
	 * 
	 * @return <code>true</code> if can complete the {@link HttpSession}.
	 * @throws Throwable If failure in operation.
	 */
	private boolean isOperationComplete() throws Throwable {

		// Propagate potential failure
		if (this.failure != null) {
			throw this.failure;
		}

		// Not complete if waiting
		return (!this.isWaiting);
	}

	/**
	 * Adds the Session Id {@link HttpCookie} to the {@link HttpResponse}.
	 * 
	 * @param sessionId  Session Id.
	 * @param expireTime Time that the {@link HttpCookie} is to expire.
	 */
	private void setSessionIdCookieToHttpResponse(String sessionId, Instant expireTime) {
		this.connection.getResponse().getCookies().setCookie(this.sessionIdCookieName, sessionId,
				(cookie) -> cookie.setExpires(expireTime));
	}

	/*
	 * ================ ContextAwareManagedObject ===========================
	 */

	@Override
	public void setManagedObjectContext(ManagedObjectContext context) {
		this.managedContextContext = context;
	}

	/*
	 * ================ AsynchronousManagedObject ===========================
	 */

	@Override
	public void setAsynchronousContext(AsynchronousContext asynchronousContext) {
		this.asynchronousContext = asynchronousContext;
	}

	/*
	 * ================ CoordinatingManagedObject ===========================
	 */

	@Override
	public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {

		// Obtain the HTTP request
		this.connection = (ServerHttpConnection) registry.getObject(this.serverHttpConnectionIndex);
		HttpRequest request = this.connection.getRequest();

		// Ensure have the HTTP session Id generator
		if (this.httpSessionIdGenerator == null) {
			this.httpSessionIdGenerator = (HttpSessionIdGenerator) registry.getObject(this.httpSessionIdGeneratorIndex);
		}

		// Ensure have the HTTP session store
		if (this.httpSessionStore == null) {
			this.httpSessionStore = (HttpSessionStore) registry.getObject(this.httpSessionStoreIndex);
		}

		// Obtain the Session Id from the Session cookie
		HttpRequestCookie sessionIdCookie = request.getCookies().getCookie(this.sessionIdCookieName);
		String sessionId = (sessionIdCookie == null ? null : sessionIdCookie.getValue());

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
	public Object getObject() throws Throwable {

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
	private class HttpSessionImpl implements HttpSession, HttpSessionAdministration {

		/**
		 * Session Id.
		 */
		private String sessionId;

		/**
		 * Creation time of the {@link HttpSession}.
		 */
		private Instant creationTime;

		/**
		 * Time to expire the {@link HttpSession} should it be idle.
		 */
		private Instant expireTime;

		/**
		 * Indicates if this {@link HttpSession} is new.
		 */
		private boolean isNew;

		/**
		 * Attributes of the {@link HttpSession}.
		 */
		private Map<String, Serializable> attributes;

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
		 * @param sessionId    Session Id.
		 * @param creationTime Creation time.
		 * @param expireTime   Time to expire the {@link HttpSession} should it be idle.
		 * @param isNew        If a new {@link HttpSession}.
		 * @param attributes   Attributes.
		 */
		private void loadState(String sessionId, Instant creationTime, Instant expireTime, boolean isNew,
				Map<String, Serializable> attributes) {
			// Load state
			this.sessionId = sessionId;
			this.creationTime = creationTime;
			this.expireTime = expireTime;
			this.isNew = isNew;
			this.attributes = attributes;

			// Now valid HTTP session
			this.isInvalid = false;
		}

		/**
		 * Flags this {@link HttpSession} as invalid.
		 * 
		 * @param failure Potential failure invalidating this {@link HttpSession}. May
		 *                be <code>null</code>.
		 */
		private void invalidate(Throwable failure) {
			this.isInvalid = true;
			this.invalidateFailure = failure;
		}

		/**
		 * Ensures {@link HttpSession} is valid for use.
		 * 
		 * @throws InvalidatedSessionHttpException If {@link HttpSession} is not valid
		 *                                         for use.
		 */
		private void ensureValid() throws InvalidatedSessionHttpException {
			// Not valid if:
			// - invalid
			// - currently invalidating
			if (this.isInvalid || HttpSessionManagedObject.this.isInvalidating) {
				throw new InvalidatedSessionHttpException(this.invalidateFailure);
			}
		}

		/**
		 * Ensures can alter the {@link HttpSession}.
		 * 
		 * @throws StoringSessionHttpException If not able to alter the
		 *                                     {@link HttpSession}.
		 */
		private void ensureCanAlter() throws StoringSessionHttpException {
			if (HttpSessionManagedObject.this.isStoring) {
				throw new StoringSessionHttpException();
			}
		}

		/*
		 * ================ HttpSession ===============================
		 */

		@Override
		public String getSessionId() {
			return HttpSessionManagedObject.this.managedContextContext.run(() -> {
				this.ensureValid();
				return this.sessionId;
			});
		}

		@Override
		public String getTokenName() {
			return HttpSessionManagedObject.this.managedContextContext
					.run(() -> HttpSessionManagedObject.this.sessionIdCookieName);
		}

		@Override
		public Instant getCreationTime() {
			return HttpSessionManagedObject.this.managedContextContext.run(() -> {
				this.ensureValid();
				return this.creationTime;
			});
		}

		@Override
		public Instant getExpireTime() throws InvalidatedSessionHttpException {
			return HttpSessionManagedObject.this.managedContextContext.run(() -> {
				this.ensureValid();
				return this.expireTime;
			});
		}

		@Override
		public void setExpireTime(Instant expireTime)
				throws StoringSessionHttpException, InvalidatedSessionHttpException {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				this.ensureValid();
				this.ensureCanAlter();

				// Update expiring of Session Id (and its cookie)
				this.expireTime = expireTime;
				HttpSessionManagedObject.this.setSessionIdCookieToHttpResponse(this.sessionId, this.expireTime);

				// Void return
				return null;
			});
		}

		@Override
		public boolean isNew() {
			return HttpSessionManagedObject.this.managedContextContext.run(() -> {
				this.ensureValid();
				return this.isNew;
			});
		}

		@Override
		public Serializable getAttribute(String name) {
			return HttpSessionManagedObject.this.managedContextContext.run(() -> {
				this.ensureValid();
				return this.attributes.get(name);
			});
		}

		@Override
		public Iterator<String> getAttributeNames() {
			return HttpSessionManagedObject.this.managedContextContext.run(() -> {
				this.ensureValid();
				return this.attributes.keySet().iterator();
			});
		}

		@Override
		public void setAttribute(String name, Serializable object) {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				this.ensureValid();
				this.ensureCanAlter();
				this.attributes.put(name, object);
				return null;
			});
		}

		@Override
		public void removeAttribute(String name) {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				this.ensureValid();
				this.ensureCanAlter();
				this.attributes.remove(name);
				return null;
			});
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
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.invalidateSession(isRequireNewSession);
				return null;
			});
		}

		@Override
		public void store() throws Throwable {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.storeSession(this.sessionId, this.creationTime, this.expireTime,
						this.attributes);
				return null;
			});
		}

		@Override
		public boolean isOperationComplete() throws Throwable {
			return HttpSessionManagedObject.this.managedContextContext
					.run(() -> HttpSessionManagedObject.this.isOperationComplete());
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
		 * @param connection {@link ServerHttpConnection}.
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
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.loadSessionId(sessionId, true);
				return null;
			});
		}

		@Override
		public void failedToGenerateSessionId(Throwable failure) {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.loadFailure(failure);
				return null;
			});
		}
	}

	/**
	 * {@link CreateHttpSessionOperation} implementation.
	 */
	private class CreateHttpSessionOperationImpl implements CreateHttpSessionOperation {

		/**
		 * Session Id.
		 */
		private final String sessionId;

		/**
		 * Initiate.
		 * 
		 * @param sessionId Session Id.
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
		public void sessionCreated(Instant creationTime, Instant expireTime, Map<String, Serializable> attributes) {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.loadSession(creationTime, expireTime, attributes);
				return null;
			});
		}

		@Override
		public void sessionIdCollision() {
			// Generate a new Session Id
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.generateSessionId();
				return null;
			});
		}

		@Override
		public void failedToCreateSession(Throwable cause) {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.loadFailure(cause);
				return null;
			});
		}
	}

	/**
	 * {@link RetrieveHttpSessionOperation} implementation.
	 */
	private class RetrieveHttpSessionOperationImpl implements RetrieveHttpSessionOperation {

		/**
		 * Session Id.
		 */
		private final String sessionId;

		/**
		 * Initiate.
		 * 
		 * @param sessionId Session Id.
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
		public void sessionRetrieved(Instant creationTime, Instant expireTime, Map<String, Serializable> attributes) {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.loadSession(creationTime, expireTime, attributes);
				return null;
			});
		}

		@Override
		public void sessionNotAvailable() {
			// Session not available so generate new Session
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.generateSessionId();
				return null;
			});
		}

		@Override
		public void failedToRetreiveSession(Throwable cause) {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.loadFailure(cause);
				return null;
			});
		}
	}

	/**
	 * {@link StoreHttpSessionOperation} implementation.
	 */
	private class StoreHttpSessionOperationImpl implements StoreHttpSessionOperation {

		/**
		 * Session Id.
		 */
		private final String sessionId;

		/**
		 * Creation time.
		 */
		private final Instant creationTime;

		/**
		 * Expire time.
		 */
		private final Instant expireTime;

		/**
		 * Attributes.
		 */
		private final Map<String, Serializable> attributes;

		/**
		 * Initiate.
		 * 
		 * @param sessionId    Session Id.
		 * @param creationTime Creation time.
		 * @param expireTime   Time to expire the {@link HttpSession} should it be idle.
		 * @param attributes   Attributes.
		 */
		public StoreHttpSessionOperationImpl(String sessionId, Instant creationTime, Instant expireTime,
				Map<String, Serializable> attributes) {
			this.sessionId = sessionId;
			this.creationTime = creationTime;
			this.expireTime = expireTime;
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
		public Instant getCreationTime() {
			return this.creationTime;
		}

		@Override
		public Instant getExpireTime() {
			return this.expireTime;
		}

		@Override
		public Map<String, Serializable> getAttributes() {
			return this.attributes;
		}

		@Override
		public void sessionStored() {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.storeComplete();
				return null;
			});
		}

		@Override
		public void failedToStoreSession(Throwable cause) {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.loadFailure(cause);
				return null;
			});
		}
	}

	/**
	 * {@link InvalidateHttpSessionOperation} implementation.
	 */
	private class InvalidateHttpSessionOperationImpl implements InvalidateHttpSessionOperation {

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
		 * @param sessionId           Session Id.
		 * @param isRequireNewSession Flag indicating if require a new
		 *                            {@link HttpSession}.
		 */
		public InvalidateHttpSessionOperationImpl(String sessionId, boolean isRequireNewSession) {
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
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				if (this.isRequireNewSession) {
					// Generate a new Session as required
					HttpSessionManagedObject.this.generateSessionId();
				} else {
					// Flag invalidate complete, leaving Session invalid
					HttpSessionManagedObject.this.invalidateComplete();
				}
				return null;
			});
		}

		@Override
		public void failedToInvalidateSession(Throwable cause) {
			HttpSessionManagedObject.this.managedContextContext.run(() -> {
				HttpSessionManagedObject.this.loadFailure(cause);
				return null;
			});
		}
	}

}
