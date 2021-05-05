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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.configurer.ChoiceBuilder;
import net.officefloor.gef.configurer.ClassBuilder;
import net.officefloor.gef.configurer.FlagBuilder;
import net.officefloor.gef.configurer.ListBuilder;
import net.officefloor.gef.configurer.MappingBuilder;
import net.officefloor.gef.configurer.MultipleBuilder;
import net.officefloor.gef.configurer.OptionalBuilder;
import net.officefloor.gef.configurer.PropertiesBuilder;
import net.officefloor.gef.configurer.ResourceBuilder;
import net.officefloor.gef.configurer.SelectBuilder;
import net.officefloor.gef.configurer.TextBuilder;
import net.officefloor.gef.configurer.internal.AbstractBuilder;
import net.officefloor.gef.configurer.internal.AbstractConfigurationBuilder;
import net.officefloor.gef.configurer.internal.ValueInput;
import net.officefloor.gef.configurer.internal.ValueInputContext;

/**
 * {@link MultipleBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class MultipleBuilderImpl<M, V> extends AbstractBuilder<M, List<V>, ValueInput, MultipleBuilder<M, V>>
		implements MultipleBuilder<M, V> {

	/**
	 * Delegate {@link AbstractConfigurationBuilder}.
	 */
	private final AbstractConfigurationBuilder<V> delegate;

	/**
	 * {@link Function} to obtain the label for the item. Default implementation
	 * provided, as require label for tab.
	 */
	private Function<V, String> getItemLabel = (item) -> item == null ? "" : item.toString();

	/**
	 * Instantiate.
	 * 
	 * @param label     Label.
	 * @param envBridge {@link EnvironmentBridge}.
	 */
	public MultipleBuilderImpl(String label, EnvironmentBridge envBridge) {
		super(label);

		// Create the delegate configuration builder
		this.delegate = new AbstractConfigurationBuilder<V>(envBridge) {
		};
	}

	/*
	 * =============== MultipleBuilder =======================
	 */

	@Override
	public void itemLabel(Function<V, String> getItemLabel) {
		this.getItemLabel = getItemLabel;
	}

	@Override
	protected ValueInput createInput(ValueInputContext<M, List<V>> context) {

		// Create the tab pane
		TabPane tabs = new TabPane();

		// Add the tabs for each value
		final Map<V, Tab> itemToTab = new HashMap<>();
		Runnable loadTabs = () -> {

			// Load the tabs for the models
			List<V> models = context.getInputValue().getValue();
			List<Tab> itemTabs = new ArrayList<>(models.size());
			for (V item : models) {

				// Obtain the tab for the model
				Tab tab = itemToTab.get(item);
				if (tab == null) {

					// Register new tab
					String itemLabel = this.getItemLabel.apply(item);
					tab = new Tab(itemLabel);
					tab.setClosable(false);

					// Configure the tab to load
					GridPane grid = new GridPane();
					tab.setContent(grid);

					// Load the configuration
					this.delegate.recursiveLoadConfiguration(item, null, grid, context.getOptionalActioner(),
							context.dirtyProperty(), context.validProperty(), context.getErrorListener());
				}

				// Include the tab
				itemTabs.add(tab);
			}

			// Load the tabs
			tabs.getTabs().setAll(itemTabs);
		};

		// Return the input
		return new ValueInput() {

			@Override
			public Node getNode() {
				return tabs;
			}

			@Override
			public void activate() {
				// Load the tabs
				loadTabs.run();

				// Update the tabs on change
				context.getInputValue().addListener((observable, oldValue, newValue) -> loadTabs.run());
			}
		};
	}

	/*
	 * =============== InputBuilder =======================
	 */

	@Override
	public ChoiceBuilder<V> choices(String label) {
		return this.delegate.choices(label);
	}

	@Override
	public <I> ListBuilder<V, I> list(String label, Class<I> itemType) {
		return this.delegate.list(label, itemType);
	}

	@Override
	public <I> SelectBuilder<V, I> select(String label, Function<V, ObservableList<I>> getItems) {
		return this.delegate.select(label, getItems);
	}

	@Override
	public OptionalBuilder<V> optional(Predicate<V> isShow) {
		return this.delegate.optional(isShow);
	}

	@Override
	public <I> MultipleBuilder<V, I> multiple(String label, Class<I> itemType) {
		return this.delegate.multiple(label, itemType);
	}

	@Override
	public PropertiesBuilder<V> properties(String label) {
		return this.delegate.properties(label);
	}

	@Override
	public MappingBuilder<V> map(String label, Function<V, ObservableList<String>> getSources,
			Function<V, ObservableList<String>> getTargets) {
		return this.delegate.map(label, getSources, getTargets);
	}

	@Override
	public ClassBuilder<V> clazz(String label) {
		return this.delegate.clazz(label);
	}

	@Override
	public ResourceBuilder<V> resource(String label) {
		return this.delegate.resource(label);
	}

	@Override
	public TextBuilder<V> text(String label) {
		return this.delegate.text(label);
	}

	@Override
	public FlagBuilder<V> flag(String label) {
		return this.delegate.flag(label);
	}

}
