/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.template.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.model.change.Change;
import net.officefloor.woof.template.WoofTemplateExtensionChangeContext;
import net.officefloor.woof.template.WoofTemplateExtensionSource;
import net.officefloor.woof.template.WoofTemplateExtensionSourceProperty;
import net.officefloor.woof.template.WoofTemplateExtensionSourceSpecification;

/**
 * Abstract {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWoofTemplateExtensionSource implements
		WoofTemplateExtensionSource {

	/*
	 * =================== WoofTemplateExtensionSource =================
	 */

	@Override
	public WoofTemplateExtensionSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the loaded specification
		return specification;
	}

	/**
	 * Overridden to load specifications.
	 * 
	 * @param context
	 *            Specifications.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for the {@link WoofTemplateExtensionSource#getSpecification()}.
	 */
	public static interface SpecificationContext {

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
		 *            {@link WoofTemplateExtensionSourceProperty}.
		 */
		void addProperty(WoofTemplateExtensionSourceProperty property);
	}

	/**
	 * Specification for this {@link WoofTemplateExtensionSource}.
	 */
	private class Specification implements SpecificationContext,
			WoofTemplateExtensionSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<WoofTemplateExtensionSourceProperty> properties = new LinkedList<WoofTemplateExtensionSourceProperty>();

		/*
		 * ========== SpecificationContext ========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new WoofTemplateExtensionSourcePropertyImpl(
					name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new WoofTemplateExtensionSourcePropertyImpl(
					name, label));
		}

		@Override
		public void addProperty(WoofTemplateExtensionSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ========== WoofTemplateExtensionSourceSpecification ===========
		 */

		@Override
		public WoofTemplateExtensionSourceProperty[] getProperties() {
			return this.properties
					.toArray(new WoofTemplateExtensionSourceProperty[0]);
		}
	}

	@Override
	public Change<?> createConfigurationChange(
			WoofTemplateExtensionChangeContext context) {
		// By default no change is necessary
		return null;
	}

}
