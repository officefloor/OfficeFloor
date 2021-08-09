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

package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Describes an object which the {@link ManagedObject} for the
 * {@link ManagedObjectSource} is dependent upon.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependencyMetaData<O extends Enum<O>> {

	/**
	 * Obtains the {@link Enum} key identifying this dependency. If
	 * <code>null</code> then dependency will be referenced by this instance's index
	 * in the array returned from {@link ManagedObjectSourceMetaData}.
	 * 
	 * @return {@link Enum} key identifying the dependency or <code>null</code>
	 *         indicating identified by an index.
	 */
	O getKey();

	/**
	 * Obtains the {@link Class} that the dependent object must extend/implement.
	 * 
	 * @return Type of the dependency.
	 */
	Class<?> getType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying the
	 *         type.
	 */
	String getTypeQualifier();

	/**
	 * <p>
	 * Obtains the annotations for the dependency.
	 * <p>
	 * This enables further description of required dependency.
	 * 
	 * @return Annotations for the dependency.
	 */
	Object[] getAnnotations();

	/**
	 * Provides a descriptive name for this dependency. This is useful to better
	 * describe the dependency.
	 * 
	 * @return Descriptive name for this dependency.
	 */
	String getLabel();

}
