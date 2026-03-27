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
