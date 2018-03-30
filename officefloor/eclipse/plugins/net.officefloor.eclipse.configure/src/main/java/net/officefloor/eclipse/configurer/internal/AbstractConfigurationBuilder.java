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
package net.officefloor.eclipse.configurer.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ClassBuilder;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.ErrorListener;
import net.officefloor.eclipse.configurer.FlagBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.PropertiesBuilder;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.internal.inputs.ChoiceBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.FlagBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.ListBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.TextBuilderImpl;

/**
 * Abstract {@link ConfigurationBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractConfigurationBuilder<M> implements ConfigurationBuilder<M>, ValueRendererContext<M> {

	/**
	 * Listing of the {@link ValueRenderer} instances.
	 */
	private final List<ValueRenderer<M>> renderers = new ArrayList<>();

	/**
	 * {@link ErrorListener}.
	 */
	private ErrorListener errorListener = null;

	/**
	 * Model.
	 */
	private M model;

	/**
	 * Instantiate.
	 */
	public AbstractConfigurationBuilder() {
	}

	/**
	 * Overrides the default {@link ErrorListener}.
	 * 
	 * @param errorListener
	 *            {@link ErrorListener}.
	 */
	public void setErrorListener(ErrorListener errorListener) {
		this.errorListener = errorListener;
	}

	/**
	 * Obtain the list of {@link ValueRenderer} instances.
	 * 
	 * @return {@link ValueRenderer} instances.
	 */
	@SuppressWarnings("unchecked")
	public ValueRenderer<M>[] getValueRenderers() {
		return this.renderers.toArray(new ValueRenderer[this.renderers.size()]);
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

		// Provide error listener
		if (this.errorListener == null) {
			// Provide default error listener
			this.errorListener = new ErrorListener() {
				@Override
				public void valid() {
					// TODO Auto-generated method stub
					System.err.println("TODO IMPLEMENT errorListener.valid()");
				}

				@Override
				public void error(Throwable error) {
					// TODO Auto-generated method stub
					System.err.println("TODO IMPLEMENT errorListener.error(...)");
					error.printStackTrace();
				}

				@Override
				public void error(String message) {
					// TODO Auto-generated method stub
					System.err.println("TODO IMPLEMENT errorListener.valid(" + message + ")");
				}
			};
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

		// Provide narrow screen configuration
		VBox narrow = new VBox();
		narrow.getStyleClass().add("configurer-container-narrow");

		// Responsive view
		final double RESPONSIVE_WIDTH = 600;
		InvalidationListener listener = (event) -> {
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
		};
		scroll.widthProperty().addListener(listener);
		listener.invalidated(null); // set initial view

		// Load the value render list
		new ValueLister(wide, 1, narrow, this.getValueRenderers());
	}

	/**
	 * Convenience method to register the {@link ValueRenderer}.
	 * 
	 * @param builder
	 *            Builder.
	 * @return Input builder.
	 */
	private <B extends ValueRenderer<M>> B registerBuilder(B builder) {
		this.renderers.add(builder);
		return builder;
	}

	/*
	 * ================== ValueRendererContext ====================
	 */

	@Override
	public M getModel() {
		return this.model;
	}

	@Override
	public void refreshError() {
		// TODO Auto-generated method stub

	}

	/*
	 * ================== ConfigurationBuilder ====================
	 */

	@Override
	public TextBuilder<M> text(String label) {
		return this.registerBuilder(new TextBuilderImpl<>(label));
	}

	@Override
	public FlagBuilder<M> flag(String label) {
		return this.registerBuilder(new FlagBuilderImpl<>(label));
	}

	@Override
	public ChoiceBuilder<M> choices(String label) {
		return this.registerBuilder(new ChoiceBuilderImpl<>(label));
	}

	@Override
	public <I> ListBuilder<M, I> list(String label, Class<I> itemType) {
		return this.registerBuilder(new ListBuilderImpl<>(label));
	}

	@Override
	public PropertiesBuilder<M> properties(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void map(String label, Function<M, List<String>> getMappedItems) {
		// TODO Auto-generated method stub

	}

	@Override
	public ClassBuilder<M> clazz(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservableStringValue resource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void apply(Consumer<M> applier) {
		// TODO Auto-generated method stub

	}

	/**
	 * Lists some of the {@link ValueRenderer} instances.
	 */
	private class ValueLister {

		/**
		 * {@link GridPane} for wide view.
		 */
		private final GridPane wide;

		/**
		 * {@link VBox} for narrow view.
		 */
		private final VBox narrow;

		/**
		 * Registered {@link Node} instances to wide {@link GridPane}.
		 */
		private final List<Node> registeredWideNodes = new LinkedList<>();

		/**
		 * Registered {@link Node} instances to narrow {@link VBox}.
		 */
		private final List<Node> registeredNarrowNodes = new LinkedList<>();

		/**
		 * Next {@link ValueLister}.
		 */
		private ValueLister nextLister = null;

		/**
		 * Instantiate.
		 * 
		 * @param wide
		 *            Wide view {@link GridPane}.
		 * @param rowIndex
		 *            Row index within wide view {@link GridPane} to continue rendering
		 *            inputs.
		 * @param narrow
		 *            Narrow view {@link GridPane}.
		 * @param renderers
		 *            {@link ValueRenderer} instances.
		 */
		@SuppressWarnings("unchecked")
		public ValueLister(GridPane wide, int rowIndex, VBox narrow, ValueRenderer<M>[] renderers) {
			this.wide = wide;
			this.narrow = narrow;

			// Render in the items
			for (int i = 0; i < renderers.length; i++) {
				ValueRenderer<M> renderer = renderers[i];

				// Initialise the renderer
				renderer.init(AbstractConfigurationBuilder.this);

				// ======== wide ==============
				int column = 1; // initial spacing

				// Render the label
				Node wideLabel = renderer.createLabel();
				if (wideLabel != null) {
					wide.add(wideLabel, column++, rowIndex);
					GridPane.setHgrow(wideLabel, Priority.SOMETIMES);
					this.registeredWideNodes.add(wideLabel);
				}

				// Provide error feedback
				Node wideError = renderer.createErrorFeedback();
				if (wideError != null) {
					wide.add(wideError, column++, rowIndex);
					this.registeredWideNodes.add(wideError);
				}

				// Render the input
				Node wideInput = renderer.createInput();
				if (wideInput != null) {
					wide.add(wideInput, column++, rowIndex);
					GridPane.setHgrow(wideInput, Priority.ALWAYS);
					this.registeredWideNodes.add(wideInput);
				}

				// Provide some spacing at the end
				Node wideSpacing = new Pane();
				wide.add(wideSpacing, column++, rowIndex, 1, 1);
				this.registeredWideNodes.add(wideSpacing);

				// Increment row index for next value
				rowIndex++;

				// ======= narrow ==============

				// Contain particular item
				VBox narrowItem = new VBox();
				narrowItem.getStyleClass().add("configurer-container-narrow-item");
				narrow.getChildren().add(narrowItem);
				this.registeredNarrowNodes.add(narrowItem);

				// Render the label with error
				HBox labelAndError = new HBox();
				narrowItem.getChildren().add(labelAndError);
				Node narrowLabel = renderer.createLabel();
				if (narrowLabel != null) {
					labelAndError.getChildren().add(narrowLabel);
				}
				Node narrowError = renderer.createErrorFeedback();
				if (narrowError != null) {
					labelAndError.getChildren().add(narrowError);
				}

				// Render the input
				Node inputVisual = renderer.createInput();
				if (inputVisual != null) {
					narrowItem.getChildren().add(inputVisual);
				}

				// Determine if choice value renderer
				if (renderer instanceof ChoiceValueRenderer) {
					ChoiceValueRenderer<M> choiceRenderer = (ChoiceValueRenderer<M>) renderer;

					// Load choice
					int splitRowIndex = rowIndex;
					ValueRenderer<M>[] splitRenderers = Arrays.copyOfRange(renderers, i + 1, renderers.length);
					Runnable loadChoice = () -> {

						// Obtain the choice
						Integer choice = choiceRenderer.getChoiceIndex().getValue();
						if (choice == null) {
							// No choice selected, so carry on with renderers
							this.nextLister = new ValueLister(wide, splitRowIndex, narrowItem, splitRenderers);

						} else {
							// Have choice, so create concat list of remaining
							ValueRenderer<M>[] choiceRenderers = choiceRenderer.getChoiceValueRenders()[choice].get();
							ValueRenderer<M>[] remainingRenderers = new ValueRenderer[choiceRenderers.length
									+ splitRenderers.length];
							for (int c = 0; c < choiceRenderers.length; c++) {
								remainingRenderers[c] = choiceRenderers[c];
							}
							for (int s = 0; s < splitRenderers.length; s++) {
								remainingRenderers[choiceRenderers.length + s] = splitRenderers[s];
							}
							this.nextLister = new ValueLister(wide, splitRowIndex, narrowItem, remainingRenderers);
						}
					};

					// Listen for changes in choice
					choiceRenderer.getChoiceIndex().addListener((event) -> {

						// Clear next listing (as change in choice)
						if (this.nextLister != null) {
							this.nextLister.removeControls();
							this.nextLister = null;
						}

						// Load new choice
						loadChoice.run();
					});

					// Load current choice
					loadChoice.run();

					// Stop loading renderers (as next listing will render)
					return;
				}
			}
		}

		/**
		 * Removes the controls from view.
		 */
		private void removeControls() {
			this.wide.getChildren().removeAll(this.registeredWideNodes);
			this.narrow.getChildren().removeAll(this.registeredNarrowNodes);
		}
	}

}