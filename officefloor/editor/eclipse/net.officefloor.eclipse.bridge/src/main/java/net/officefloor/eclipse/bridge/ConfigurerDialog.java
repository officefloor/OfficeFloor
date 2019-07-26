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
package net.officefloor.eclipse.bridge;

import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.officefloor.gef.configurer.CloseListener;
import net.officefloor.gef.configurer.Configuration;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.Configurer;
import net.officefloor.gef.bridge.EnvironmentBridge;
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
	 * @param envBridge   {@link EnvironmentBridge}.
	 * @param parentShell {@link Shell}.
	 */
	public ConfigurerDialog(EnvironmentBridge envBridge, Shell parentShell) {
		super(envBridge);
		this.parentShell = parentShell;
	}

	/**
	 * Loads the configuration to {@link Composite}.
	 * 
	 * @param model  Model.
	 * @param parent Parent {@link Composite}.
	 * @return {@link Configuration}.
	 */
	public Configuration loadConfiguration(M model, Composite parent) {

		// Create the FX Canvas
		FXCanvas fxCanvas = new FXCanvasEx(parent, SWT.NONE) {
			public Point computeSize(int wHint, int hHint, boolean changed) {
				// Always the parent size
				Rectangle bounds = parent.getClientArea();
				return new Point(bounds.width - 5, bounds.height - 5);
			}
		};

		// Create pane for configuration components
		Pane pane = new Pane();

		// Load scene into canvas (matching background colour)
		org.eclipse.swt.graphics.Color background = parent.getBackground();
		Scene scene = new Scene(pane, Color.rgb(background.getRed(), background.getGreen(), background.getBlue()));

		// Load the scene to the canvas
		fxCanvas.setScene(scene);

		// Load the configuration
		return this.loadConfiguration(model, pane);
	}

	/**
	 * Opens the dialog to configure the {@link Model}.
	 * 
	 * @param model {@link Model}.
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