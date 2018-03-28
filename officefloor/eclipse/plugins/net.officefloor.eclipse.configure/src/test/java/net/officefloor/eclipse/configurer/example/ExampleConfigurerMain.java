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
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.TextBuilder;

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

		// Configure text
		TextBuilder<ExampleModel> text = builder.text("Text", (model, value) -> model.text = value)
				.init((model) -> model.text).validate((context) -> {
					if (context.getValue().length() < 3) {
						context.setError("Value too short");
					}
				});
		text.getValue().addListener((event, oldValue, newValue) -> System.out.println("Text = " + newValue));

	}

	@Override
	protected ExampleModel createModel() {
		ExampleModel model = new ExampleModel();
		model.text = "test";
		return model;
	}

}