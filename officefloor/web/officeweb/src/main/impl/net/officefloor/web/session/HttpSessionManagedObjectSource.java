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

import java.net.HttpCookie;
import java.time.Clock;
import java.time.ZoneId;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.generator.UuidHttpSessionIdGenerator;
import net.officefloor.web.session.spi.HttpSessionIdGenerator;
import net.officefloor.web.session.spi.HttpSessionStore;
import net.officefloor.web.session.store.MemoryHttpSessionStore;

/**
 * {@link ManagedObjectSource} for a {@link HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpSessionManagedObjectSource extends AbstractManagedObjectSource<Indexed, Indexed> {

	/**
	 * Property name to obtain the {@link HttpCookie} name of the Session Id.
	 */
	public static final String PROPERTY_SESSION_ID_COOKIE_NAME = "session.id.cookie.name";

	/**
	 * Property name specifying whether to use an external
	 * {@link HttpSessionIdGenerator}. Value of <code>true</code> indicates to
	 * link as dependency. A default {@link HttpSessionIdGenerator} is used
	 * otherwise.
	 */
	public static final String PROPERTY_USE_DEPENDENCY_SESSION_ID_GENERATOR = "use.dependency.session.id.generator";

	/**
	 * Property name specifying whether to use an external
	 * {@link HttpSessionStore}. Value of <code>true</code> indicates to link as
	 * dependency. A default {@link HttpSessionStore} is used otherwise.
	 */
	public static final String PROPERTY_USE_DEPENDENCY_SESSION_STORE = "use.dependency.session.store";

	/**
	 * Property name to obtain the default maximum idle times for
	 * {@link HttpSession} instances.
	 */
	public static final String PROPERTY_MAX_IDLE_TIME = "max.idle.time";

	/**
	 * Keep default consistent with JEE to aid in special processing of session
	 * identifiers by such things as search engines.
	 */
	public static final String DEFAULT_SESSION_ID_COOKIE_NAME = "jsessionid";

	/**
	 * Default maximum idle time for a {@link HttpSession} in seconds.
	 */
	private static final int DEFAULT_MAX_IDLE_TIME = (20 * 60); // 20 minutes

	/**
	 * Name of the {@link HttpCookie} containing the Session Id.
	 */
	private String sessionIdCookieName;

	/**
	 * Dependency index of the {@link ServerHttpConnection}.
	 */
	private int serverHttpConnectionIndex;

	/**
	 * Dependency index of the {@link HttpSessionIdGenerator}.
	 */
	private int httpSessionIdGeneratorIndex = -1;

	/**
	 * {@link HttpSessionIdGenerator} to use if not link in as dependency.
	 */
	private HttpSessionIdGenerator generator = null;

	/**
	 * Dependency index of the {@link HttpSessionStore}.
	 */
	private int httpSessionStoreIndex = -1;

	/**
	 * {@link HttpSessionStore} to use if not link in as dependency.
	 */
	private HttpSessionStore store = null;

	/*
	 * ================== AbstractManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required (as have defaults)
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, Indexed> context) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();

		// Specify types
		context.setObjectClass(HttpSession.class);
		context.setManagedObjectClass(HttpSessionManagedObject.class);

		// Obtain the Session Id cookie name
		this.sessionIdCookieName = mosContext.getProperty(PROPERTY_SESSION_ID_COOKIE_NAME,
				DEFAULT_SESSION_ID_COOKIE_NAME);

		// Register dependency on HTTP connection
		this.serverHttpConnectionIndex = context.addDependency(ServerHttpConnection.class).setLabel("HTTP_CONNECTION")
				.getIndex();

		// Determine Session Id generator to use
		String useDependencySessionIdGenerator = mosContext.getProperty(PROPERTY_USE_DEPENDENCY_SESSION_ID_GENERATOR,
				String.valueOf(false));
		if (Boolean.parseBoolean(useDependencySessionIdGenerator)) {
			// Use dependency Session Id generator
			this.httpSessionIdGeneratorIndex = context.addDependency(HttpSessionIdGenerator.class)
					.setLabel("SESSION_ID_GENERATOR").getIndex();
		} else {
			// Use default Session Id generator
			this.generator = new UuidHttpSessionIdGenerator();
		}

		// Determine Session Store to use
		String useDependencySessionStore = mosContext.getProperty(PROPERTY_USE_DEPENDENCY_SESSION_STORE,
				String.valueOf(false));
		if (Boolean.parseBoolean(useDependencySessionStore)) {
			// Use dependency Session Store
			this.httpSessionStoreIndex = context.addDependency(HttpSessionStore.class).setLabel("SESSION_STORE")
					.getIndex();
		} else {
			// Use default Session Store
			int maxIdleTime = Integer
					.parseInt(mosContext.getProperty(PROPERTY_MAX_IDLE_TIME, String.valueOf(DEFAULT_MAX_IDLE_TIME)));
			this.store = new MemoryHttpSessionStore(Clock.system(ZoneId.of("GMT")), maxIdleTime);
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpSessionManagedObject(this.sessionIdCookieName, this.serverHttpConnectionIndex,
				this.httpSessionIdGeneratorIndex, this.generator, this.httpSessionStoreIndex, this.store);
	}
}
