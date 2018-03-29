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
import java.util.function.Supplier;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractConfigurationBuilder;
import net.officefloor.eclipse.configurer.internal.ChoiceValueRenderer;
import net.officefloor.eclipse.configurer.internal.ValueRenderer;

/**
 * {@link ChoiceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ChoiceBuilderImpl<M> extends AbstractBuilder<M, Integer, ChoiceBuilder<M>>
		implements ChoiceBuilder<M>, ValueRenderer<M>, ChoiceValueRenderer<M> {

	/**
	 * {@link ChoiceConfigurationBuilder} instances.
	 */
	private final List<ChoiceConfigurationBuilder> choices = new ArrayList<>();

	/**
	 * {@link Supplier} of the {@link ValueRenderer} instances for a particular
	 * choice.
	 */
	private Supplier<ValueRenderer<M>[]>[] choiceFactories;

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 */
	public ChoiceBuilderImpl(String label) {
		super(label);
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
	@SuppressWarnings("unchecked")
	public void init(M model) {

		// Load the choices
		this.choiceFactories = new Supplier[this.choices.size()];
		for (int i = 0; i < this.choices.size(); i++) {
			ChoiceConfigurationBuilder choice = this.choices.get(i);
			this.choiceFactories[i] = () -> choice.getValueRenderers();
		}
	}

	@Override
	public Node createInput(Property<Integer> value) {

		// Display choices
		FlowPane choicesVisual = new FlowPane();

		// Load the choices
		ToggleGroup selections = new ToggleGroup();
		for (int i = 0; i < this.choices.size(); i++) {
			ChoiceConfigurationBuilder choice = this.choices.get(i);

			// Create selection
			RadioButton selection = new RadioButton(choice.label);
			selection.setToggleGroup(selections);
			choicesVisual.getChildren().add(selection);

			// Handle selecting the choice
			int choiceIndex = i;
			boolean[] isActive = new boolean[] { false };
			selection.selectedProperty().addListener((event) -> {

				// Only update listing if change to active
				if (selection.isSelected() && !isActive[0]) {
					value.setValue(choiceIndex);
				}

				// Capture whether active
				isActive[0] = selection.isSelected();
			});
		}

		// Return the pane to contain choices
		return choicesVisual;
	}

	/*
	 * ================ ChoiceValueRenderer ===================
	 */

	@Override
	public Supplier<ValueRenderer<M>[]>[] getChoiceValueRenders() {
		return this.choiceFactories;
	}

	@Override
	public ReadOnlyProperty<Integer> getChoiceIndex() {
		return this.getValue();
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
		 * @param label
		 *            Label.
		 */
		public ChoiceConfigurationBuilder(String label) {
			this.label = label;
		}
	}

}