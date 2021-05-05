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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.ChoiceBuilder;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.internal.AbstractBuilder;
import net.officefloor.gef.configurer.internal.AbstractConfigurationBuilder;
import net.officefloor.gef.configurer.internal.ChoiceValueInput;
import net.officefloor.gef.configurer.internal.ValueInput;
import net.officefloor.gef.configurer.internal.ValueInputContext;
import net.officefloor.gef.configurer.internal.ValueRendererFactory;

/**
 * {@link ChoiceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ChoiceBuilderImpl<M> extends AbstractBuilder<M, Integer, ChoiceValueInput<M>, ChoiceBuilder<M>>
		implements ChoiceBuilder<M> {

	/**
	 * {@link EnvironmentBridge}.
	 */
	private final EnvironmentBridge envBridge;

	/**
	 * {@link ChoiceConfigurationBuilder} instances.
	 */
	private final List<ChoiceConfigurationBuilder> choices = new ArrayList<>();

	/**
	 * Instantiate.
	 * 
	 * @param label     Label.
	 * @param envBridge {@link EnvironmentBridge}.
	 */
	public ChoiceBuilderImpl(String label, EnvironmentBridge envBridge) {
		super(label);
		this.envBridge = envBridge;
	}

	/*
	 * =============== ChoiceBuilder ===============
	 */

	@Override
	public ConfigurationBuilder<M> choice(String label) {
		ChoiceConfigurationBuilder choice = new ChoiceConfigurationBuilder(label);
		this.choices.add(choice);
		return choice;
	}

	/*
	 * ============== AbstractBuilder ================
	 */

	@Override
	public ChoiceValueInput<M> createInput(ValueInputContext<M, Integer> context) {

		// Display choices
		FlowPane choicesVisual = new FlowPane();

		// Obtain the initial value
		Integer initialValue = context.getInputValue().getValue();

		// Load the choices
		ToggleGroup selections = new ToggleGroup();
		for (int i = 0; i < this.choices.size(); i++) {
			ChoiceConfigurationBuilder choice = this.choices.get(i);

			// Create selection
			RadioButton selection = new RadioButton(choice.label);
			selection.setToggleGroup(selections);
			choicesVisual.getChildren().add(selection);

			// Determine if initial selection
			if ((initialValue != null) && (initialValue.equals(i))) {
				selection.setSelected(true);
			}

			// Handle selecting the choice
			int choiceIndex = i;
			boolean[] isActive = new boolean[] { false };
			selection.selectedProperty().addListener((observable, oldValue, newValue) -> {

				// Only update listing if change to active
				if (selection.isSelected() && !isActive[0]) {
					context.getInputValue().setValue(choiceIndex);
				}

				// Capture whether active
				isActive[0] = selection.isSelected();
			});
		}

		// Return the pane to contain choices
		return new ChoiceValueInputImpl(choicesVisual, context);
	}

	/**
	 * {@link ChoiceValueInput} implementation.
	 */
	private class ChoiceValueInputImpl implements ChoiceValueInput<M> {

		/**
		 * {@link FlowPane}.
		 */
		private final FlowPane choicesVisual;

		/**
		 * {@link ValueInputContext}.
		 */
		private final ValueInputContext<M, Integer> context;

		/**
		 * {@link Supplier} of the {@link ValueRendererFactory} instances for a
		 * particular choice.
		 */
		private final Supplier<ValueRendererFactory<M, ? extends ValueInput>[]>[] choiceFactories;

		/**
		 * Instantiate.
		 * 
		 * @param choicesVisual {@link FlowPane}.
		 * @param context       {@link ValueInputContext}.
		 */
		@SuppressWarnings("unchecked")
		private ChoiceValueInputImpl(FlowPane choicesVisual, ValueInputContext<M, Integer> context) {
			this.choicesVisual = choicesVisual;
			this.context = context;

			// Load the choices
			this.choiceFactories = new Supplier[ChoiceBuilderImpl.this.choices.size()];
			for (int i = 0; i < ChoiceBuilderImpl.this.choices.size(); i++) {
				ChoiceConfigurationBuilder choice = ChoiceBuilderImpl.this.choices.get(i);
				this.choiceFactories[i] = () -> choice.getValueRendererFactories();
			}
		}

		/*
		 * ============== ChoiceValueInput =================
		 */

		@Override
		public Node getNode() {
			return this.choicesVisual;
		}

		@Override
		public Supplier<ValueRendererFactory<M, ? extends ValueInput>[]>[] getChoiceValueRendererFactories() {
			return this.choiceFactories;
		}

		@Override
		public ReadOnlyProperty<Integer> getChoiceIndex() {
			return this.context.getInputValue();
		}
	}

	/**
	 * {@link ConfigurationBuilder} for particular choice.
	 */
	private class ChoiceConfigurationBuilder extends AbstractConfigurationBuilder<M> {

		/**
		 * Label for the choice.
		 */
		private String label;

		/**
		 * Instantiate.
		 * 
		 * @param label Label.
		 */
		public ChoiceConfigurationBuilder(String label) {
			super(ChoiceBuilderImpl.this.envBridge);
			this.label = label;
		}
	}

}
