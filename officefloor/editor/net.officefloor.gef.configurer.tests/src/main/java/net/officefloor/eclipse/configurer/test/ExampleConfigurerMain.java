/*-
 * #%L
 * net.officefloor.gef.configurer.tests
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

package net.officefloor.eclipse.configurer.test;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.configurer.test.ExampleModel.ExampleItem;
import net.officefloor.gef.bridge.ClassLoaderEnvironmentBridge;
import net.officefloor.gef.configurer.AbstractConfigurerApplication;
import net.officefloor.gef.configurer.ChoiceBuilder;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.Configurer;
import net.officefloor.gef.configurer.ListBuilder;
import net.officefloor.gef.configurer.MultipleBuilder;
import net.officefloor.gef.configurer.OptionalBuilder;
import net.officefloor.gef.configurer.SelectBuilder;
import net.officefloor.gef.configurer.TextBuilder;
import net.officefloor.gef.configurer.ValueLoader;

/**
 * Main for running example configurer.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleConfigurerMain extends AbstractConfigurerApplication {

	/**
	 * Main to run the configurer.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	/**
	 * Logs the {@link ExampleModel}.
	 * 
	 * @param loader {@link ValueLoader}.
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
	 * @param loader {@link ValueLoader}.
	 * @return {@link ValueLoader} with logging.
	 */
	private <V> ValueLoader<ExampleItem, V> logItem(ValueLoader<ExampleItem, V> loader) {
		return (model, value) -> {
			loader.loadValue(model, value);
			model.write(System.out);
		};
	}

	/*
	 * ================ AbstractConfigurerApplication ================
	 */

	@Override
	protected void loadConfiguration(Pane parent) {

		// Create the configurer
		Configurer<ExampleModel> configurer = new Configurer<>(new ClassLoaderEnvironmentBridge());
		ConfigurationBuilder<ExampleModel> builder = configurer;

		// Title
		builder.title("Configuration");

		// Configure text (non-editable)
		builder.text("Label").init((model) -> model.text);

		// Configure text (editable)
		TextBuilder<ExampleModel> textBuilder = builder.text("Text").init((model) -> model.text).validate((context) -> {
			switch (context.getValue().getValue().length()) {
			case 0:
				throw new Exception("Test failure");
			case 1:
				context.setError("Text too short");
			}
		}).setValue(this.log((model, value) -> model.text = value));

		// Configure multi-line text (non-editable)
		builder.text("Multiline Label").multiline(true).init((model) -> model.multilineText);

		// Configure multi-line text (editable)
		builder.text("Multiline Text").multiline(true).init((model) -> model.multilineText)
				.setValue(this.log((model, value) -> model.multilineText = value));

		// Optional based on text
		OptionalBuilder<ExampleModel> textOptionalBuilder = builder.optional((model) -> !"hide".equals(model.text));
		textOptionalBuilder.text("Optional Text").init((model) -> model.text)
				.setValue(this.log((model, value) -> model.text = value));

		// Reload of optional based on value
		textBuilder.validate((context) -> context.reload(textOptionalBuilder));

		// Configure select
		SelectBuilder<ExampleModel, ExampleItem> selectBuilder = builder
				.select("Select", (model) -> FXCollections.observableArrayList(model.selections))
				.itemLabel((item) -> item.text).init((model) -> model.selectedItem)
				.setValue(this.log((model, value) -> model.selectedItem = value));

		// Optional based on select
		OptionalBuilder<ExampleModel> selectOptionalBuilder = builder.optional((model) -> model.selectedItem.flag);
		selectOptionalBuilder.text("Optional Selection").init((model) -> model.selectedItem.text);

		// Reload optional based on selection
		selectBuilder.validate((context) -> context.reload(selectOptionalBuilder));

		// Configure class
		builder.clazz("Class").init((model) -> model.className)
				.setValue(this.log((model, value) -> model.className = value));

		// Configure resource
		builder.resource("Resource").init((model) -> model.resourceName)
				.setValue(this.log((model, value) -> model.resourceName = value));

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
			switch (model.properties.getPropertyNames().length) {
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

		// Load the configuration
		configurer.loadConfiguration(new ExampleModel(), parent);
	}

}
