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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.widgets.Shell;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.officefloor.eclipse.configurer.Actioner;
import net.officefloor.eclipse.configurer.Builder;
import net.officefloor.eclipse.configurer.ChoiceBuilder;
import net.officefloor.eclipse.configurer.ClassBuilder;
import net.officefloor.eclipse.configurer.CloseListener;
import net.officefloor.eclipse.configurer.Configuration;
import net.officefloor.eclipse.configurer.ConfigurationBuilder;
import net.officefloor.eclipse.configurer.DefaultImages;
import net.officefloor.eclipse.configurer.ErrorListener;
import net.officefloor.eclipse.configurer.FlagBuilder;
import net.officefloor.eclipse.configurer.ListBuilder;
import net.officefloor.eclipse.configurer.MappingBuilder;
import net.officefloor.eclipse.configurer.MultipleBuilder;
import net.officefloor.eclipse.configurer.PropertiesBuilder;
import net.officefloor.eclipse.configurer.ResourceBuilder;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.configurer.ValueValidator.ValueValidatorContext;
import net.officefloor.eclipse.configurer.internal.inputs.ChoiceBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.ClassBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.FlagBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.ListBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.MappingBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.MultipleBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.PropertiesBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.ResourceBuilderImpl;
import net.officefloor.eclipse.configurer.internal.inputs.TextBuilderImpl;
import net.officefloor.eclipse.osgi.OfficeFloorOsgiBridge;

/**
 * Abstract {@link ConfigurationBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConfigurationBuilder<M> implements ConfigurationBuilder<M> {

	/**
	 * CSS class applied to {@link GridPane} in wide view.
	 */
	public static final String CSS_CLASS_WIDE = "wide";

	/**
	 * CSS class applied to the {@link GridPane} in narrow view.
	 */
	public static final String CSS_CLASS_NARROW = "narrow";

	/**
	 * Listing of the {@link ValueRendererFactory} instances.
	 */
	private final List<ValueRendererFactory<M, ? extends ValueInput>> rendererFactories = new ArrayList<>();

	/**
	 * {@link OfficeFloorOsgiBridge}.
	 */
	private final OfficeFloorOsgiBridge osgiBridge;

	/**
	 * Parent {@link Shell} for dialogs.
	 */
	private final Shell parentShell;

	/**
	 * Title.
	 */
	private String title = null;

	/**
	 * {@link ValueValidator}.
	 */
	private ValueValidator<M, M> validator = null;

	/**
	 * {@link ErrorListener}.
	 */
	private ErrorListener errorListener = null;

	/**
	 * Label for the apply {@link Actioner}.
	 */
	private String applierLabel = null;

	/**
	 * {@link Applier} to apply the model.
	 */
	private Applier<M> applier = null;

	/**
	 * {@link CloseListener}.
	 */
	private CloseListener closeListener = null;

	/**
	 * Instantiate.
	 * 
	 * @param osgiBridge
	 *            {@link OfficeFloorOsgiBridge}.
	 * @param parentShell
	 *            Parent {@link Shell}. May be <code>null</code> if no dialog
	 *            configuration required by inputs.
	 */
	public AbstractConfigurationBuilder(OfficeFloorOsgiBridge osgiBridge, Shell parentShell) {
		this.osgiBridge = osgiBridge;
		this.parentShell = parentShell;
	}

	/**
	 * Obtains the parent {@link Shell}.
	 * 
	 * @return Parent {@link Shell}.
	 */
	public Shell getParentShell() {
		if (this.parentShell == null) {
			throw new IllegalStateException("Not initialised against a parent " + Shell.class.getSimpleName()
					+ " but requiring dialog based configuration");
		}
		return this.parentShell;
	}

	/**
	 * Obtain the list of {@link ValueRendererFactory} instances.
	 * 
	 * @return {@link ValueRendererFactory} instances.
	 */
	@SuppressWarnings("unchecked")
	public ValueRendererFactory<M, ? extends ValueInput>[] getValueRendererFactories() {
		return this.rendererFactories.toArray(new ValueRendererFactory[this.rendererFactories.size()]);
	}

	/**
	 * Loads the configuration to the parent {@link Pane}.
	 * 
	 * @param model
	 *            Model.
	 * @param configurationNode
	 *            Configuration {@link Pane}.
	 * @return {@link Configuration}.
	 */
	protected Configuration loadConfiguration(M model, Pane configurationNode) {

		// Load the default styling
		Scene scene = configurationNode.getScene();
		if (scene != null) {
			scene.getStylesheets().add(
					AbstractConfigurationBuilder.class.getPackage().getName().replace('.', '/') + "/Configurer.css");
		}

		// Create the dirty and valid properties
		Property<Boolean> dirtyProperty = new SimpleBooleanProperty(false);
		SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);

		// Create the actioner
		ActionerImpl<M> actioner = null;
		if (this.applier != null) {
			actioner = new ActionerImpl<>(model, this.applierLabel, this.applier, dirtyProperty, this.closeListener);
		}

		// Scroll both narrow and wide views
		ScrollPane scroll = new ScrollPane() {
			@Override
			public void requestFocus() {
				// avoid stealing focus
				// (work around for GEF drag/drop aborting on focus change)
			}
		};
		scroll.prefWidthProperty().bind(configurationNode.widthProperty());
		scroll.prefHeightProperty().bind(configurationNode.heightProperty());
		scroll.setFitToWidth(true);
		scroll.getStyleClass().add("configurer-container");

		// Provide grid to load configuration
		GridPane grid = new GridPane();
		scroll.setContent(grid);

		// Determine if provided an error listener
		ErrorListener errorListener = this.errorListener;
		DefaultErrorListener<M> defaultErrorListener = null;
		if (errorListener == null) {

			// Provide wrappers for action and error listening
			VBox wrapper = new VBox();
			wrapper.getStyleClass().setAll("configurer", "dialog-pane");
			wrapper.pseudoClassStateChanged(PseudoClass.getPseudoClass("header"), true);
			configurationNode.getChildren().add(wrapper);

			// Create the error listener
			defaultErrorListener = new DefaultErrorListener<>(this.title, actioner, wrapper, scroll, this.closeListener,
					dirtyProperty, validProperty);
			errorListener = defaultErrorListener;

			// Bind height of scroll (minus header)
			scroll.prefHeightProperty().bind(Bindings.subtract(configurationNode.heightProperty(),
					defaultErrorListener.header.heightProperty()));

			// Load header and configuration
			wrapper.getChildren().setAll(defaultErrorListener.header, scroll);

		} else {
			// Just configuration node (as error listening provided)
			scroll.getStyleClass().setAll("configurer");
			scroll.pseudoClassStateChanged(PseudoClass.getPseudoClass("no-header"), true);
			configurationNode.getChildren().add(scroll);
		}

		// Allow action to notify of apply error
		if (actioner != null) {
			actioner.errorListener = errorListener;
		}

		// Apply CSS (so Scene available to inputs)
		configurationNode.applyCss();

		// Load the configuration to grid
		Configuration configuration = this.recursiveLoadConfiguration(model, configurationNode, grid, actioner,
				dirtyProperty, validProperty, errorListener);

		// Setup to display heading (with apply button disabled)
		if (defaultErrorListener != null) {
			defaultErrorListener.valid();
		}

		// Return the configuration
		return configuration;
	}

	/**
	 * Loads the configuration to the {@link GridPane}.
	 * 
	 * @param model
	 *            Model.
	 * @param configuartionNode
	 *            Configuration {@link Node}.
	 * @param grid
	 *            {@link GridPane}.
	 * @param actioner
	 *            {@link Actioner}.
	 * @param dirtyProperty
	 *            Dirty {@link Property}.
	 * @param validProperty
	 *            Valid {@link Property}.
	 * @param errorListener
	 *            {@link ErrorListener}.
	 */
	public Configuration recursiveLoadConfiguration(M model, Node configurationNode, GridPane grid, Actioner actioner,
			Property<Boolean> dirtyProperty, Property<Boolean> validProperty, ErrorListener errorListener) {

		// Apply CSS (so Scene available to inputs)
		grid.applyCss();

		// Load the value render list
		ValueLister<M> lister = new ValueLister<>(model, this.validator, configurationNode, grid, this.title,
				dirtyProperty, validProperty, errorListener, actioner, this.getValueRendererFactories(), null);
		lister.organiseWide(1); // ensure initially organised

		// Responsive view
		final double RESPONSIVE_WIDTH = 800;
		InvalidationListener listener = (event) -> {
			if (grid.getWidth() < RESPONSIVE_WIDTH) {
				// Avoid events if already narrow
				if (lister.isWideNotNarrow) {
					lister.organiseNarrow(1);
				}
			} else {
				// Again avoid events if already wide
				if (!lister.isWideNotNarrow) {
					lister.organiseWide(1);
				}
			}
		};
		grid.widthProperty().addListener(listener);
		listener.invalidated(null); // organise initial view

		// Ensure display potential error
		lister.refreshError();

		// Return the configuration
		return lister;
	}

	/**
	 * Convenience method to register the {@link ValueRendererFactory}.
	 * 
	 * @param builder
	 *            Builder.
	 * @return Input builder.
	 */
	private <B extends ValueRendererFactory<M, ? extends ValueInput>> B registerBuilder(B builder) {
		this.rendererFactories.add(builder);
		return builder;
	}

	/*
	 * ================== ConfigurationBuilder ====================
	 */

	@Override
	public ConfigurationBuilder<M> title(String title) {
		this.title = title;
		return this;
	}

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
		return this.registerBuilder(new ChoiceBuilderImpl<>(label, this.osgiBridge, this.parentShell));
	}

	@Override
	public <I> ListBuilder<M, I> list(String label, Class<I> itemType) {
		return this.registerBuilder(new ListBuilderImpl<>(label));
	}

	@Override
	public <I> MultipleBuilder<M, I> multiple(String label, Class<I> itemType) {
		return this.registerBuilder(new MultipleBuilderImpl<>(label, this.osgiBridge, this.parentShell));
	}

	@Override
	public PropertiesBuilder<M> properties(String label) {
		return this.registerBuilder(new PropertiesBuilderImpl<>(label));
	}

	@Override
	public MappingBuilder<M> map(String label, Function<M, ObservableList<String>> getSources,
			Function<M, ObservableList<String>> getTargets) {
		return this.registerBuilder(new MappingBuilderImpl<>(label, getSources, getTargets));
	}

	@Override
	public ClassBuilder<M> clazz(String label) {
		return this.registerBuilder(new ClassBuilderImpl<>(label, this.osgiBridge, this.getParentShell()));
	}

	@Override
	public ResourceBuilder<M> resource(String label) {
		return this.registerBuilder(new ResourceBuilderImpl<>(label, this.osgiBridge, this.getParentShell()));
	}

	@Override
	public void validate(ValueValidator<M, M> validator) {
		this.validator = validator;
	}

	@Override
	public void error(ErrorListener errorListener) {
		this.errorListener = errorListener;
	}

	@Override
	public void apply(String label, Applier<M> applier) {
		if ((label == null) || (label.trim().length() == 0)) {
			label = "Apply"; // ensure have label
		}
		this.applierLabel = label;
		this.applier = applier;
	}

	@Override
	public void close(CloseListener closeListener) {
		this.closeListener = closeListener;
	}

	/**
	 * Lists some of the {@link ValueRenderer} instances.
	 */
	private static class ValueLister<M> implements ValueRendererContext<M>, Configuration {

		/**
		 * Model.
		 */
		private final M model;

		/**
		 * {@link ValueValidator} for the model.
		 */
		private final ValueValidator<M, M> modelValidator;

		/**
		 * Configuration {@link Node}.
		 */
		private final Node configuartionNode;

		/**
		 * Title.
		 */
		private final String title;

		/**
		 * {@link GridPane}.
		 */
		private final GridPane grid;

		/**
		 * Indicates whether configuration is dirty.
		 */
		private final Property<Boolean> dirtyProperty;

		/**
		 * Indicates whether configuration is valid.
		 */
		private final Property<Boolean> validProperty;

		/**
		 * {@link ErrorListener}.
		 */
		private final ErrorListener errorListener;

		/**
		 * {@link Actioner}.
		 */
		private final Actioner actioner;

		/**
		 * {@link Input} instances for this {@link ValueLister}.
		 */
		private final List<Input<M, ? extends ValueInput>> inputs = new LinkedList<>();

		/**
		 * Indicates if organised for wide not narrow view.
		 */
		private boolean isWideNotNarrow = false;

		/**
		 * Row index for organising this list.
		 */
		private int rowIndex;

		/**
		 * Previous {@link ValueLister}.
		 */
		private final ValueLister<M> prevLister;

		/**
		 * Next {@link ValueLister}.
		 */
		private ValueLister<M> nextLister = null;

		/**
		 * Instantiate.
		 * 
		 * @param model
		 *            Model.
		 * @param modelValidator
		 *            {@link ValueValidator} for the model. May be <code>null</code>.
		 * @param configurationNode
		 *            Configuration {@link Node}.
		 * @param grid
		 *            {@link GridPane}.
		 * @param title
		 *            Title.
		 * @param dirtyProperty
		 *            Dirty {@link Property}.
		 * @param validProperty
		 *            Valid {@link Property}.
		 * @param errorListener
		 *            {@link ErrorListener}.
		 * @param actioner
		 *            {@link Actioner}.
		 * @param rowIndex
		 *            Row index within wide view {@link GridPane} to continue rendering
		 *            inputs.
		 * @param rendererFactories
		 *            {@link ValueRendererFactory} instances.
		 * @param prevLister
		 *            Previous {@link ValueLister}.
		 */
		@SuppressWarnings("unchecked")
		public ValueLister(M model, ValueValidator<M, M> modelValidator, Node configurationNode, GridPane grid,
				String title, Property<Boolean> dirtyProperty, Property<Boolean> validProperty,
				ErrorListener errorListener, Actioner actioner,
				ValueRendererFactory<M, ? extends ValueInput>[] rendererFactories, ValueLister<M> prevLister) {
			this.model = model;
			this.modelValidator = modelValidator;
			this.configuartionNode = configurationNode;
			this.grid = grid;
			this.dirtyProperty = dirtyProperty;
			this.validProperty = validProperty;
			this.errorListener = errorListener;
			this.actioner = actioner;
			this.title = title;
			this.prevLister = prevLister;

			// Ensure activate the inputs (once added)
			List<ValueInput> inputsToActivate = new ArrayList<>(rendererFactories.length * 2);
			try {

				// Render in the items
				for (int i = 0; i < rendererFactories.length; i++) {
					ValueRendererFactory<M, ? extends ValueInput> rendererFactory = rendererFactories[i];

					// Create the renderer
					ValueRenderer<M, ValueInput> renderer = (ValueRenderer<M, ValueInput>) rendererFactory
							.createValueRenderer(this);

					// Obtain the value input
					ValueInput valueInput = renderer.createInput();
					inputsToActivate.add(valueInput);

					// Add the input
					Node valueInputNode = valueInput.getNode();
					if (valueInputNode != null) {
						grid.getChildren().add(valueInputNode);
					}

					// Add the label (if provided)
					String labelText = renderer.getLabel(valueInput);
					Node label = null;
					if ((labelText == null) || (labelText.trim().length() == 0)) {
						labelText = null; // no label
					} else {
						// Create the label
						label = renderer.createLabel(labelText, valueInput);
						if (label != null) {
							grid.getChildren().add(label);
						}
					}

					// Add the error feedback (if provided)
					Node error = renderer.createErrorFeedback(valueInput);
					if (error != null) {
						grid.getChildren().add(error);
					}

					// Register the input
					Input<M, ? extends ValueInput> input = new Input<>(labelText, label, error, valueInputNode,
							valueInput, renderer);
					this.inputs.add(input);
					grid.getChildren().add(input.spacing);

					// Determine if choice value renderer
					if (valueInput instanceof ChoiceValueInput) {
						ChoiceValueInput<M> choiceRenderer = (ChoiceValueInput<M>) valueInput;

						// Load choice
						ValueRendererFactory<M, ? extends ValueInput>[] splitRenderers = Arrays
								.copyOfRange(rendererFactories, i + 1, rendererFactories.length);
						Runnable loadChoice = () -> {

							// Obtain the choice
							Integer choice = choiceRenderer.getChoiceIndex().getValue();
							if (choice == null) {
								// No choice selected, so carry on with renderers
								this.nextLister = new ValueLister<>(this.model, this.modelValidator, configurationNode,
										grid, this.title, this.dirtyProperty, this.validProperty, this.errorListener,
										this.actioner, splitRenderers, this);

							} else {
								// Have choice, so create concat list of remaining
								ValueRendererFactory<M, ? extends ValueInput>[] choiceRenderers = choiceRenderer
										.getChoiceValueRendererFactories()[choice].get();
								ValueRendererFactory<M, ? extends ValueInput>[] remainingRenderers = new ValueRendererFactory[choiceRenderers.length
										+ splitRenderers.length];
								for (int c = 0; c < choiceRenderers.length; c++) {
									remainingRenderers[c] = choiceRenderers[c];
								}
								for (int s = 0; s < splitRenderers.length; s++) {
									remainingRenderers[choiceRenderers.length + s] = splitRenderers[s];
								}
								this.nextLister = new ValueLister<>(this.model, this.modelValidator, configurationNode,
										grid, this.title, this.dirtyProperty, this.validProperty, this.errorListener,
										this.actioner, remainingRenderers, this);
							}

							// Organise choice changes
							if (this.isWideNotNarrow) {
								this.organiseWide(this.rowIndex);
							} else {
								this.organiseNarrow(this.rowIndex);
							}

							// Refresh error (now different choice)
							this.refreshError();
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

			} finally {
				// Activate the inputs
				for (ValueInput input : inputsToActivate) {
					input.activate();
				}
			}
		}

		/**
		 * Organises for wide view.
		 * 
		 * @param rowIndex
		 *            Row index to start organising inputs.
		 */
		private void organiseWide(int rowIndex) {
			this.rowIndex = rowIndex;

			// Organise the inputs
			for (Input<M, ? extends ValueInput> input : this.inputs) {
				if (input.label != null) {
					GridPane.setConstraints(input.label, 1, rowIndex, 1, 1, HPos.LEFT, VPos.CENTER, Priority.SOMETIMES,
							Priority.ALWAYS);
				}
				if (input.errorFeedback != null) {
					GridPane.setConstraints(input.errorFeedback, 2, rowIndex, 1, 1, HPos.CENTER, VPos.CENTER,
							Priority.SOMETIMES, Priority.ALWAYS);
				}
				if (input.input != null) {
					GridPane.setConstraints(input.input, 3, rowIndex, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS,
							Priority.ALWAYS);
				}
				GridPane.setConstraints(input.spacing, 4, rowIndex, 1, 1, HPos.LEFT, VPos.TOP, Priority.NEVER,
						Priority.NEVER);

				// Increment for next row
				rowIndex += 2;
			}

			// Ensure next lister is also organised
			if (this.nextLister != null) {
				this.nextLister.organiseWide(rowIndex);
			}

			// Indicate now wide view
			this.grid.getStyleClass().remove(CSS_CLASS_NARROW);
			this.grid.getStyleClass().add(CSS_CLASS_WIDE);
			this.isWideNotNarrow = true;
		}

		/**
		 * Organises for narrow view.
		 * 
		 * @param rowIndex
		 *            Row index to start organising inputs.
		 */
		private void organiseNarrow(int rowIndex) {
			this.rowIndex = rowIndex;

			// Organise the inputs
			for (Input<M, ? extends ValueInput> input : this.inputs) {
				if (input.label != null) {
					GridPane.setConstraints(input.label, 1, rowIndex, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS,
							Priority.ALWAYS);
				}
				if ((input.errorFeedback != null) && (input.input != null)) {
					rowIndex++;
					if (input.errorFeedback != null) {
						GridPane.setConstraints(input.errorFeedback, 0, rowIndex, 1, 1, HPos.RIGHT, VPos.CENTER,
								Priority.SOMETIMES, Priority.ALWAYS);
					}
					if (input.input != null) {
						// Provide input on next row
						GridPane.setConstraints(input.input, 1, rowIndex, 2, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS,
								Priority.ALWAYS);
					}
				}

				// Increment for next row
				rowIndex++;
				GridPane.setConstraints(input.spacing, 1, rowIndex, 2, 1, HPos.LEFT, VPos.TOP, Priority.NEVER,
						Priority.ALWAYS);
				rowIndex++;
			}

			// Ensure next lister is also organised
			if (this.nextLister != null) {
				this.nextLister.organiseNarrow(rowIndex);
			}

			// Indicate now narrow view
			this.grid.getStyleClass().remove(CSS_CLASS_WIDE);
			this.grid.getStyleClass().add(CSS_CLASS_NARROW);
			this.isWideNotNarrow = false;
		}

		/**
		 * Removes the controls from view.
		 */
		private void removeControls() {
			for (Input<M, ? extends ValueInput> input : this.inputs) {
				if (input.label != null) {
					this.grid.getChildren().remove(input.label);
				}
				if (input.errorFeedback != null) {
					this.grid.getChildren().remove(input.errorFeedback);
				}
				if (input.input != null) {
					this.grid.getChildren().remove(input.input);
				}
				this.grid.getChildren().remove(input.spacing);
			}
		}

		/*
		 * ================== ValueRendererContext ====================
		 */

		@Override
		public M getModel() {
			return this.model;
		}

		@Override
		public Actioner getOptionalActioner() {
			return this.actioner;
		}

		@Override
		public ErrorListener getErrorListener() {
			return this.errorListener;
		}

		@Override
		public void reload(Builder<?, ?, ?> builder) {

			// Obtain the first value lister
			ValueLister<M> firstValueLister = this;
			while (firstValueLister.prevLister != null) {
				firstValueLister = firstValueLister.prevLister;
			}

			// Search for the builder
			ValueLister<M> lister = firstValueLister;
			while (lister != null) {
				for (Input<M, ?> input : lister.inputs) {
					input.renderer.reloadIf(builder);
				}
				lister = lister.nextLister;
			}
		}

		/**
		 * Ensure do not recursively refresh the error.
		 */
		private boolean isRefreshingError = false;

		@Override
		@SuppressWarnings("unchecked")
		public void refreshError() {

			// Obtain the first value lister
			ValueLister<M> firstValueLister = this;
			while (firstValueLister.prevLister != null) {
				firstValueLister = firstValueLister.prevLister;
			}

			// Ensure not recursively refresh error
			if (firstValueLister.isRefreshingError) {
				return;
			}

			// Refresh the error
			firstValueLister.isRefreshingError = true;
			try {

				// Search for first error
				InputError<M, ? extends ValueInput> errorInput = firstValueLister.getFirstError();

				// If valid, undertake validation of model
				if ((errorInput == null) && (this.modelValidator != null)) {
					try {

						// Validate the model
						InputError<M, ? extends ValueInput>[] validateError = new InputError[] { null };
						this.modelValidator.validate(new ValueValidatorContext<M, M>() {

							@Override
							public M getModel() {
								return ValueLister.this.model;
							}

							@Override
							public ReadOnlyProperty<M> getValue() {
								return new SimpleObjectProperty<>(ValueLister.this.model);
							}

							@Override
							public void setError(String message) {
								validateError[0] = new InputError<>(null, new MessageOnlyException(message));
							}

							@Override
							public void reload(Builder<?, ?, ?> builder) {
								ValueLister.this.reload(builder);
							}
						});
						errorInput = validateError[0];

					} catch (Throwable ex) {
						// Provide failure error
						errorInput = new InputError<>(null, ex);
					}
				}

				// Indicate if valid
				this.validProperty.setValue(errorInput == null);

				// Determine if error
				if (errorInput == null) {
					this.errorListener.valid();
					return; // no error
				}

				// Provide appropriate error
				String label = errorInput.input != null ? errorInput.input.labelText : null;
				if (errorInput.error instanceof MessageOnlyException) {
					this.errorListener.error(label, errorInput.error.getMessage());
				} else {
					this.errorListener.error(label, errorInput.error);
				}

			} finally {
				firstValueLister.isRefreshingError = false;
			}
		}

		/**
		 * Obtains the first {@link ValueRenderer} error.
		 * 
		 * @return First {@link ValueRenderer} error or <code>null</code>
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private InputError<M, ? extends ValueInput> getFirstError() {

			// Search through for the first error
			for (Input input : this.inputs) {

				// Determine if first error
				Throwable error = input.renderer.getError(input.valueInput);
				if (error != null) {
					return new InputError<>(input, error);
				}
			}

			// No error, so determine error in next list
			return (this.nextLister != null) ? this.nextLister.getFirstError() : null;
		}

		/*
		 * ============ Configuration =====================
		 */

		@Override
		public Node getConfigurationNode() {
			return this.configuartionNode;
		}

		@Override
		public String getTitle() {
			return this.title;
		}

		@Override
		public Property<Boolean> dirtyProperty() {
			return this.dirtyProperty;
		}

		@Override
		public Property<Boolean> validProperty() {
			return this.validProperty;
		}

		@Override
		public Actioner getActioner() {
			if (this.actioner == null) {
				throw new IllegalStateException("No apply configuration for model " + this.model.getClass().getName());
			}
			return this.actioner;
		}
	}

	/**
	 * {@link Actioner} implementation.
	 */
	private static class ActionerImpl<M> implements Actioner {

		/**
		 * Model.
		 */
		private final M model;

		/**
		 * Label.
		 */
		private final String label;

		/**
		 * Applier.
		 */
		private final Applier<M> applier;

		/**
		 * Dirty {@link Property}.
		 */
		private final Property<Boolean> dirtyProperty;

		/**
		 * {@link CloseListener}.
		 */
		private final CloseListener closeListener;

		/**
		 * {@link ErrorListener}.
		 */
		private ErrorListener errorListener;

		/**
		 * Instantiate.
		 * 
		 * @param model
		 *            Model.
		 * @param label
		 *            Label.
		 * @param applier
		 *            {@link Applier}.
		 * @param dirtyProperty
		 *            Dirty {@link Property}.
		 * @param closeListner
		 *            {@link CloseListener}.
		 */
		private ActionerImpl(M model, String label, Applier<M> applier, Property<Boolean> dirtyProperty,
				CloseListener closeListner) {
			this.model = model;
			this.label = label;
			this.applier = applier;
			this.dirtyProperty = dirtyProperty;
			this.closeListener = closeListner;
		}

		/*
		 * ============== Actioner ===================
		 */

		@Override
		public String getLabel() {
			return this.label;
		}

		@Override
		public void action() {

			// Apply the model (handle potential failure in applying)
			try {
				this.applier.apply(this.model);

			} catch (MessageOnlyApplyException ex) {
				this.errorListener.error(null, ex.getMessage());
				return; // failed to apply, so do not carry on to close

			} catch (Throwable ex) {
				this.errorListener.error(null, ex);
				return; // failed to apply, so do not carry on to close
			}

			// No longer dirty
			this.dirtyProperty.setValue(false);

			// Notify that applied
			if (this.closeListener != null) {
				this.closeListener.applied();
			}
		}
	}

	/**
	 * Input.
	 */
	private static class Input<M, I extends ValueInput> {

		/**
		 * Label text for the input.
		 */
		private final String labelText;

		/**
		 * Label for the input.
		 */
		private final Node label;

		/**
		 * Error feedback for the input.
		 */
		private final Node errorFeedback;

		/**
		 * {@link Node} to capture the input.
		 */
		private final Node input;

		/**
		 * Used for spacing.
		 */
		private final Pane spacing = new Pane();

		/**
		 * {@link ValueInput}.
		 */
		private final I valueInput;

		/**
		 * {@link ValueRenderer}. As bindings are weak references, need strong reference
		 * to {@link ValueRenderer} to keep bindings active.
		 */
		private final ValueRenderer<M, I> renderer;

		/**
		 * Instantiate.
		 * 
		 * @param labelText
		 *            Label text for the input.
		 * @param label
		 *            Label for the input.
		 * @param errorFeedback
		 *            Error feedback for the input.
		 * @param input
		 *            {@link Node} to capture the input.
		 * @param valueInput
		 *            {@link ValueInput}.
		 * @param renderer
		 *            {@link ValueRenderer}.
		 */
		private Input(String labelText, Node label, Node errorFeedback, Node input, I valueInput,
				ValueRenderer<M, I> renderer) {
			this.labelText = labelText;
			this.label = label;
			this.errorFeedback = errorFeedback;
			this.input = input;
			this.valueInput = valueInput;
			this.renderer = renderer;
		}
	}

	/**
	 * Input error.
	 */
	private static class InputError<M, I extends ValueInput> {

		/**
		 * {@link Input}.
		 */
		private final Input<M, I> input;

		/**
		 * {@link Throwable} error.
		 */
		private final Throwable error;

		/**
		 * Instantiate.
		 * 
		 * @param input
		 *            {@link Input}.
		 * @param error
		 *            {@link Throwable} error.
		 */
		private InputError(Input<M, I> input, Throwable error) {
			this.input = input;
			this.error = error;
		}
	}

	/**
	 * Default {@link ErrorListener}.
	 */
	private static class DefaultErrorListener<M> implements ErrorListener {

		/**
		 * Header.
		 */
		private final GridPane header = new GridPane();

		/**
		 * Valid header.
		 */
		private final HBox validHeader;

		/**
		 * Error header.
		 */
		private final HBox errorHeader;

		/**
		 * Error {@link Label}.
		 */
		private final Label errorLabel;

		/**
		 * Toggle for showing the stack trace.
		 */
		private final HBox stackTraceToggle = new HBox();

		/**
		 * Indicate if showing stack trace.
		 */
		private boolean isShowingStackTrace = false;

		/**
		 * Stack trace.
		 */
		private final TextArea stackTrace = new TextArea();

		/**
		 * Instantiate.
		 * 
		 * @param title
		 *            Title.
		 * @param actioner
		 *            {@link Actioner}.
		 * @param grid
		 *            {@link GridPane}.
		 * @param scroll
		 *            {@link ScrollPane}.
		 * @param closeListener
		 *            {@link CloseListener}.
		 * @param dirtyProperty
		 *            Dirty {@link Property}.
		 * @param validProperty
		 *            Valid {@link Property}.
		 */
		private DefaultErrorListener(String title, Actioner actioner, VBox wrapper, ScrollPane scroll,
				CloseListener closeListener, Property<Boolean> dirtyProperty, ObservableBooleanValue validProperty) {

			// Style the header
			this.header.getStyleClass().setAll("header-panel", "dialog-pane", "button-bar", "configurer-header");

			// Provide the error header
			this.errorHeader = new HBox(10.0);
			this.errorHeader.getStyleClass().setAll("error");
			this.errorHeader.alignmentProperty().setValue(Pos.CENTER_LEFT);
			this.errorHeader.setVisible(false);
			GridPane.setHgrow(this.errorHeader, Priority.ALWAYS);
			this.header.add(this.errorHeader, 0, 0);

			// Provide the error header details
			this.errorLabel = new Label();
			this.errorHeader.getChildren().setAll(
					new ImageView(new Image(DefaultImages.ERROR_IMAGE_PATH, 15, 15, true, true)), this.errorLabel);

			// Provide the valid header
			this.validHeader = new HBox(10.0);
			this.validHeader.getStyleClass().setAll("valid");
			this.validHeader.alignmentProperty().setValue(Pos.CENTER_LEFT);
			GridPane.setHgrow(this.validHeader, Priority.ALWAYS);
			this.header.add(this.validHeader, 0, 0);

			// Provide valid header details
			if (actioner != null) {
				Button actionButton = new Button(actioner.getLabel());
				actionButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("default"), true);
				actionButton.setOnAction((event) -> actioner.action());
				actionButton.disableProperty().bind(Bindings.not(validProperty));
				this.validHeader.getChildren().add(actionButton);
			}
			if (title != null) {
				this.validHeader.getChildren().add(new Label(title));
			}

			// Stack trace not editable and fills area
			this.stackTrace.setEditable(false);
			this.stackTrace.prefHeightProperty().bind(scroll.prefHeightProperty());
			this.stackTrace.prefWidthProperty().bind(wrapper.widthProperty());

			// Provide stack trace toggle
			Hyperlink stackTraceToggle = new Hyperlink();
			stackTraceToggle.setText("Show Stack Trace");
			stackTraceToggle.getStyleClass().setAll("details-button", "more");
			stackTraceToggle.setOnAction((event) -> {
				if (this.isShowingStackTrace) {
					// Hide stack trace
					wrapper.getChildren().setAll(this.header, scroll);
					stackTraceToggle.setText("Show Stack Trace");
					stackTraceToggle.getStyleClass().setAll("details-button", "more");
					this.isShowingStackTrace = false;
				} else {
					// Show stack trace
					wrapper.getChildren().setAll(this.header, this.stackTrace);
					stackTraceToggle.setText("Hide Stack Trace");
					stackTraceToggle.getStyleClass().setAll("details-button", "less");
					this.isShowingStackTrace = true;
				}
			});
			this.stackTraceToggle.getChildren().add(stackTraceToggle);
			this.stackTraceToggle.getStyleClass().setAll("container");
			this.stackTraceToggle.alignmentProperty().setValue(Pos.CENTER_RIGHT);
			GridPane.setHgrow(this.stackTraceToggle, Priority.SOMETIMES);
			this.header.add(this.stackTraceToggle, 1, 0);

			// Determine if cancel
			if (closeListener != null) {
				// Provide cancel button
				Button cancelButton = new Button("Cancel");
				cancelButton.alignmentProperty().setValue(Pos.CENTER_RIGHT);
				cancelButton.setOnAction((event) -> closeListener.cancelled());
				cancelButton.setCancelButton(true);
				GridPane.setHgrow(cancelButton, Priority.ALWAYS);
				this.header.add(cancelButton, 2, 0);
			}
		}

		/*
		 * ============= ErrorListener ====================
		 */

		@Override
		public void error(String inputLabel, String message) {

			// Display the error
			this.errorLabel.setText((inputLabel == null ? "" : inputLabel + ": ") + message);
			this.errorHeader.setVisible(true);

			// No stack trace
			this.stackTraceToggle.setVisible(false);

			// Disallow applying
			this.validHeader.setVisible(false);
		}

		@Override
		public void error(String inputLabel, Throwable error) {

			// Obtain the error message
			String message = error.getMessage();
			if ((message == null) || (message.trim().length() == 0)) {
				message = error.getClass().getName();
			}

			// Display the error
			this.error(inputLabel, message);

			// Provide stack trace on tool tip
			StringWriter buffer = new StringWriter();
			error.printStackTrace(new PrintWriter(buffer));
			this.stackTrace.setText(buffer.toString());
			this.stackTraceToggle.setVisible(true);
		}

		@Override
		public void valid() {
			this.errorHeader.setVisible(false);
			this.validHeader.setVisible(true);
			this.stackTraceToggle.setVisible(false);
		}
	}

}