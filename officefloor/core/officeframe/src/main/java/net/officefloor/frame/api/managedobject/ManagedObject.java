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

package net.officefloor.frame.api.managedobject;

/**
 * <p>
 * Core interface of a Managed Object.
 * <p>
 * Additional managed functionality is available by implementing the following
 * interfaces:
 * <ol>
 * <li>{@link ContextAwareManagedObject}</li>
 * <li>{@link AsynchronousManagedObject}</li>
 * <li>{@link CoordinatingManagedObject}</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObject {

	/**
	 * Obtains the object being managed.
	 * 
	 * @return Object being managed.
	 * @throws Throwable
	 *             Indicating failed to obtain the object for use.
	 */
	Object getObject() throws Throwable;

}
