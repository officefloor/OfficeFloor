/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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
