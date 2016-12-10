/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.woof.template.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.model.change.Change;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionChangeContext;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceProperty;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceSpecification;

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