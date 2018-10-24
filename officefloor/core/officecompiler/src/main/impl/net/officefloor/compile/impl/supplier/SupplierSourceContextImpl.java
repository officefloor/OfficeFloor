/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.supplier;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link SupplierSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierSourceContextImpl extends SourceContextImpl implements SupplierSourceContext {

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link SupplierThreadLocalTypeImpl} instances.
	 */
	private final List<SupplierThreadLocalTypeImpl<?>> supplierThreadLocals = new LinkedList<>();

	/**
	 * {@link SuppliedManagedObjectSourceImpl} instances.
	 */
	private final List<SuppliedManagedObjectSourceTypeImpl> suppliedManagedObjectSources = new LinkedList<>();

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType Indicates if loading type.
	 * @param propertyList  {@link PropertyList}.
	 * @param context       {@link NodeContext}.
	 */
	public SupplierSourceContextImpl(boolean isLoadingType, PropertyList propertyList, NodeContext context) {
		super(isLoadingType, context.getRootSourceContext(), new PropertyListSourceProperties(propertyList));
		this.context = context;
	}

	/**
	 * Obtains the {@link SupplierThreadLocalType} instances.
	 * 
	 * @return {@link SupplierThreadLocalType} instances.
	 */
	public SupplierThreadLocalType[] getSupplierThreadLocalTypes() {
		return this.supplierThreadLocals.stream().toArray(SupplierThreadLocalType[]::new);
	}

	/**
	 * Obtains the {@link SuppliedManagedObjectSourceType} instances.
	 * 
	 * @return {@link SuppliedManagedObjectSourceType} instances.
	 */
	public SuppliedManagedObjectSourceType[] getSuppliedManagedObjectSourceTypes() {
		return this.suppliedManagedObjectSources.stream().toArray(SuppliedManagedObjectSourceType[]::new);
	}

	/*
	 * ====================== SupplierSourceContext =====================
	 */

	@Override
	public <T> SupplierThreadLocal<T> addSupplierThreadLocal(String qualifier, Class<? extends T> type) {
		SupplierThreadLocalTypeImpl<T> supplierThreadLocal = new SupplierThreadLocalTypeImpl<>(qualifier, type);
		this.supplierThreadLocals.add(supplierThreadLocal);
		return supplierThreadLocal.getSupplierThreadLocal();
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>> SuppliedManagedObjectSource addManagedObjectSource(String qualifier,
			Class<?> type, ManagedObjectSource<D, F> managedObjectSource) {

		// Create the supplied managed object source
		PropertyList properties = this.context.createPropertyList();
		SuppliedManagedObjectSourceTypeImpl supplied = new SuppliedManagedObjectSourceTypeImpl(type, qualifier,
				managedObjectSource, properties);

		// Register the supplied managed object source
		this.suppliedManagedObjectSources.add(supplied);

		// Return the managed object source for configuring
		return supplied;
	}

}