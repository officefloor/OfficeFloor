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

package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Listens for change in access control (or {@link Escalation} in failing to
 * authenticate).
 * 
 * @author Daniel Sagenschneider
 */
public interface AccessControlListener<AC extends Serializable> {

	/**
	 * Notified of a change to access control.
	 * 
	 * @param accessControl
	 *            Access control. May be <code>null</code> if
	 *            <ul>
	 *            <li>logging out</li>
	 *            <li>failure in authenticating</li>
	 *            </ul>
	 * @param escalation
	 *            Possible {@link Escalation}. Will be <code>null</code> if
	 *            successfully obtain access control or logout.
	 */
	void accessControlChange(AC accessControl, Throwable escalation);

}
