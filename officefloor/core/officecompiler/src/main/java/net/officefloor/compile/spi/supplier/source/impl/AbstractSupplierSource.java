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
package net.officefloor.compile.spi.supplier.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceProperty;
import net.officefloor.compile.spi.supplier.source.SupplierSourceSpecification;

/**
 * Abstract {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSupplierSource implements SupplierSource {

	/*
	 * =============== SupplierSource ===============================
	 */

	@Override
	public SupplierSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the specification
		return specification;
	}

	/**
	 * Loads the {@link SupplierSourceSpecification}.
	 * 
	 * @param context
	 *            {@link SpecificationContext}.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for defining the specification.
	 */
	protected interface SpecificationContext {

		/**
		 * Adds a property.
		 * 
		 * @param name
		 *            Name of property that is also used as the label.
		 */
		void addProperty(String name);

		/**
		 * Adds a property.
		 * 
		 * @param name
		 *            Name of property.
		 * @param label
		 *            Label for the property.
		 */
		void addProperty(String name, String label);

		/**
		 * Adds a property.
		 * 
		 * @param property
		 *            {@link SupplierSourceProperty}.
		 */
		void addProperty(SupplierSourceProperty property);

	}

	/**
	 * {@link SpecificationContext} providing the detail of the
	 * {@link SupplierSourceSpecification}.
	 */
	private static class Specification implements SpecificationContext,
			SupplierSourceSpecification {

		/**
		 * {@link SupplierSourceProperty} instances.
		 */
		private final List<SupplierSourceProperty> properties = new LinkedList<SupplierSourceProperty>();

		/*
		 * ================== SpecificationContext ===========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new SupplierSourcePropertyImpl(name, null));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new SupplierSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(SupplierSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * =================== SupplierSpecification ==============================
		 */

		@Override
		public SupplierSourceProperty[] getProperties() {
			return this.properties.toArray(new SupplierSourceProperty[0]);
		}
	}

	/**
	 * <code>supply</code> to be implemented.
	 */

}