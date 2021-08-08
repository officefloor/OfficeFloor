/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of a dependency required by a
 * {@link ManagedFunction} added by {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFunctionDependencyType {

	/**
	 * Obtains the name of the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @return Name of the {@link ManagedObjectFunctionDependency}.
	 */
	String getFunctionObjectName();

	/**
	 * Obtains the type of the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @return Type of the {@link ManagedObjectFunctionDependency}.
	 */
	Class<?> getFunctionObjectType();

	/**
	 * Obtains the type qualifier for the {@link ManagedObjectFunctionDependency}.
	 * 
	 * @return Type qualifier for the {@link ManagedObjectFunctionDependency}.
	 */
	String getFunctionObjectTypeQualifier();

}
