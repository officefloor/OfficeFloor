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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import net.officefloor.eclipse.configurer.AbstractConfigurerRunnable;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.dialog.ConfigurerDialog;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;

/**
 * Runs the example {@link ConfigurerDialog}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleConfigurerDialog extends AbstractConfigurerRunnable {

	/**
	 * Main to run the configurer.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		new ExampleConfigurerDialog().run();
	}

	/*
	 * ================ AbstractConfigurerRunnable ================
	 */

	@Override
	protected void loadConfiguration(Shell shell) {

		// Provide button to open dialog
		Button openDialog = new Button(shell, SWT.NONE);
		openDialog.setText("Open Dialog");
		openDialog.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {

				// Create the dialog
				ConfigurerDialog<ExampleModel> dialog = new ConfigurerDialog<>(
						OfficeFloorOsgiBridge.getClassLoaderInstance(), shell);
				ConfigurationBuilder<ExampleModel> builder = dialog;

				// Configure the dialog
				builder.title("Example");

				builder.text("Text").init((m) -> m.text).setValue((m, value) -> m.text = value).validate((ctx) -> {
					switch (ctx.getValue().getValue().length()) {
					case 0:
						throw new Exception("Test failure");
					case 1:
						ctx.setError("Text too short");
					}
				});

				builder.flag("Flag").init((m) -> m.flag).setValue((m, value) -> m.flag = value);

				builder.apply("Change", (model) -> {
					System.out.println("Applying model:");
					model.write(System.out);
				});

				// Show the dialog
				ExampleModel model = new ExampleModel();
				model.text = "test";
				dialog.open(model);
			}
		});
	}

}