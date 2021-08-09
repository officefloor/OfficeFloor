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

/*
 * Created on Nov 30, 2005
 */
package net.officefloor.frame.api.managedobject;

/**
 * <p>
 * Registry providing the dependent Object instances for a
 * {@link CoordinatingManagedObject} instance.
 * <p>
 * This is provided by the OfficeFloor implementation.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectRegistry<O extends Enum<O>> {

	/**
	 * Obtains the dependency {@link Object} for the dependency key.
	 * 
	 * @param key
	 *            Key identifying the dependency {@link Object}.
	 * @return Dependency {@link Object} for the key.
	 */
	Object getObject(O key);

	/**
	 * <p>
	 * Obtains the dependency {@link Object} by its index.
	 * <p>
	 * This enables a dynamic number of dependencies for the
	 * {@link ManagedObject}.
	 * 
	 * @param index
	 *            Index identifying the dependency {@link Object}.
	 * @return Dependency {@link Object} for the index.
	 */
	Object getObject(int index);

}
