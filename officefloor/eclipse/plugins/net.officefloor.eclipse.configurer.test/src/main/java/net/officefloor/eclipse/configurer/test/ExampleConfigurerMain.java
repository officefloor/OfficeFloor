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
package net.officefloor.eclipse.configurer.test;

import javafx.collections.FXCollections;
import net.officefloor.eclipse.configurer.AbstractConfigurerRunnable;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.MultipleBuilder;
import net.officefloor.eclipse.configurer.ValueLoader;
import net.officefloor.eclipse.configurer.test.ExampleModel.ExampleItem;

/**
 * Main for running example configurer.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleConfigurerMain extends AbstractConfigurerRunnable<ExampleModel> {

	/**
	 * Main to run the configurer.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		new ExampleConfigurerMain().run();
	}

	/**
	 * Logs the {@link ExampleModel}.
	 * 
	 * @param loader
	 *            {@link ValueLoader}.
	 * @return {@link ValueLoader} with logging.
	 */
	private <V> ValueLoader<ExampleModel, V> log(ValueLoader<ExampleModel, V> loader) {
		return (model, value) -> {
			loader.loadValue(model, value);
			model.write(System.out);
		};
	}

	/**
	 * Logs the {@link ExampleItem}.
	 * 
	 * @param loader
	 *            {@link ValueLoader}.
	 * @return {@link ValueLoader} with logging.
	 */
	private <V> ValueLoader<ExampleItem, V> logItem(ValueLoader<ExampleItem, V> loader) {
		return (model, value) -> {
			loader.loadValue(model, value);
			model.write(System.out);
		};
	}

	/*
	 * ================ AbstractConfigurerRunnable ================
	 */

	@Override
	protected void build(ConfigurationBuilder<ExampleModel> builder) {
		
		// Title
		builder.title("Configuration");

		// Configure text (non-editable)
		builder.text("Label").init((model) -> model.text);

		// Configure text (editable)
		builder.text("Text").init((model) -> model.text).validate((context) -> {
			switch (context.getValue().getValue().length()) {
			case 0:
				throw new Exception("Test failure");
			case 1:
				context.setError("Text too short");
			}
		}).setValue(this.log((model, value) -> model.text = value));

		// Provide choices
		ChoiceBuilder<ExampleModel> choices = builder.choices("Choices");
		choices.choice("one").text("Choice One").init((model) -> model.choiceValue)
				.setValue(this.log((model, value) -> model.choiceValue = "ONE: " + value));
		choices.choice("two").text("Choice Two").init((model) -> model.choiceValue)
				.setValue(this.log((model, value) -> model.choiceValue = "TWO: " + value));

		// Provide flag (non-editable)
		builder.flag("Flag").init((model) -> model.flag);

		// Provide flag (editable)
		builder.flag("Flag").init((model) -> model.flag).setValue(this.log((model, flag) -> model.flag = flag));

		// Provide list of values (without ability to add further rows)
		ListBuilder<ExampleModel, ExampleItem> list = builder.list("List", ExampleItem.class)
				.init((model) -> model.items).setValue(this.log((model, items) -> model.items = items));
		list.text("Label").init((model) -> model.text);
		list.text("Text").init((model) -> model.text).setValue(this.logItem((item, value) -> item.text = value));
		list.flag("Flag").init((model) -> model.flag);
		list.flag("Flag").init((model) -> model.flag).setValue(this.logItem((item, value) -> item.flag = value));

		// Provide list of values (able to add and delete rows)
		ListBuilder<ExampleModel, ExampleItem> editableList = builder.list("List", ExampleItem.class)
				.addItem(() -> new ExampleItem("New", true)).deleteItem()
				.setValue(this.log((model, items) -> model.items = items));
		editableList.text("Text").init((model) -> model.text)
				.setValue(this.logItem((item, value) -> item.text = value));
		editableList.flag("Flag").init((model) -> model.flag)
				.setValue(this.logItem((item, value) -> item.flag = value));

		// Provide properties
		builder.properties("Properties").init((model) -> model.properties)
				.setValue(this.log((model, value) -> model.properties = value));

		// Provide mapping
		builder.map("Mapping", (model) -> FXCollections.observableArrayList(model.sources),
				(model) -> FXCollections.observableArrayList(model.targets)).init((model) -> model.mapping)
				.setValue(this.log((model, value) -> model.mapping = value));

		// Provide multiple
		MultipleBuilder<ExampleModel, ExampleItem> multiple = builder.multiple("Multiple", ExampleItem.class)
				.init((model) -> model.multiple).setValue(this.log((model, items) -> model.multiple = items));
		multiple.itemLabel((item) -> item.text);
		multiple.text("Label").init((item) -> item.text).setValue(this.logItem((item, value) -> item.text = value))
				.validate((context) -> {
					switch (context.getValue().getValue().length()) {
					case 0:
						throw new Exception("Test failure");
					case 1:
						context.setError("Text too short");
					}
				});
		multiple.flag("Flag").init((item) -> item.flag).setValue(this.logItem((item, value) -> item.flag = value));

		// Validate the model
		builder.validate((context) -> {
			ExampleModel model = context.getValue().getValue();
			switch(model.properties.getPropertyNames().length) {
			case 0:
				throw new Exception("Model invalid");
			case 1:
				context.setError("Model requires more properties");
			}
		});
		
		// Allow applying configuration
		builder.apply("Execute", (model) -> {
			System.out.println("Applied model:");
			model.write(System.out);
		});
	}

	@Override
	protected ExampleModel createModel() {
		return new ExampleModel();
	}

}