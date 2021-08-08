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

package net.officefloor.compile.spi.officefloor.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceProperty;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceSpecification;

/**
 * Abstract {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeFloorSource implements OfficeFloorSource {

	/*
	 * =============== OfficeFloorSource ==================================
	 */

	@Override
	public OfficeFloorSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the specification
		return specification;
	}

	/**
	 * Loads the {@link OfficeFloorSourceSpecification}.
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
		 *            {@link OfficeFloorSourceProperty}.
		 */
		void addProperty(OfficeFloorSourceProperty property);

	}

	/**
	 * {@link SpecificationContext} providing the detail of the
	 * {@link OfficeFloorSourceSpecification}.
	 */
	private static class Specification implements SpecificationContext,
			OfficeFloorSourceSpecification {

		/**
		 * {@link OfficeFloorSourceProperty} instances.
		 */
		private final List<OfficeFloorSourceProperty> properties = new LinkedList<OfficeFloorSourceProperty>();

		/*
		 * ================== SpecificationContext ===========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new OfficeFloorSourcePropertyImpl(name, null));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new OfficeFloorSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(OfficeFloorSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ================ OfficeFloorSpecification ========================
		 */

		@Override
		public OfficeFloorSourceProperty[] getProperties() {
			return this.properties.toArray(new OfficeFloorSourceProperty[0]);
		}
	}

	/**
	 * <code>sourceOfficeFloor</code> to be implemented.
	 */

}
