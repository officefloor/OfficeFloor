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
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
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
	 * {@link SuppliedManagedObjectSourceImpl} instances.
	 */
	private final List<SuppliedManagedObjectSourceImpl> suppliedManagedObjectSources = new LinkedList<>();

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SupplierSourceContextImpl(boolean isLoadingType, PropertyList propertyList, NodeContext context) {
		super(isLoadingType, context.getRootSourceContext(), new PropertyListSourceProperties(propertyList));
		this.context = context;
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
	public <D extends Enum<D>, F extends Enum<F>> SuppliedManagedObjectSource addManagedObjectSource(Class<?> type,
			ManagedObjectSource<D, F> managedObjectSource) {
		return this.addManagedObjectSource(null, type, managedObjectSource);
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>> SuppliedManagedObjectSource addManagedObjectSource(String qualifier,
			Class<?> type, ManagedObjectSource<D, F> managedObjectSource) {

		// Create the supplied managed object source
		PropertyList properties = this.context.createPropertyList();
		SuppliedManagedObjectSourceImpl supplied = new SuppliedManagedObjectSourceImpl(type, qualifier,
				managedObjectSource, properties);

		// Register the supplied managed object source
		this.suppliedManagedObjectSources.add(supplied);

		// Return the managed object source for configuring
		return supplied;
	}

	/**
	 * {@link SuppliedManagedObjectSource} implementation and corresponding
	 * {@link SuppliedManagedObjectSourceType}.
	 */
	private static class SuppliedManagedObjectSourceImpl
			implements SuppliedManagedObjectSource, SuppliedManagedObjectSourceType {

		/**
		 * Object type.
		 */
		private final Class<?> objectType;

		/**
		 * Qualifier. May be <code>null</code>.
		 */
		private final String qualifier;

		/**
		 * {@link ManagedObjectSource}.
		 */
		private final ManagedObjectSource<?, ?> managedObjectSource;

		/**
		 * {@link PropertyList}.
		 */
		private final PropertyList properties;

		/**
		 * Initiate.
		 * 
		 * @param objectType
		 *            Object type.
		 * @param qualifier
		 *            Qualifier. May be <code>null</code>.
		 * @param managedObjectSource
		 *            {@link ManagedObjectSource}.
		 * @param properties
		 *            {@link PropertyList}.
		 */
		public SuppliedManagedObjectSourceImpl(Class<?> objectType, String qualifier,
				ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties) {
			this.objectType = objectType;
			this.qualifier = qualifier;
			this.managedObjectSource = managedObjectSource;
			this.properties = properties;
		}

		/*
		 * ===================== SuppliedManagedObjectSource ================
		 */

		@Override
		public void addProperty(String name, String value) {
			this.properties.addProperty(name).setValue(value);
		}

		/*
		 * =================== SuppliedManagedObjectSourceType ==============
		 */

		@Override
		public Class<?> getObjectType() {
			return this.objectType;
		}

		@Override
		public String getQualifier() {
			return this.qualifier;
		}

		@Override
		public ManagedObjectSource<?, ?> getManagedObjectSource() {
			return this.managedObjectSource;
		}

		@Override
		public PropertyList getPropertyList() {
			return this.properties;
		}
	}

}