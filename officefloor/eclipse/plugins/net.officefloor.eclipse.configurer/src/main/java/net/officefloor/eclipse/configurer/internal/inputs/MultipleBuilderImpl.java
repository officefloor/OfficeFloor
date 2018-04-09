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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Shell;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ClassBuilder;
import net.officefloor.eclipse.configurer.FlagBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.MappingBuilder;
import net.officefloor.eclipse.configurer.MultipleBuilder;
import net.officefloor.eclipse.configurer.PropertiesBuilder;
import net.officefloor.eclipse.configurer.ResourceBuilder;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractBuilder;
import net.officefloor.eclipse.configurer.internal.AbstractConfigurationBuilder;
import net.officefloor.eclipse.configurer.internal.ValueInput;
import net.officefloor.eclipse.configurer.internal.ValueInputContext;

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
	private final AbstractConfigurationBuilder<V> delegate = new AbstractConfigurationBuilder<>();

	/**
	 * {@link Function} to obtain the label for the item. Default implementation
	 * provided, as require label for tab.
	 */
	private Function<V, String> getItemLabel = (item) -> item == null ? "" : item.toString();

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 */
	public MultipleBuilderImpl(String label) {
		super(label);
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
					this.delegate.loadConfiguration(item, grid, this.delegate);
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
				context.getInputValue().addListener((event) -> loadTabs.run());
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
	public ClassBuilder<V> clazz(String label, IJavaProject javaProject, Shell shell) {
		return this.delegate.clazz(label, javaProject, shell);
	}

	@Override
	public ResourceBuilder<V> resource(String label, IJavaProject javaProject, Shell shell) {
		return this.delegate.resource(label, javaProject, shell);
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