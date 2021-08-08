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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.OptionalThreadLocal;

/**
 * <p>
 * Provides additional means to obtain the {@link ManagedObject} from
 * {@link ThreadLocal}.
 * <p>
 * This is typically used for integrating third party libraries that expect to
 * obtain objects from {@link ThreadLocal} state.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadDependencyMappingBuilder extends DependencyMappingBuilder {

	/**
	 * Obtains the {@link OptionalThreadLocal} for the {@link ManagedObject}.
	 * 
	 * @param <T> Type of object.
	 * @return {@link OptionalThreadLocal} for the {@link ManagedObject}.
	 */
	<T> OptionalThreadLocal<T> getOptionalThreadLocal();

}
