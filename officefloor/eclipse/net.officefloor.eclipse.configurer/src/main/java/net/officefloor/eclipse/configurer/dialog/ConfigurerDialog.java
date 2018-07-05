/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.configurer.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import net.officefloor.eclipse.configurer.CloseListener;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;
import net.officefloor.model.Model;

/**
 * Dialog for a {@link Configurer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurerDialog<M> extends Configurer<M> implements ConfigurationBuilder<M> {

	/**
	 * {@link Shell}.
	 */
	private final Shell parentShell;

	/**
	 * Instantiate.
	 * 
	 * @param osgiBridge
	 *            {@link OfficeFloorOsgiBridge}.
	 * @param parentShell
	 *            {@link Shell}.
	 */
	public ConfigurerDialog(OfficeFloorOsgiBridge osgiBridge, Shell parentShell) {
		super(osgiBridge, parentShell);
		this.parentShell = parentShell;
	}

	/**
	 * Opens the dialog to configure the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 */
	public void open(M model) {

		// Create dialog shell
		Shell dialog = new Shell(this.parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		dialog.setLayout(new RowLayout());

		// Handle closing dialog
		this.close(new CloseListener() {

			@Override
			public void cancelled() {
				dialog.close();
			}

			@Override
			public void applied() {
				dialog.close();
			}
		});

		// Load configuration
		this.loadConfiguration(model, dialog);

		// Display dialog
		dialog.pack();
		dialog.open();
		Display display = this.parentShell.getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}