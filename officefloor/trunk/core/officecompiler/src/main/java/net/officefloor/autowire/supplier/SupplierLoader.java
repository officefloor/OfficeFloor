/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.autowire.supplier;

import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceProperty;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;

/**
 * Loads the {@link SupplierType} from the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link SupplierSourceSpecification} for the {@link SupplierSource}.
	 * 
	 * @param supplierSourceClass
	 *            {@link SupplierSource} class.
	 * @return {@link PropertyList} of the {@link SupplierSourceProperty}
	 *         instances of the {@link SupplierSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<S extends SupplierSource> PropertyList loadSpecification(
			Class<S> supplierSourceClass);

	/**
	 * Loads and returns {@link SupplierType} for the {@link SupplierSource}.
	 * 
	 * @param supplierSourceClass
	 *            Class of the {@link SupplierSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link SupplierType}.
	 * @return {@link SupplierType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<S extends SupplierSource> SupplierType loadSupplierType(
			Class<S> supplierSourceClass, PropertyList propertyList);

	/**
	 * Fills the {@link SupplyOrder} instances with a
	 * {@link SuppliedManagedObject}.
	 * 
	 * @param supplierSourceClass
	 *            Class of the {@link SupplierSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to configure
	 *            the {@link SupplierSource} to fill the {@link SupplyOrder}
	 *            instances.
	 * @param supplyOrders
	 *            {@link SupplyOrder} instances to be filled.
	 */
	<S extends SupplierSource> void fillSupplyOrders(
			Class<S> supplierSourceClass, PropertyList propertyList,
			SupplyOrder... supplyOrders);

}