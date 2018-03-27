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

/**
 * Main for running example configurer.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleJavaFxMain extends AbstractConfigurerRunnable<ExampleModel> {

	/**
	 * Main to run the configurer.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		new ExampleJavaFxMain().run();
	}

	/*
	 * ================ AbstractConfigurerRunnable ================
	 */

	@Override
	protected void build(ConfigurationBuilder<ExampleModel> builder) {
		builder.text("Text", (model, text) -> model.text = text);
	}

	@Override
	protected ExampleModel createModel() {
		return new ExampleModel();
	}

}