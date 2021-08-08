/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * <p>
 * {@link Escalation} indicating authentication is required.
 * <p>
 * This may be thrown by any functionality as the {@link WebArchitect} is
 * expected to catch this {@link Escalation} at the {@link Office} level and
 * issue a challenge to the client.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticationRequiredException extends RuntimeException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the required {@link HttpSecurity}.
	 */
	private final String requiredHttpSecurityName;

	/**
	 * Initiate for any {@link HttpSecurity}.
	 */
	public AuthenticationRequiredException() {
		this(null);
	}

	/**
	 * Initiate for a specific {@link HttpSecurity}.
	 * 
	 * @param requiredHttpSecurityName Name of the specific {@link HttpSecurity}
	 *                                 required. May be <code>null</code> to
	 *                                 indicate any configured {@link HttpSecurity}.
	 */
	public AuthenticationRequiredException(String requiredHttpSecurityName) {
		this.requiredHttpSecurityName = requiredHttpSecurityName;
	}

	/**
	 * Obtains the required {@link HttpSecurity} name.
	 * 
	 * @return Required {@link HttpSecurity} name or <code>null</code> if any
	 *         {@link HttpSecurity}.
	 */
	public String getRequiredHttpSecurityName() {
		return this.requiredHttpSecurityName;
	}

}
