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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;

/**
 * Provides configuration for the {@link OptionalThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalConfiguration {

	/**
	 * Specifies the {@link ManagedObjectIndex} identifying the
	 * {@link ManagedObject} for the {@link OptionalThreadLocal}.
	 * 
	 * @param managedObjectIndex {@link ManagedObjectIndex} identifying the
	 *                           {@link ManagedObject} for the
	 *                           {@link OptionalThreadLocal}.
	 */
	void setManagedObjectIndex(ManagedObjectIndex managedObjectIndex);

}
