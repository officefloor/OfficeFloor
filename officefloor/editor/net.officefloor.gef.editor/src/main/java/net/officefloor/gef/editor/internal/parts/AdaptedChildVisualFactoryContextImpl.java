/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor.internal.parts;

import java.util.function.Function;
import java.util.function.Supplier;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.model.Model;

/**
 * {@link AdaptedChildVisualFactoryContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedChildVisualFactoryContextImpl<M extends Model> extends AdaptedModelVisualFactoryContextImpl<M>
		implements AdaptedChildVisualFactoryContext<M> {

	/**
	 * {@link Function} interface to register the child group.
	 */
	public static interface ChildGroupRegistrator {

		/**
		 * Registers the child group.
		 * 
		 * @param childGroupName Name of the child group.
		 * @param parent         Parent {@link Pane} for the child group.
		 * @return <code>true</code> if registered.
		 */
		boolean registerChildGroup(String childGroupName, Pane parent);
	}

	/**
	 * {@link Supplier} of the label.
	 */
	private final Supplier<ReadOnlyProperty<String>> label;

	/**
	 * {@link ChildGroupRegistrator}.
	 */
	private final ChildGroupRegistrator childGroupRegistrator;

	/**
	 * Instantiate.
	 * 
	 * @param modelClass            {@link Class} of the {@link Model}.
	 * @param isPalettePrototype    Indicates if rendering the palette prototype.
	 * @param label                 {@link Supplier} of the label.
	 * @param childGroupRegistrator {@link ChildGroupRegistrator}.
	 * @param connectorLoader       {@link ConnectorLoader}.
	 * @param actioner              {@link Actioner}.
	 */
	public AdaptedChildVisualFactoryContextImpl(Class<M> modelClass, boolean isPalettePrototype,
			Supplier<ReadOnlyProperty<String>> label, ChildGroupRegistrator childGroupRegistrator,
			ConnectorLoader<M> connectorLoader, Actioner<M> actioner) {
		super(modelClass, isPalettePrototype, connectorLoader, actioner);
		this.label = label;
		this.childGroupRegistrator = childGroupRegistrator;
	}

	@Override
	public Label label(Pane parent) {
		// Ensure label is configured
		ReadOnlyProperty<String> labelProperty = this.label.get();
		if (labelProperty == null) {
			throw new IllegalStateException("No label configured for visual for model " + this.modelClass.getName());
		}

		// Configure the label
		Label label = this.addNode(parent, new Label());
		label.textProperty().bind(labelProperty);
		return label;
	}

	@Override
	public <P extends Pane> P childGroup(String childGroupName, P parent) {
		if (childGroupName == null) {
			throw new NullPointerException("No child group name provided for view of " + this.modelClass.getName());
		}

		// Register the child group
		if (this.childGroupRegistrator.registerChildGroup(childGroupName, parent)) {
			return parent;
		}

		// As here, no children group registered
		throw new IllegalStateException(
				"No children group '" + childGroupName + "' registered for view of model " + this.modelClass.getName());
	}

	@Override
	public boolean isPalettePrototype() {
		return this.isPalettePrototype;
	}

}
