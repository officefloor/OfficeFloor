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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.PropertiesBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;
import net.officefloor.eclipse.configurer.internal.ValueInput;
import net.officefloor.eclipse.configurer.internal.ValueRenderer;

/**
 * {@link PropertiesBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertiesBuilderImpl<M> extends AbstractBuilder<M, PropertyList, PropertiesBuilder<M>>
		implements PropertiesBuilder<M>, ValueRenderer<M> {

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
		super(label);
		this.list = new ListBuilderImpl<>(label);

		// Configure property
		this.list.text("Name").init((property) -> property.name).setValue((property, value) -> property.name = value);
		this.list.text("Value").init((property) -> property.value)
				.setValue((property, value) -> property.value = value);

		// Provide means to add/remove properties
		this.list.addItem(() -> new PropertyItem("", "")).deleteItem();
	}

	/*
	 * ================== AbstractBuilder ======================
	 */

	@Override
	protected ValueInput createInput(javafx.beans.property.Property<PropertyList> value) {

		// Load the properties
		List<PropertyItem> items = new ArrayList<>();
		for (Property property : value.getValue()) {
			items.add(new PropertyItem(property.getName(), property.getValue()));
		}

		// Return the created list of properties
		return this.list.createInput(new SimpleObjectProperty<>(items));
	}

	/*
	 * ================== PropertiesBuilder ======================
	 */

	@Override
	public PropertiesBuilder<M> required(ObjectProperty<PropertyList> required) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * =============== ValueRenderer ======================
	 */

	@Override
	public Node createErrorFeedback() {
		return this.list.createErrorFeedback();
	}

	@Override
	public Throwable getError() {
		return this.list.getError();
	}

	@Override
	public void loadValue(M model) {
		this.list.loadValue(model);
	}

}