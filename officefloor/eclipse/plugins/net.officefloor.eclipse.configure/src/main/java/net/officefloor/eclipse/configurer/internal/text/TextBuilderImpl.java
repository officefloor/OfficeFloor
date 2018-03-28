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
package net.officefloor.eclipse.configurer.internal.text;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import net.officefloor.eclipse.configurer.TextBuilder;
import net.officefloor.eclipse.configurer.ValueLoader;
import net.officefloor.eclipse.configurer.ValueValidator;
import net.officefloor.eclipse.configurer.ValueValidator.ValueValidatorContext;
import net.officefloor.eclipse.configurer.internal.ValueRenderer;
import net.officefloor.eclipse.configurer.internal.ValueRendererContext;

/**
 * {@link TextBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TextBuilderImpl<M> implements TextBuilder<M>, ValueRenderer<M> {

	/**
	 * Label.
	 */
	private final String label;

	/**
	 * {@link ObservableStringValue}.
	 */
	private final ReadOnlyStringWrapper value = new ReadOnlyStringWrapper();

	/**
	 * {@link ValueLoader}.
	 */
	private final ValueLoader<M, String> textLoader;

	/**
	 * {@link ValueRendererContext}.
	 */
	private ValueRendererContext<M> context;

	/**
	 * {@link Function} to obtain the initial value from the model.
	 */
	private Function<M, String> getInitialValue = null;

	/**
	 * {@link ValueValidator}.
	 */
	private ValueValidator<String> validator = null;

	/**
	 * Tracks if an error occurred on validation.
	 */
	private boolean isError = false;

	/**
	 * {@link ValueValidatorContext}.
	 */
	private final ValueValidatorContext<String> validatorContext = new ValueValidatorContext<String>() {

		@Override
		public String getValue() {
			return TextBuilderImpl.this.value.get();
		}

		@Override
		public void setError(String message) {
			TextBuilderImpl.this.isError = true;
			TextBuilderImpl.this.context.setError(message);
		}
	};

	/**
	 * Instantiate.
	 * 
	 * @param label
	 *            Label.
	 * @param textLoader
	 *            {@link ValueLoader}.
	 */
	public TextBuilderImpl(String label, ValueLoader<M, String> textLoader) {
		this.label = label;
		this.textLoader = textLoader;
	}

	/**
	 * Undertakes validation.
	 */
	private void validate() {
		// Reset to no error
		this.isError = false;
		try {
			// Undertake validation
			this.validator.validate(this.validatorContext);
		} catch (Exception ex) {
			// Flag error and notifty
			this.isError = true;
			this.context.setError(ex);
		}
		if (!this.isError) {
			// No error in validate, so clear error
			this.context.setError((String) null);
		}
	}

	/*
	 * ================ TextBuilder ===================
	 */

	@Override
	public TextBuilder<M> init(Function<M, String> getInitialValue) {
		this.getInitialValue = getInitialValue;
		return this;
	}

	@Override
	public TextBuilder<M> validate(ValueValidator<String> validator) {
		this.validator = validator;

		// Handle validation of changed value
		this.value.addListener((event) -> this.validate());

		// Return this
		return this;
	}

	@Override
	public ObservableValue<String> getValue() {
		return this.value.getReadOnlyProperty();
	}

	/*
	 * ================ ValueRenderer ==================
	 */

	@Override
	public void init(ValueRendererContext<M> context) {
		this.context = context;

		// Load the initial value
		if (this.getInitialValue != null) {
			String initialValue = this.getInitialValue.apply(this.context.getModel());
			this.value.set(initialValue);
		}
	}

	@Override
	public Node createLabel() {
		return new Label(this.label);
	}

	@Override
	public Node createInput() {
		TextField text = new TextField(this.value.get());
		this.value.bindBidirectional(text.textProperty());
		return text;
	}

	@Override
	public void loadValue(M model) {
		this.textLoader.loadValue(model, this.value.get());
	}

}