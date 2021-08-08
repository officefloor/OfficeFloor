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

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * {@link Escalation} from managing a {@link ManagedObject}.
 * <p>
 * This enables generic handling of {@link ManagedObject} {@link Escalation}
 * failures.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ManagedObjectEscalation extends Escalation {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link Class} of the {@link Object} returned from the failed
	 * {@link ManagedObject}.
	 */
	private final Class<?> objectType;

	/**
	 * Initiate.
	 * 
	 * @param objectType {@link Class} of the {@link Object} returned from the
	 *                   failed {@link ManagedObject}.
	 */
	public ManagedObjectEscalation(Class<?> objectType) {
		this.objectType = objectType;
	}

	/**
	 * Allows for a cause of the {@link Escalation}.
	 * 
	 * @param objectType {@link Class} of the {@link Object} returned from the
	 *                   failed {@link ManagedObject}.
	 * @param cause      Cause of the {@link Escalation}.
	 */
	public ManagedObjectEscalation(Class<?> objectType, Throwable cause) {
		super(cause);
		this.objectType = objectType;
	}

	/**
	 * Obtains the {@link Class} of the {@link Object} returned from the failed
	 * {@link ManagedObject}.
	 * 
	 * @return {@link Class} of the {@link Object} returned from the failed
	 *         {@link ManagedObject}.
	 */
	public Class<?> getObjectType() {
		return this.objectType;
	}

}
