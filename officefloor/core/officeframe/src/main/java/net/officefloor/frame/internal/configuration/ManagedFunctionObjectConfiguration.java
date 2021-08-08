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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Configuration for a dependent {@link Object} of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionObjectConfiguration<O> {

	/**
	 * Indicates if this dependent {@link Object} is the argument passed to the
	 * {@link ManagedFunction}.
	 * 
	 * @return <code>true</code> if is argument passed to the
	 *         {@link ManagedFunction}. <code>false</code> indicates it is a
	 *         {@link ManagedObject} dependency.
	 */
	boolean isParameter();

	/**
	 * <p>
	 * Obtains the name of the {@link ManagedObject} within the
	 * {@link ManagedObjectScope}.
	 * <p>
	 * This must return a value if not a parameter.
	 * 
	 * @return Name of the {@link ManagedObject} within the
	 *         {@link ManagedObjectScope}.
	 */
	String getScopeManagedObjectName();

	/**
	 * Obtains the type of {@link Object} required by the
	 * {@link ManagedFunction}.
	 * 
	 * @return Type of {@link Object} required by the {@link ManagedFunction}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the index identifying the dependent {@link Object}.
	 * 
	 * @return Index identifying the dependent {@link Object}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the dependent {@link Object}.
	 * 
	 * @return Key identifying the dependent {@link Object}. <code>null</code>
	 *         if indexed.
	 */
	O getKey();

}
