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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.configurer.internal.ValueRenderer;
import net.officefloor.eclipse.configurer.internal.ValueRendererContext;
import net.officefloor.eclipse.configurer.internal.text.TextBuilderImpl;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} configurer that uses JavaFx.
 * 
 * @author Daniel Sagenschneider
 */
public class Configurer<M> implements ConfigurationBuilder<M> {

	/**
	 * Path to the error {@link Image}.
	 */
	private static final String ERROR_IMAGE_PATH = "/" + Configurer.class.getPackage().getName().replace('.', '/')
			+ "/error.png";

	/**
	 * Listing of the {@link ValueRenderer} instances.
	 */
	private final List<ValueInput> inputs = new ArrayList<>();

	/**
	 * Model.
	 */
	private M model;

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

	/**
	 * Loads the configuration to the parent {@link Pane}.
	 * 
	 * @param model
	 *            Model.
	 * @param parent
	 *            Parent {@link Pane}.
	 */
	public void loadConfiguration(M model, Pane parent) {
		this.model = model;

		// Load the renderers
		for (ValueInput input : this.inputs) {
			input.renderer.init(input);
		}

		// Load the default styling
		parent.getScene().getStylesheets().add(this.getClass().getName().replace('.', '/') + ".css");

		// Scroll both narrow and wide views
		ScrollPane scroll = new ScrollPane();
		scroll.prefWidthProperty().bind(parent.widthProperty());
		scroll.prefHeightProperty().bind(parent.heightProperty());
		scroll.setFitToWidth(true);
		scroll.getStyleClass().add("configurer-container");
		parent.getChildren().add(scroll);

		// Provide wide screen configuration
		GridPane wide = new GridPane();
		wide.getStyleClass().add("configurer-container-wide");
		for (int row = 0; row < this.inputs.size(); row++) {
			ValueInput input = this.inputs.get(row);

			// Render the label
			int column = 1;
			Node label = input.renderer.createLabel();
			wide.add(label, column, row);
			GridPane.setHgrow(label, Priority.SOMETIMES);

			// Provide error feedback
			column++;
			wide.add(input.createError(), column, row);

			// Render the input
			column++;
			Node inputVisual = input.renderer.createInput();
			wide.add(inputVisual, column, row);
			GridPane.setHgrow(inputVisual, Priority.ALWAYS);

			// Provide some spacing at the end
			column++;
			wide.add(new Pane(), column, row, 1, 1);
		}

		// Provide narrow screen configuration
		VBox narrow = new VBox();
		narrow.getStyleClass().add("configurer-container-narrow");
		for (ValueInput input : this.inputs) {

			// Contain particular item
			VBox narrowItem = new VBox();
			narrowItem.getStyleClass().add("configurer-container-narrow-item");
			narrow.getChildren().add(narrowItem);

			// Render the label with error
			HBox labelAndError = new HBox();
			narrowItem.getChildren().add(labelAndError);
			Node label = input.renderer.createLabel();
			labelAndError.getChildren().add(label);
			labelAndError.getChildren().add(input.createError());

			// Render the input
			Node inputVisual = input.renderer.createInput();
			narrowItem.getChildren().add(inputVisual);
		}

		// Load the content
		scroll.setContent(wide);

		// Responsive view
		final double RESPONSIVE_WIDTH = 400;
		scroll.widthProperty().addListener((event) -> {
			if (scroll.getWidth() < RESPONSIVE_WIDTH) {
				// Avoid events if already narrow
				if (scroll.getContent() != narrow) {
					scroll.setContent(narrow);
				}
			} else {
				// Again avoid events if already wide
				if (scroll.getContent() != wide) {
					scroll.setContent(wide);
				}
			}
		});
	}

	/*
	 * ================== ConfigurationBuilder ====================
	 */

	@Override
	public TextBuilder<M> text(String label, ValueLoader<M, String> textLoader) {
		TextBuilderImpl<M> builder = new TextBuilderImpl<>(label, textLoader);
		this.inputs.add(new ValueInput(builder));
		return builder;
	}

	@Override
	public ClassBuilder<M> clazz(String label, ValueLoader<M, String> classLoader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservableStringValue resource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservableBooleanValue flag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validate(ValueValidator<M> validator) {
		// TODO Auto-generated method stub

	}

	@Override
	public <E extends Enum<E>> ChoiceBuilder<M, E> choices(String label, Class<E> choiceEnumClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <I> ItemBuilder<I> list(String label, Class<I> itemClass, ValueLoader<M, List<I>> itemsLoader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertiesBuilder<M> properties(String label, ValueLoader<M, PropertyList> propertiesLoader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void map(String label, Function<M, List<String>> getMappedItems,
			ValueLoader<M, Map<String, String>> mapping) {
		// TODO Auto-generated method stub

	}

	@Override
	public void apply(Consumer<M> applier) {
		// TODO Auto-generated method stub

	}

	/**
	 * Input of a value.
	 */
	private class ValueInput implements ValueRendererContext<M> {

		/**
		 * {@link ValueRenderer}.
		 */
		private final ValueRenderer<M> renderer;

		/**
		 * Error.
		 */
		private final ReadOnlyStringWrapper error = new ReadOnlyStringWrapper();

		/**
		 * Instantiate.
		 * 
		 * @param renderer
		 *            {@link ValueRenderer}.
		 */
		private ValueInput(ValueRenderer<M> renderer) {
			this.renderer = renderer;
		}

		/**
		 * Instantiate.
		 * 
		 * @return {@link Node} for the error.
		 */
		private Node createError() {
			ImageView error = new ImageView(new Image(ERROR_IMAGE_PATH, 15, 15, true, true));
			Tooltip errorTooltip = new Tooltip();
			errorTooltip.getStyleClass().add("error-tooltip");
			Tooltip.install(error, errorTooltip);
			error.visibleProperty().set(false); // default hide
			this.error.addListener((event) -> {
				String errorText = this.error.get();
				if ((errorText != null) && (errorText.length() > 0)) {
					// Display the error
					errorTooltip.setText(errorText);
					error.visibleProperty().set(true);
				} else {
					// No error
					error.visibleProperty().set(false);
				}
			});
			return error;
		}

		/*
		 * ============= ValueRendererContext ===============
		 */

		@Override
		public M getModel() {
			return Configurer.this.model;
		}

		@Override
		public void setError(String message) {
			this.error.set(message);
		}

		@Override
		public void setError(Throwable error) {
			this.error.set(error.getMessage());
		}
	}

}