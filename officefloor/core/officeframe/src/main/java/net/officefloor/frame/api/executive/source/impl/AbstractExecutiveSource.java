/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.executive.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceProperty;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;

/**
 * Abstract {@link ExecutiveSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractExecutiveSource implements ExecutiveSource {

	/*
	 * ================== ExecutiveSource =====================
	 */

	@Override
	public ExecutiveSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the loaded specification
		return specification;
	}

	/**
	 * Overridden to load specification.
	 * 
	 * @param context {@link SpecificationContext}.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for {@link #loadSpecification(SpecificationContext)}.
	 */
	public static interface SpecificationContext {

		/**
		 * Adds a property.
		 * 
		 * @param name Name of property that is also used as the label.
		 */
		void addProperty(String name);

		/**
		 * Adds a property.
		 * 
		 * @param name  Name of property.
		 * @param label Label for the property.
		 */
		void addProperty(String name, String label);

		/**
		 * Adds a property.
		 * 
		 * @param property {@link ExecutiveSourceProperty}.
		 */
		void addProperty(ExecutiveSourceProperty property);
	}

	/**
	 * Specification for the {@link ExecutiveSource}.
	 */
	private class Specification implements SpecificationContext, ExecutiveSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<ExecutiveSourceProperty> properties = new LinkedList<ExecutiveSourceProperty>();

		/*
		 * ================ SpecificationContext ======================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new ExecutiveSourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new ExecutiveSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(ExecutiveSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ================= ExecutiveSourceSpecification ======================
		 */

		@Override
		public ExecutiveSourceProperty[] getProperties() {
			return this.properties.toArray(new ExecutiveSourceProperty[0]);
		}
	}

}
