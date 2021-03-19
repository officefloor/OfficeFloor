/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
