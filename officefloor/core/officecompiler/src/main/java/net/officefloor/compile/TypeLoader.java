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

package net.officefloor.compile;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Encapsulates {@link ClassLoader} handling to load the various
 * {@link OfficeFloor} types.
 * <p>
 * As the {@link OfficeFloorCompiler} being used may be loading {@link Class}
 * instances via an alternate {@link ClassLoader}, this interface provides means
 * to use that alternate {@link ClassLoader} to load the necessary
 * {@link OfficeFloor} types.
 * 
 * @author Daniel Sagenschneider
 */
public interface TypeLoader {

	/**
	 * Loads the {@link FunctionNamespaceType}.
	 * 
	 * @param managedFunctionName            Name of {@link ManagedFunctionSource}.
	 * @param managedFunctionSourceClassName {@link ManagedFunctionSource} class
	 *                                       name.
	 * @param properties                     {@link PropertyList}.
	 * @return {@link FunctionNamespaceType}.
	 */
	FunctionNamespaceType loadManagedFunctionType(String managedFunctionName, String managedFunctionSourceClassName,
			PropertyList properties);

	/**
	 * Loads the {@link ManagedObjectType}.
	 * 
	 * @param managedObjectName            Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClassName {@link ManagedObjectSource} class name.
	 * @param properties                   {@link PropertyList}.
	 * @return {@link ManagedObjectType}.
	 */
	ManagedObjectType<?> loadManagedObjectType(String managedObjectName, String managedObjectSourceClassName,
			PropertyList properties);

}
