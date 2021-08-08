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
 * Operation to obtain details of retrieving a {@link HttpSession} from the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface RetrieveHttpSessionOperation {

	/**
	 * Obtains the session Id of the {@link HttpSession} to retrieve.
	 *
	 * @return Session Id of the {@link HttpSession} to retrieve.
	 */
	String getSessionId();

	/**
	 * Flags that the {@link HttpSession} was successfully retrieved from the
	 * {@link HttpSessionStore}.
	 *
	 * @param creationTime
	 *            Time the {@link HttpSession} was created in the
	 *            {@link HttpSessionStore}.
	 * @param expireTime
	 *            Time to expire the {@link HttpSession} should it be idle.
	 * @param attributes
	 *            Attributes for the retrieved {@link HttpSession}.
	 */
	void sessionRetrieved(Instant creationTime, Instant expireTime, Map<String, Serializable> attributes);

	/**
	 * <p>
	 * Flags that the {@link HttpSession} is not available in the
	 * {@link HttpSessionStore}.
	 * <p>
	 * Typically this is due to the {@link HttpSession} timing out and being
	 * invalidated.
	 */
	void sessionNotAvailable();

	/**
	 * Flags that failed to retrieve the {@link HttpSession} from the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToRetreiveSession(Throwable cause);

}
