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
package net.officefloor.compile.supplier;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * <code>Type definition</code> of a {@link SupplierThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierThreadLocalType {

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