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

package net.officefloor.compile.supplier;

import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * <code>Type definition</code> of a {@link SupplierThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierThreadLocalType extends OptionalThreadLocalReceiver {

	/**
	 * Obtains the type of {@link Object} required.
	 * 
	 * @return Type of {@link Object} required.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the possible qualifier for the required {@link ManagedObject}.
	 * 
	 * @return Qualifier for the required {@link ManagedObject}. May be
	 *         <code>null</code>.
	 */
	String getQualifier();

}
