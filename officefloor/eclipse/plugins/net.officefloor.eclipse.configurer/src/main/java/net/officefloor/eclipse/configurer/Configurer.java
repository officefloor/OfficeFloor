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

import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.officefloor.eclipse.configurer.internal.AbstractConfigurationBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.Model;

/**
 * {@link OfficeFloor} configurer that uses JavaFx.
 * 
 * @author Daniel Sagenschneider
 */
public class Configurer<M> extends AbstractConfigurationBuilder<M> {

	/**
	 * Instantiate.
	 * 
	 * @param javaProject
	 *            {@link IJavaProject}.
	 * @param parentShell
	 *            Parent {@link Shell}.
	 */
	public Configurer(IJavaProject javaProject, Shell parentShell) {
		super(javaProject, parentShell);
	}

	/**
	 * Loads the configuration to {@link Composite}.
	 * 
	 * @param model
	 *            Model.
	 * @param parent
	 *            Parent {@link Composite}.
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
	 * Loads the {@link ObjectProperty}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @param nodeProperty
	 *            {@link ObjectProperty}.
	 * @return {@link Configuration}.
	 */
	public Configuration loadConfiguration(M model, Property<Node> nodeProperty) {

		// Create pane for configuration components
		Pane pane = new Pane();
		nodeProperty.setValue(pane);

		// Load the configuration
		return this.loadConfiguration(model, pane);
	}

	/**
	 * Initialise application.
	 */
	public static class InitApplication extends Application {

		@Override
		public void start(Stage primaryStage) throws Exception {
		}
	}

}