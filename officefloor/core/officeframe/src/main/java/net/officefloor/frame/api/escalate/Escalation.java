/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Internal failures within the {@link OfficeFrame} extend {@link Escalation}.
 * <p>
 * However, all {@link Throwable} instances thrown from {@link ManagedFunction}
 * and {@link ManagedObject} instances are considered to follow the
 * {@link Escalation} paradigm. This is that the invoker need not deal with
 * {@link Escalation} instances, and these are handled by other
 * {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class Escalation extends Throwable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public Escalation() {
		super();
	}

	/**
	 * Allows for a cause of the {@link Escalation}.
	 * 
	 * @param cause Cause of the {@link Escalation}.
	 */
	public Escalation(Throwable cause) {
		super(cause);
	}

}
