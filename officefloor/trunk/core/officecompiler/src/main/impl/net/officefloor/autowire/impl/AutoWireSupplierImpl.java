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
package net.officefloor.autowire.impl;

import net.officefloor.autowire.AutoWireSupplier;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;

/**
 * {@link AutoWireSupplier} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireSupplierImpl extends AutoWirePropertiesImpl implements
		AutoWireSupplier {

	/**
	 * {@link SupplierSource} class name.
	 */
	private final String supplierSourceClassName;

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param supplierSourceClassName
	 *            {@link SupplierSource} class name.
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public AutoWireSupplierImpl(OfficeFloorCompiler compiler,
			String supplierSourceClassName, PropertyList properties) {
		super(compiler, properties);
		this.supplierSourceClassName = supplierSourceClassName;
	}

	/*
	 * ================= SupplierSource ==============================
	 */

	@Override
	public String getSupplierSourceClassName() {
		return this.supplierSourceClassName;
	}

}