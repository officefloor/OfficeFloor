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
package net.officefloor.eclipse.configurer.example;

import net.officefloor.eclipse.configurer.AbstractConfigurerRunnable;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.example.ExampleModel.ExampleItem;

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

	/*
	 * ================ AbstractConfigurerRunnable ================
	 */

	@Override
	protected void build(ConfigurationBuilder<ExampleModel> builder) {

		// Configure text (non-editable)
		builder.text("Label").init((model) -> model.text);

		// Configure text (editable)
		builder.text("Text").init((model) -> model.text).validate((context) -> {
			if (context.getValue().getValue().length() < 3) {
				context.setError("Value too short");
			}
		}).setValue((model, value) -> model.text = value);

		// Provide choices
		ChoiceBuilder<ExampleModel> choices = builder.choices("Choices");
		choices.choice("one").text("Choice One").setValue((model, value) -> model.choiceValue = "ONE: " + value);
		choices.choice("two").text("Choice Two").setValue((model, value) -> model.choiceValue = "TWO: " + value);

		// Provide flag (non-editable)
		builder.flag("Flag").init((model) -> model.flag);

		// Provide flag (editable)
		builder.flag("Flag").init((model) -> model.flag).setValue((model, flag) -> model.flag = flag);

		// Provide list of values (without ability to add further rows)
		ListBuilder<ExampleModel, ExampleItem> list = builder.list("List", ExampleItem.class)
				.init((model) -> model.items);
		list.text("Label").init((model) -> model.text);
		list.text("Text").init((model) -> model.text).setValue((item, value) -> item.text = value);
		list.flag("Flag").init((model) -> model.flag);
		list.flag("Flag").init((model) -> model.flag).setValue((item, value) -> item.flag = value);

		// Provide list of values (able to add and delete rows)
		ListBuilder<ExampleModel, ExampleItem> editableList = builder.list("List", ExampleItem.class)
				.addItem(() -> new ExampleItem("New", true)).deleteItem();
		editableList.text("Text").init((model) -> model.text);
		editableList.flag("Flag").init((model) -> model.flag);

		// Provide properties
		builder.properties("Properties").init((model) -> model.properties);
	}

	@Override
	protected ExampleModel createModel() {
		return new ExampleModel();
	}

}