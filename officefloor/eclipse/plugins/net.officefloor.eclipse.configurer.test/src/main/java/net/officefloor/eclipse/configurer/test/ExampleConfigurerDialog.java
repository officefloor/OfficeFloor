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

import javafx.application.Application;
import javafx.stage.Stage;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.dialog.ConfigurerDialog;

/**
 *
 * @author Daniel Sagenschneider
 */
public class ExampleConfigurerDialog extends Application {

	/**
	 * Instantiate.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             If fails to run.
	 */
	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		ConfigurerDialog<ExampleModel> dialog = new ConfigurerDialog<>("Example", "Test configuration");

		ConfigurationBuilder<ExampleModel> builder = dialog;
		builder.text("Text").init((m) -> m.text).setValue((m, value) -> m.text = value).validate((ctx) -> {
			switch (ctx.getValue().getValue().length()) {
			case 0:
				throw new Exception("Test failure");
			case 1:
				ctx.setError("Text too short");
			}
		});

		ExampleModel model = new ExampleModel();
		dialog.configureModel(model);
	}

}