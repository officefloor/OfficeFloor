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
