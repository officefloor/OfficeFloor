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

import net.officefloor.web.session.HttpSession;

/**
 * Operation to obtain details of invalidating a {@link HttpSession} within the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface InvalidateHttpSessionOperation {

	/**
	 * Obtains the session Id of the {@link HttpSession} to invalidate.
	 *
	 * @return Session Id of the {@link HttpSession} to invalidate.
	 */
	String getSessionId();

	/**
	 * Flags the {@link HttpSession} was invalidated successfully within the
	 * {@link HttpSessionStore}.
	 */
	void sessionInvalidated();

	/**
	 * Flags failed to invalidate the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToInvalidateSession(Throwable cause);

}
