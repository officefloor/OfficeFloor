/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.configurer.internal.inputs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.PropertiesBuilder;
import net.officefloor.eclipse.configurer.ValueLoader;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.configurer.ValueValidator.ValueValidatorContext;
import net.officefloor.eclipse.configurer.internal.ValueInput;
import net.officefloor.eclipse.configurer.internal.ValueRenderer;
import net.officefloor.eclipse.configurer.internal.ValueRendererContext;
import net.officefloor.eclipse.configurer.internal.ValueRendererFactory;

/**
 * {@link PropertiesBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertiesBuilderImpl<M> implements PropertiesBuilder<M>, ValueRendererFactory<M, ValueInput> {

	/**
	 * {@link Property} item.
	 */
	public static class PropertyItem {

		/**
		 * Name.
		 */
		private String name;

		/**
		 * Value.
		 */
		private String value;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            Name.
		 * @param value
		 *            Value.
		 */
		private PropertyItem(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

	/**
	 * {@link ListBuilder} for the {@link PropertyList}.
	 */
	private final ListBuilderImpl<M, PropertyItem> list;

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 */
	public PropertiesBuilderImpl(String label) {
		this.list = new ListBuilderImpl<>(label);

		// Configure property
		this.list.text("Name").init((property) -> property.name).setValue((property, value) -> property.name = value);
		this.list.text("Value").init((property) -> property.value)
				.setValue((property, value) -> property.value = value);

		// Provide means to add/remove properties
		this.list.addItem(() -> new PropertyItem("", "")).deleteItem();
	}

	/*
	 * ================== PropertiesBuilder ======================
	 */

	@Override
	public PropertiesBuilder<M> required(ObjectProperty<PropertyList> required) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertiesBuilder<M> init(Function<M, PropertyList> getInitialValue) {
		this.list.init((model) -> {

			// Obtain the properties
			PropertyList propertyList = getInitialValue.apply(model);

			// Translate properties into property items
			List<PropertyItem> items = new ArrayList<>();
			for (Property property : propertyList) {
				items.add(new PropertyItem(property.getName(), property.getValue()));
			}

			// Return the property items
			return items;
		});
		return this;
	}

	@Override
	public PropertiesBuilder<M> validate(ValueValidator<PropertyList> validator) {
		this.list.validate((context) -> {

			// Obtain the property items
			List<PropertyItem> items = context.getValue().getValue();

			// Translate to properties
			PropertyList properties = OfficeFloorCompiler.newPropertyList();
			for (PropertyItem item : items) {
				properties.addProperty(item.name).setValue(item.value);
			}
			SimpleObjectProperty<PropertyList> propertiesProperty = new SimpleObjectProperty<>(properties);

			// Validate the properties
			validator.validate(new ValueValidatorContext<PropertyList>() {

				@Override
				public void setError(String message) {
					context.setError(message);
				}

				@Override
				public ReadOnlyProperty<PropertyList> getValue() {
					return propertiesProperty;
				}
			});
		});
		return this;
	}

	@Override
	public PropertiesBuilder<M> setValue(ValueLoader<M, PropertyList> valueLoader) {
		this.list.setValue((model, items) -> {

			// Translate to properties
			PropertyList properties = OfficeFloorCompiler.newPropertyList();
			for (PropertyItem item : items) {
				properties.addProperty(item.name).setValue(item.value);
			}

			// Load the properties
			valueLoader.loadValue(model, properties);
		});
		return this;
	}

	/*
	 * ================= ValueRendererFactory =======================
	 */

	@Override
	public ValueRenderer<M, ValueInput> createValueRenderer(ValueRendererContext<M> context) {
		return this.list.createValueRenderer(context);
	}

}