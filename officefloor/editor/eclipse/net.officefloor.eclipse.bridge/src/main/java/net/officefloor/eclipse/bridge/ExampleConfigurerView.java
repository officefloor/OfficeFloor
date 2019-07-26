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

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.gef.fx.swt.canvas.FXCanvasEx;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.officefloor.gef.configurer.ConfigurationBuilder;
import net.officefloor.gef.configurer.Configurer;
import net.officefloor.gef.configurer.ValueLoader;
import net.officefloor.model.Model;

/**
 * Example configurer view.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleConfigurerView {

	/**
	 * Logs the {@link ExampleModel}.
	 * 
	 * @param loader {@link ValueLoader}.
	 * @return {@link ValueLoader} with logging.
	 */
	private <V> ValueLoader<ExampleModel, V> log(ValueLoader<ExampleModel, V> loader) {
		return (model, value) -> {
			loader.loadValue(model, value);
			model.write(System.out);
		};
	}

	@Inject
	public ExampleConfigurerView(Composite parent) throws Exception {

		// Obtain the java project
		final String PROJECT_NAME = "Test";
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(PROJECT_NAME);
		if (project == null) {
			throw new IllegalStateException("No project by name " + PROJECT_NAME + " in workspace");
		}
		IJavaProject javaProject = JavaCore.create(project);

		// Create the configurer
		Configurer<ExampleModel> configurer = new Configurer<>(new OfficeFloorOsgiBridge(javaProject));

		// Provide configuration
		ConfigurationBuilder<ExampleModel> builder = configurer;

		// Text with validation
		builder.text("Text").init((m) -> m.text).setValue((m, value) -> m.text = value).validate((ctx) -> {
			switch (ctx.getValue().getValue().length()) {
			case 0:
				throw new Exception("Test failure");
			case 1:
				ctx.setError("Text too short");
			}
		});

		// Different class inputs
		builder.clazz("Class").init((model) -> model.className);
		builder.clazz("Model Class").init((model) -> model.modelClassName)
				.setValue(this.log((model, value) -> model.modelClassName = value)).superType(Model.class);
		builder.clazz("Missing class").superType(ExampleConfigurerView.class);

		// Class path resource
		builder.resource("Resource").init((model) -> model.resourceName)
				.setValue((model, value) -> model.resourceName = value);

		// Provide ability to update
		builder.apply("Update", (model) -> {
			System.out.println("Applied model:");
			model.write(System.out);
		});

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

		// Load model to view
		configurer.loadConfiguration(new ExampleModel(), pane);
	}
}