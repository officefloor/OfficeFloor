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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.officefloor.eclipse.configurer.internal.AbstractConfigurationBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} configurer that uses JavaFx.
 * 
 * @author Daniel Sagenschneider
 */
public class Configurer<M> extends AbstractConfigurationBuilder<M> {

	/**
	 * Loads the configuration to {@link Composite}.
	 * 
	 * @param model
	 *            Model.
	 * @param parent
	 *            Parent {@link Composite}.
	 */
	public void loadConfiguration(M model, Composite parent) {

		// Create the FX Canvas
		FXCanvas fxCanvas = new FXCanvas(parent, SWT.NONE) {
			public Point computeSize(int wHint, int hHint, boolean changed) {
				// Always the parent size
				Rectangle bounds = parent.getClientArea();
				return new Point(bounds.width - 5, bounds.height - 5);
			}
		};

		// Create pane for configuration components
		Pane pane = new Pane();
		pane.getStyleClass().add("root");

		// Load scene into canvas (matching background colour)
		org.eclipse.swt.graphics.Color background = parent.getBackground();
		Scene scene = new Scene(pane, Color.rgb(background.getRed(), background.getGreen(), background.getBlue()));

		// Load the scene to the canvas
		fxCanvas.setScene(scene);

		// Load the configuration
		this.loadConfiguration(model, pane);
	}

}