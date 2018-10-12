/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.supplier;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.supplier.SupplierThreadLocalType;

/**
 * {@link SupplierThreadLocalType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierThreadLocalTypeImpl<T> implements SupplierThreadLocalType {

	/**
	 * {@link SupplierThreadLocal} instances for this
	 * {@link SupplierThreadLocalType}.
	 */
	private final SupplierThreadLocalImpl<T> supplierThreadLocal = new SupplierThreadLocalImpl<>();

	/**
	 * Qualifier. May be <code>null</code>.
	 */
	private final String qualifier;

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * Instantiate.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Object type.
	 */
	public SupplierThreadLocalTypeImpl(String qualifier, Class<?> objectType) {
		this.qualifier = qualifier;
		this.objectType = objectType;
	}

	/**
	 * Obtains the {@link SupplierThreadLocal}.
	 * 
	 * @return {@link SupplierThreadLocal}.
	 */
	public SupplierThreadLocal<T> getSupplierThreadLocal() {
		return (SupplierThreadLocal<T>) this.supplierThreadLocal;
	}

	/*
	 * =================== SupplierThreadLocalType =====================
	 */

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	/**
	 * {@link SupplierThreadLocal} implementation.
	 */
	public static class SupplierThreadLocalImpl<T> implements SupplierThreadLocal<T> {

		@Override
		public T get() {
			// TODO implement SupplierThreadLocal<T>.get(...)
			throw new UnsupportedOperationException("TODO implement SupplierThreadLocal<T>.get(...)");
		}
	}

}