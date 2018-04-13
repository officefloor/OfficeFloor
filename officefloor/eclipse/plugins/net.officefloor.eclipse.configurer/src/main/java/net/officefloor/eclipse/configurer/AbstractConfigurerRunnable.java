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
package net.officefloor.eclipse.configurer;

import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides means to test editor configurations without loading the Eclipse
 * platform.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConfigurerRunnable<M> implements Runnable {

	/**
	 * Builds the configuration.
	 * 
	 * @param builder
	 *            {@link ConfigurationBuilder}.
	 */
	protected abstract void build(ConfigurationBuilder<M> builder);

	/**
	 * Creates the model.
	 * 
	 * @return Model.
	 */
	protected abstract M createModel();

	/*
	 * ==================== Runnable ==========================
	 */

	@Override
	public void run() {

		// Build the configuration
		Configurer<M> configurer = new Configurer<>();
		this.build(configurer);

		// Load the model
		M model = this.createModel();

		// Create the SWT display with shell
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new RowLayout());

		// Load configuration to shell
		configurer.loadConfiguration(model, shell, null);

		// Run the display
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

}