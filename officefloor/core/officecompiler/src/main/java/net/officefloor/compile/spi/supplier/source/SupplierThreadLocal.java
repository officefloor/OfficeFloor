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

package net.officefloor.compile.spi.supplier.source;

import java.util.function.Supplier;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Provides {@link ThreadLocal} access to the {@link ManagedObject} object
 * instances for the {@link SuppliedManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierThreadLocal<T> extends Supplier<T> {

	/**
	 * <p>
	 * Obtains the object for the respective {@link ManagedObject} this represents.
	 * <p>
	 * This is only to be used within the {@link SuppliedManagedObjectSource}
	 * {@link ManagedObject} implementations. Within this scope, the object will
	 * always be returned. Used outside this scope, the result is unpredictable.
	 * 
	 * @return Object from the {@link ManagedObject}.
	 */
	T get();

}
