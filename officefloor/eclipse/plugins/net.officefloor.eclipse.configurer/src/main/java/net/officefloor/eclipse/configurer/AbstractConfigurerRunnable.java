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

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides means to test editor configurations without loading the Eclipse
 * platform.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConfigurerRunnable implements Runnable {

	/**
	 * Loads the configuration.
	 * 
	 * @return Configuration.
	 */
	protected abstract void loadConfiguration(Shell shell);

	/*
	 * ==================== Runnable ==========================
	 */

	@Override
	public void run() {

		// Turn off GTK3 until Java 9 available
		if (!"0".equals(System.getenv("SWT_GTK3"))) {
			System.err.println(
					"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.err.println();
			System.err.println(
					" WARNING: running JavaFx with possible binding to GTK3.  Set environment SWT_GTK3=0 to avoid problem (particularly if segmentation fault running)");
			System.err.println();
			System.err.println(
					"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}

		// Create the SWT display with shell
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		// Load configuration to shell
		this.loadConfiguration(shell);

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