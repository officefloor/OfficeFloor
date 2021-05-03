/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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

package net.officefloor.gef.configurer.internal.inputs;

import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import net.officefloor.gef.configurer.SelectBuilder;
import net.officefloor.gef.configurer.internal.AbstractBuilder;
import net.officefloor.gef.configurer.internal.ValueInput;
import net.officefloor.gef.configurer.internal.ValueInputContext;

/**
 * {@link SelectBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SelectBuilderImpl<M, I> extends AbstractBuilder<M, I, ValueInput, SelectBuilder<M, I>>
		implements SelectBuilder<M, I> {

	/**
	 * Obtains the items from the model.
	 */
	private final Function<M, ObservableList<I>> getItems;

	/**
	 * <p>
	 * Obtains label from item.
	 * <p>
	 * Default implementation to take {@link Object#toString()} of item.
	 */
	private Function<I, String> getLabel = (item) -> item == null ? "" : item.toString();

	/**
	 * Instantiate.
	 * 
	 * @param label Label.
	 */
	public SelectBuilderImpl(String label, Function<M, ObservableList<I>> getItems) {
		super(label);
		this.getItems = getItems;
	}

	/*
	 * ============= SelectBuilder ==============
	 */

	@Override
	public SelectBuilder<M, I> itemLabel(Function<I, String> getLabel) {
		this.getLabel = getLabel;
		return this;
	}

	/*
	 * ============= AbstractBuilder ==============
	 */

	@Override
	protected ValueInput createInput(ValueInputContext<M, I> context) {

		// Obtain the value
		Property<I> value = context.getInputValue();

		// Determine if can edit the value
		if (this.isEditable()) {

			// Obtain the items
			M model = context.getModel();
			ObservableList<I> items = this.getItems.apply(model);

			// Provide editable text box
			ChoiceBox<I> choices = new ChoiceBox<>();
			choices.setItems(items);
			choices.setConverter(new StringConverter<I>() {

				@Override
				public String toString(I item) {
					return SelectBuilderImpl.this.getLabel.apply(item);
				}

				@Override
				public I fromString(String label) {
					for (I item : items) {
						if (SelectBuilderImpl.this.getLabel.apply(item).equals(label)) {
							return item;
						}
					}
					return null; // not found
				}
			});
			choices.valueProperty().bindBidirectional(value);
			choices.getStyleClass().add("configurer-input-choices");
			return () -> choices;

		} else {

			// Provide label (can not edit)
			Label label = new Label(this.getLabel.apply(value.getValue()));
			label.getStyleClass().add("configurer-input-label");
			return () -> label;
		}
	}

}
