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
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link Escalation} indicating that the {@link ManagedObjectSource} was timed
 * out in providing a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceManagedObjectTimedOutEscalation extends ManagedObjectEscalation {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 * 
	 * @param objectType {@link Class} of the {@link Object} returned from the timed
	 *                   out {@link ManagedObject}.
	 */
	public SourceManagedObjectTimedOutEscalation(Class<?> objectType) {
		super(objectType);
	}

}
