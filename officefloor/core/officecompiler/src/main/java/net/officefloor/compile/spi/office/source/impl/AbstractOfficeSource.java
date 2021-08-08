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

package net.officefloor.compile.spi.office.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceProperty;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;

/**
 * Abstract {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeSource implements OfficeSource {

	/*
	 * =============== OfficeSource ==================================
	 */

	@Override
	public OfficeSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the specification
		return specification;
	}

	/**
	 * Loads the {@link OfficeSourceSpecification}.
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
		 *            {@link OfficeSourceProperty}.
		 */
		void addProperty(OfficeSourceProperty property);

	}

	/**
	 * {@link SpecificationContext} providing the detail of the
	 * {@link OfficeSourceSpecification}.
	 */
	private static class Specification implements SpecificationContext,
			OfficeSourceSpecification {

		/**
		 * {@link OfficeSourceProperty} instances.
		 */
		private final List<OfficeSourceProperty> properties = new LinkedList<OfficeSourceProperty>();

		/*
		 * ================== SpecificationContext ===========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new OfficeSourcePropertyImpl(name, null));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new OfficeSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(OfficeSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * =================== OfficeSpecification ===========================
		 */

		@Override
		public OfficeSourceProperty[] getProperties() {
			return this.properties.toArray(new OfficeSourceProperty[0]);
		}
	}

	/**
	 * <code>sourceOffice</code> to be implemented.
	 */

}
