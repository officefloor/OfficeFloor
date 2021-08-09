/*-
 * #%L
 * [bundle] OfficeFloor Configurer
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.gef.configurer.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import net.officefloor.gef.configurer.Actioner;
import net.officefloor.gef.configurer.Builder;
import net.officefloor.gef.configurer.DefaultImages;
import net.officefloor.gef.configurer.ErrorListener;
import net.officefloor.gef.configurer.ValueLoader;
import net.officefloor.gef.configurer.ValueValidator;
import net.officefloor.gef.configurer.ValueValidator.ValueValidatorContext;

/**
 * Abstract {@link ValueRenderer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractBuilder<M, V, I extends ValueInput, B extends Builder<M, V, B>>
		implements Builder<M, V, B>, ValueRendererFactory<M, I>, ColumnRenderer<M, V> {

	/**
	 * Label.
	 */
	private final String label;

	/**
	 * {@link ValueLoader}.
	 */
	private ValueLoader<M, V> valueLoader = null;

	/**
	 * {@link Function} to obtain the initial value from the model.
	 */
	private Function<M, V> getInitialValue = null;

	/**
	 * {@link ValueValidator} instances.
	 */
	private List<ValueValidator<M, V>> validators = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param label Label.
	 */
	public AbstractBuilder(String label) {
		this.label = label;
	}

	/**
	 * Creates the input {@link ValueInput} for the {@link ObservableValue}.
	 * 
	 * @param context {@link ValueInputContext}.
	 * @return {@link ValueInput} to configure the {@link ObservableValue}.
	 */
	protected abstract I createInput(ValueInputContext<M, V> context);

	/**
	 * Creates the label {@link Node}.
	 * 
	 * @param labelText  Label text.
	 * @param valueInput {@link ValueInput}.
	 * @return Label {@link Node}.
	 */
	protected Node createLabel(String labelText, I valueInput) {
		return new Label(this.getLabel());
	}

	/**
	 * Creates the error feedback {@link Node}.
	 * 
	 * @param valueInput    {@link ValueInput}.
	 * @param errorProperty Error {@link Property}.
	 * @return Error feedback {@link Node}.
	 */
	protected Node createErrorFeedback(I valueInput, Property<Throwable> errorProperty) {
		ImageView error = new ImageView(new Image(DefaultImages.ERROR_IMAGE_PATH, 15, 15, true, true));
		Tooltip errorTooltip = new Tooltip();
		errorTooltip.getStyleClass().add("error-tooltip");
		Tooltip.install(error, errorTooltip);
		ChangeListener<Throwable> listener = (observable, oldValue, newValue) -> {
			Throwable cause = newValue;
			if (cause != null) {
				// Display the error
				errorTooltip.setText(cause.getMessage());
				error.visibleProperty().set(true);
			} else {
				// No error
				error.visibleProperty().set(false);
			}
		};
		errorProperty.addListener(listener);
		listener.changed(errorProperty, null, errorProperty.getValue()); // Initialise
		return error;
	}

	/**
	 * Obtains the error.
	 * 
	 * @param valueInput {@link ValueInput}.
	 * @param error      {@link Throwable} error. May be <code>null</code> if no
	 *                   error.
	 * @return {@link Throwable} error or <code>null</code> if no error.
	 */
	protected Throwable getError(I valueInput, ReadOnlyProperty<Throwable> error) {
		return error.getValue();
	}

	/**
	 * Creates the {@link Property} for the {@link TableCell}.
	 * 
	 * @return {@link Property} for the {@link TableCell}.
	 */
	protected Property<V> createCellProperty() {
		return new SimpleObjectProperty<>();
	}

	/**
	 * Allow overriding to configure the {@link TableColumn}.
	 *
	 * @param <R>      Row object type.
	 * @param table    {@link TableView} that will contain the {@link TableColumn}.
	 * @param column   {@link TableColumn}.
	 * @param callback {@link Callback}.
	 */
	protected <R> void configureTableColumn(TableView<R> table, TableColumn<R, V> column,
			Callback<Integer, ObservableValue<V>> callback) {
	}

	/**
	 * Obtains the label.
	 * 
	 * @return Label.
	 */
	protected String getLabel() {
		return this.label == null ? "" : this.label;
	}

	/*
	 * ============ Builder ===============
	 */

	@Override
	@SuppressWarnings("unchecked")
	public B init(Function<M, V> getInitialValue) {
		this.getInitialValue = getInitialValue;
		return (B) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public B validate(ValueValidator<M, V> validator) {
		this.validators.add(validator);
		return (B) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public B setValue(ValueLoader<M, V> valueLoader) {
		this.valueLoader = valueLoader;
		return (B) this;
	}

	/*
	 * ============ ValueRendererFactory =================
	 */

	@Override
	public ValueRenderer<M, I> createValueRenderer(ValueRendererContext<M> context) {
		return new ValueRendererImpl(context);
	}

	/**
	 * {@link ValueRenderer} implementation.
	 */
	private class ValueRendererImpl
			implements ValueRenderer<M, I>, ValueValidatorContext<M, V>, ValueInputContext<M, V> {

		/**
		 * {@link ObservableValue}.
		 */
		private final Property<V> value = new SimpleObjectProperty<>();

		/**
		 * {@link ValueRendererContext}.
		 */
		private final ValueRendererContext<M> context;

		/**
		 * {@link ValueValidator} instances.
		 */
		private final List<ValueValidator<M, V>> validators = new ArrayList<>(1);

		/**
		 * Tracks if an error occurred on validation.
		 */
		private boolean isError = false;

		/**
		 * Error.
		 */
		private final Property<Throwable> error = new SimpleObjectProperty<>();

		/**
		 * Initialises the {@link ValueRenderer}.
		 */
		private ValueRendererImpl(ValueRendererContext<M> context) {
			this.context = context;

			// Obtain the model
			M model = this.context.getModel();

			// Load all the validators
			this.validators.addAll(AbstractBuilder.this.validators);

			// Initialise to model
			this.loadValue();

			// Validate initial value
			this.validate(); // validate initial value

			// Refresh on error
			this.error.addListener((observable, oldValue, newValue) -> this.context.refreshError());

			// Listen to changes of value
			this.value.addListener((observable, oldValue, newValue) -> {

				// Load value to model (so model consistent before validation)
				if (AbstractBuilder.this.valueLoader != null) {
					AbstractBuilder.this.valueLoader.loadValue(model, newValue);
				}

				// Track model becoming dirty
				context.dirtyProperty().setValue(true);

				// Undertake validation (after loading value to model)
				this.validate();
			});
		}

		/**
		 * Loads the value.
		 */
		private void loadValue() {
			if (AbstractBuilder.this.getInitialValue != null) {
				V initialValue = AbstractBuilder.this.getInitialValue.apply(this.context.getModel());
				this.value.setValue(initialValue);
			}
		}

		/**
		 * Undertakes validation.
		 */
		private void validate() {

			// Track whether error in validation
			this.isError = false;
			try {
				// Undertake validation
				Iterator<ValueValidator<M, V>> iterator = this.validators.iterator();
				while ((!this.isError) && (iterator.hasNext())) {
					ValueValidator<M, V> validator = iterator.next();
					validator.validate(this);
				}
			} catch (Throwable ex) {
				// Provide error message
				String message = ex.getMessage();
				if ((message == null) || (message.length() == 0)) {
					// Provide message from exception
					ex = new Exception("Thrown exception " + ex.getClass().getName(), ex);
				}

				// Flag error and override with exception
				this.isError = true;
				this.error.setValue(ex);
			}
			if (!this.isError) {
				// No error in validate, so clear error
				this.setError(null);
				
				// Refresh all error
				this.context.refreshError();
			}
		}

		/*
		 * ============ ValueInputContext ================
		 */

		@Override
		public M getModel() {
			return this.context.getModel();
		}

		@Override
		public Property<V> getInputValue() {
			return this.value;
		}

		@Override
		public void addValidator(ValueValidator<M, V> validator) {
			this.validators.add(validator);

			// Run validation immediately
			this.validate();
		}

		@Override
		public void refreshError() {
			this.context.refreshError();
		}

		@Override
		public Actioner getOptionalActioner() {
			return this.context.getOptionalActioner();
		}

		@Override
		public Property<Boolean> dirtyProperty() {
			return this.context.dirtyProperty();
		}

		@Override
		public Property<Boolean> validProperty() {
			return this.context.validProperty();
		}

		@Override
		public ErrorListener getErrorListener() {
			return this.context.getErrorListener();
		}

		/*
		 * =============== ValueRenderer =================
		 */

		@Override
		public I createInput() {
			return AbstractBuilder.this.createInput(this);
		}

		@Override
		public String getLabel(I valueInput) {
			return AbstractBuilder.this.getLabel();
		}

		@Override
		public Node createLabel(String labelText, I valueInput) {
			return AbstractBuilder.this.createLabel(labelText, valueInput);
		}

		@Override
		public Node createErrorFeedback(I valueInput) {
			return AbstractBuilder.this.createErrorFeedback(valueInput, this.error);
		}

		@Override
		public Throwable getError(I valueInput) {
			return AbstractBuilder.this.getError(valueInput, this.error);
		}

		/*
		 * ========== ValueValidatorContext =============
		 */

		@Override
		public ReadOnlyProperty<V> getValue() {
			return this.value;
		}

		@Override
		public void setError(String message) {

			// Blank string considered no exception
			if ((message != null) && (message.length() == 0)) {
				message = null;
			}

			// Update the error feedback
			this.isError = (message != null);
			this.error.setValue(this.isError ? new MessageOnlyException(message) : null);
		}

		@Override
		public void reload(Builder<?, ?, ?> builder) {
			this.context.reload(builder);
		}

		@Override
		public boolean reloadIf(Builder<?, ?, ?> builder) {
			boolean isBuilder = AbstractBuilder.this == builder;
			if (isBuilder) {
				// Require reloading this value
				this.loadValue();
			}
			return isBuilder;
		}
	}

	/*
	 * ============ ColumnRenderer =================
	 */

	@Override
	public <R> TableColumn<R, V> createTableColumn(TableView<R> table, Callback<Integer, ObservableValue<V>> callback) {
		TableColumn<R, V> column = new TableColumn<>(this.label);
		this.configureTableColumn(table, column, callback);
		return column;
	}

	@Override
	public boolean isEditable() {
		return (this.valueLoader != null);
	}

	@Override
	public CellRenderer<M, V> createCellRenderer(ValueRendererContext<M> context) {
		return new CellRendererImpl(context);
	}

	/**
	 * {@link CellRenderer} implementation.
	 */
	private class CellRendererImpl implements CellRenderer<M, V> {

		/**
		 * {@link ObservableValue}.
		 */
		private final Property<V> value;

		/**
		 * {@link ValueRendererContext}.
		 */
		private final ValueRendererContext<M> context;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link ValueRendererContext}.
		 */
		private CellRendererImpl(ValueRendererContext<M> context) {
			this.context = context;

			// Obtain the cell value property
			this.value = AbstractBuilder.this.createCellProperty();

			// Load initial value
			if ((AbstractBuilder.this.getInitialValue != null) && (this.context.getModel() != null)) {
				V value = AbstractBuilder.this.getInitialValue.apply(this.context.getModel());
				this.value.setValue(value);
			}

			// Always load value to model
			if (AbstractBuilder.this.valueLoader != null) {
				this.value.addListener((observable, oldValue, newValue) -> AbstractBuilder.this.valueLoader
						.loadValue(this.context.getModel(), this.value.getValue()));
			}
		}

		/*
		 * ============ CellRenderer ====================
		 */

		@Override
		public Property<V> getValue() {
			return this.value;
		}
	}

}
