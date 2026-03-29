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

package net.officefloor.web.session.spi;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import net.officefloor.web.session.HttpSession;

/**
 * Operation to obtain details of storing the {@link HttpSession} within the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface StoreHttpSessionOperation {

	/**
	 * Obtains the session Id of the {@link HttpSession} to store.
	 *
	 * @return Session Id of the {@link HttpSession} to store.
	 */
	String getSessionId();

	/**
	 * Obtains the creation time for the {@link HttpSession}.
	 *
	 * @return Creation time for the {@link HttpSession}.
	 */
	Instant getCreationTime();

	/**
	 * Obtains the time to expire the {@link HttpSession} should it be idle.
	 *
	 * @return Time to expire the {@link HttpSession} should it be idle.
	 */
	Instant getExpireTime();

	/**
	 * Obtains the attributes of the {@link HttpSession}.
	 *
	 * @return Attributes of the {@link HttpSession}.
	 */
	Map<String, Serializable> getAttributes();

	/**
	 * Flags the {@link HttpSession} was stored successfully within the
	 * {@link HttpSessionStore}.
	 */
	void sessionStored();

	/**
	 * Flags failed to store the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToStoreSession(Throwable cause);

}
