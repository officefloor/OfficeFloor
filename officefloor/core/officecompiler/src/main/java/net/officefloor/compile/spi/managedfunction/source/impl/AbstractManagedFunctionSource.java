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

package net.officefloor.compile.spi.managedfunction.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceProperty;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;

/**
 * Abstract {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractManagedFunctionSource implements ManagedFunctionSource {

	/*
	 * =============== ManagedFunctionSource =======================
	 */

	@Override
	public ManagedFunctionSourceSpecification getSpecification() {

		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the specification
		return specification;
	}

	/**
	 * Loads the {@link ManagedFunctionSourceSpecification}.
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
		 *            {@link ManagedFunctionSourceProperty}.
		 */
		void addProperty(ManagedFunctionSourceProperty property);

	}

	/**
	 * {@link SpecificationContext} providing the detail of the
	 * {@link ManagedFunctionSourceSpecification}.
	 */
	private static class Specification implements SpecificationContext, ManagedFunctionSourceSpecification {

		/**
		 * {@link ManagedFunctionSourceProperty} instances.
		 */
		private final List<ManagedFunctionSourceProperty> properties = new LinkedList<ManagedFunctionSourceProperty>();

		/*
		 * ================== SpecificationContext ===========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new ManagedFunctionSourcePropertyImpl(name, null));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new ManagedFunctionSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(ManagedFunctionSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * =================== WorkSpecification ==============================
		 */

		@Override
		public ManagedFunctionSourceProperty[] getProperties() {
			return this.properties.toArray(new ManagedFunctionSourceProperty[0]);
		}
	}

}
