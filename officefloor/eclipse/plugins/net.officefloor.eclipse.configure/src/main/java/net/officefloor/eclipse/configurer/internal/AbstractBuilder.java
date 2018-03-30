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

import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import net.officefloor.eclipse.configurer.Builder;
import net.officefloor.eclipse.configurer.Configurer;
import net.officefloor.eclipse.configurer.ValueLoader;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.configurer.ValueValidator.ValueValidatorContext;

/**
 * Abstract {@link ValueRenderer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractBuilder<M, V, B extends Builder<M, V, B>>
		implements Builder<M, V, B>, ValueRenderer<M>, ValueValidatorContext<V>, ColumnRenderer<M, V> {

	/**
	 * Path to the error {@link Image}.
	 */
	private static final String ERROR_IMAGE_PATH = "/" + Configurer.class.getPackage().getName().replace('.', '/')
			+ "/error.png";

	/**
	 * Label.
	 */
	private final String label;

	/**
	 * Tracks if an error occurred on validation.
	 */
	private boolean isError = false;

	/**
	 * Current error with value.
	 */
	private Throwable error = null;

	/**
	 * Error.
	 */
	private final StringProperty errorMessage = new SimpleStringProperty();

	/**
	 * {@link ValueLoader}.
	 */
	private ValueLoader<M, V> valueLoader = null;

	/**
	 * {@link Function} to obtain the initial value from the model.
	 */
	private Function<M, V> getInitialValue = null;

	/**
	 * {@link ValueValidator}.
	 */
	private ValueValidator<V> validator = null;

	/**
	 * {@link ObservableValue}.
	 */
	private final Property<V> value = new SimpleObjectProperty<>();

	/**
	 * {@link ValueRendererContext}.
	 */
	private ValueRendererContext<M> context;

	/**
	 * Model.
	 */
	private M model;

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 */
	public AbstractBuilder(String label) {
		this.label = label;
	}

	/**
	 * Creates the input {@link Node} for the {@link ObservableValue}.
	 * 
	 * @param value
	 *            {@link ObservableValue} to be updated.
	 * @return {@link Node} to configure the {@link ObservableValue}.
	 */
	protected abstract Node createInput(Property<V> value);

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
	 * @param column
	 *            {@link TableColumn}.
	 * @param callback
	 *            {@link Callback}.
	 */
	protected <R> void configureTableColumn(TableColumn<R, V> column, Callback<Integer, ObservableValue<V>> callback) {
	}

	/**
	 * Obtains the label.
	 * 
	 * @return Label.
	 */
	protected String getLabel() {
		return this.label == null ? "" : this.label;
	}

	/**
	 * Obtains the model.
	 * 
	 * @return Model.
	 */
	protected M getModel() {
		return this.model;
	}

	/**
	 * Obtains the {@link ValueRendererContext}.
	 * 
	 * @return {@link ValueRendererContext}.
	 */
	protected ValueRendererContext<M> getValueRendererContext() {
		return this.context;
	}

	/**
	 * Allow initialising from model.
	 * 
	 * @param model
	 *            Model.
	 */
	protected void init(M model) {
	}

	/**
	 * Undertakes validation.
	 */
	private void validate() {

		// Capture the initial error (to see if change)
		Throwable previousError = this.error;

		// Reset error for the validate
		this.isError = false;
		this.error = null;
		try {
			// Undertake validation
			this.validator.validate(this);
		} catch (Exception ex) {
			// Provide error message
			String message = ex.getMessage();
			if ((message == null) || (message.length() == 0)) {
				// Provide message from exception
				message = "Thrown exception " + ex.getClass().getName();
			}
			this.setError(message);

			// Flag error and override with exception
			this.isError = true;
			this.error = ex;
		}
		if (!this.isError) {
			// No error in validate, so clear error
			this.setError(null);

			// Determine if previously an error
			if (previousError != null) {
				this.context.refreshError();
			}

		} else {
			if ((previousError instanceof MessageOnlyException) && (this.error instanceof MessageOnlyException)) {
				// Determine if change in error message only
				if (!previousError.getMessage().equals(this.error.getMessage())) {
					// Change in message, so notify error
					this.context.refreshError();
				}

			} else if ((previousError != null) && (this.error != null)) {
				if (!previousError.equals(this.error)) {
					// Change in exception, so notify error
					this.context.refreshError();
				}
			} else if ((previousError == null) && (this.error == null)) {
				// No error, so no change
			} else {
				// Change to have/clear error
				this.context.refreshError();
			}
		}
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
	public B validate(ValueValidator<V> validator) {
		this.validator = validator;
		this.value.addListener((event) -> this.validate());
		return (B) this;
	}

	@Override
	public ReadOnlyProperty<V> getValue() {
		return this.value;
	}

	@Override
	@SuppressWarnings("unchecked")
	public B setValue(ValueLoader<M, V> valueLoader) {
		this.valueLoader = valueLoader;
		return (B) this;
	}

	/*
	 * ========== ValueValidatorContext =============
	 */

	@Override
	public void setError(String message) {

		// Blank string considered no exception
		if ((message != null) && (message.length() == 0)) {
			message = null;
		}

		// Update the error feedback
		this.isError = (message != null);
		this.errorMessage.set(message);

		// Provide message only exception
		if (message == null) {
			// No error
			this.error = null;
		} else {
			// Load message only exception
			this.error = new MessageOnlyException(message);
		}
	}

	/*
	 * ============ ValueRenderer =================
	 */

	@Override
	public void init(ValueRendererContext<M> context) {
		this.context = context;

		// Determine if change in model
		if (this.model != this.context.getModel()) {

			// Re-initialise to model
			this.model = this.context.getModel();
			if (this.getInitialValue != null) {
				V initialValue = this.getInitialValue.apply(this.model);
				this.value.setValue(initialValue);
			}

			// Allow any further model initialising
			this.init(this.getModel());
		}
	}

	@Override
	public Node createLabel() {
		return new Label(this.label);
	}

	@Override
	public Node createErrorFeedback() {
		ImageView error = new ImageView(new Image(ERROR_IMAGE_PATH, 15, 15, true, true));
		Tooltip errorTooltip = new Tooltip();
		errorTooltip.getStyleClass().add("error-tooltip");
		Tooltip.install(error, errorTooltip);
		error.visibleProperty().set(false); // default hide
		this.errorMessage.addListener((event) -> {
			String errorText = this.errorMessage.get();
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

	@Override
	public Node createInput() {
		return this.createInput(this.value);
	}

	@Override
	public Throwable getError() {
		return this.error;
	}

	@Override
	public void loadValue(M model) {
		if (this.valueLoader != null) {
			this.valueLoader.loadValue(model, this.value.getValue());
		}
	}

	/*
	 * ============ ColumnRenderer =================
	 */

	@Override
	public <R> TableColumn<R, V> createTableColumn(Callback<Integer, ObservableValue<V>> callback) {
		TableColumn<R, V> column = new TableColumn<>(this.label);
		this.configureTableColumn(column, callback);
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
		private final Property<V> value = AbstractBuilder.this.createCellProperty();

		/**
		 * {@link ValueRendererContext}.
		 */
		private final ValueRendererContext<M> context;

		/**
		 * Instantiate.
		 * 
		 * @param context
		 *            {@link ValueRendererContext}.
		 */
		private CellRendererImpl(ValueRendererContext<M> context) {
			this.context = context;

			// Load initial value
			if (AbstractBuilder.this.getInitialValue != null) {
				V value = AbstractBuilder.this.getInitialValue.apply(this.context.getModel());
				this.value.setValue(value);
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