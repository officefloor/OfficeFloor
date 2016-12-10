/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.dialog.input;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Handle for the {@link Input}.
 * 
 * @author Daniel Sagenschneider
 */
public class InputHandler<T> implements InputContext {

	/**
	 * {@link Input} being handled.
	 */
	@SuppressWarnings("rawtypes")
	private final Input input;

	/**
	 * Parent {@link Composite}.
	 */
	private final Composite parent;

	/**
	 * Initial value.
	 */
	private final Object initialValue;

	/**
	 * {@link ValueTranslator}.
	 */
	private final ValueTranslator translator;

	/**
	 * Attributes.
	 */
	private final Map<String, Object> attributes = new HashMap<String, Object>(
			1);

	/**
	 * {@link Control}.
	 */
	private Control control;

	/**
	 * {@link InputListener}.
	 */
	private InputListener listener = null;

	/**
	 * Convenience constructor.
	 * 
	 * @param parent
	 *            Parent {@link Composite}.
	 * @param input
	 *            {@link Input} being handled.
	 */
	public InputHandler(Composite parent, Input<? extends Control> input) {
		this(parent, input, null);
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param parent
	 *            Parent {@link Composite}.
	 * @param input
	 *            {@link Input} being handled.
	 * @param initialValue
	 *            Initial value.
	 */
	public InputHandler(Composite parent, Input<? extends Control> input,
			Object initialValue) {
		this(parent, input, initialValue, (ValueTranslator) null);
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param parent
	 *            Parent {@link Composite}.
	 * @param input
	 *            {@link Input} being handled.
	 * @param listener
	 *            {@link InputListener}.
	 */
	public InputHandler(Composite parent, Input<? extends Control> input,
			InputListener listener) {
		this(parent, input, null, listener);
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param parent
	 *            Parent {@link Composite}.
	 * @param input
	 *            {@link Input} being handled.
	 * @param initialValue
	 *            Initial value.
	 * @param listener
	 *            {@link InputListener}.
	 */
	public InputHandler(Composite parent, Input<? extends Control> input,
			Object initialValue, InputListener listener) {
		this(parent, input, initialValue, (ValueTranslator) null);
		this.listener = listener;
	}

	/**
	 * Initiate.
	 * 
	 * @param parent
	 *            Parent {@link Composite}.
	 * @param input
	 *            {@link Input} being handled.
	 * @param initialValue
	 *            Initial value.
	 * @param translator
	 *            {@link ValueTranslator}.
	 */
	public InputHandler(Composite parent, Input<? extends Control> input,
			Object initialValue, ValueTranslator translator) {
		this.input = input;
		this.parent = parent;
		this.initialValue = initialValue;
		this.translator = translator;

		// Build the control
		this.control = this.input.buildControl(this);
	}

	/**
	 * Obtains the {@link Control}.
	 * 
	 * @return {@link Control}.
	 */
	public Control getControl() {
		return this.control;
	}

	/**
	 * Obtains the {@link Input}.
	 * 
	 * @return {@link Input}.
	 */
	@SuppressWarnings("unchecked")
	public Input<? extends Control> getInput() {
		return this.input;
	}

	/**
	 * Obtains the value.
	 * 
	 * @return Value.
	 * @throws InvalidValueException
	 *             If value is invalid.
	 */
	@SuppressWarnings("unchecked")
	public T getValue() throws InvalidValueException {

		// Obtain the value from the control
		Object value = this.input.getValue(this.control, this);

		// Determine if must translate the value
		if (this.translator != null) {
			value = this.translator.translate(value);
		}

		// Return the value
		return (T) value;
	}

	/**
	 * Obtains the value defaulting to <code>null</code> if failure obtaining.
	 * 
	 * @return Value or <code>null</code>.
	 */
	public T getTrySafeValue() {
		try {
			return this.getValue();
		} catch (InvalidValueException ex) {
			return null;
		}
	}

	/**
	 * Specifies the {@link InputListener}.
	 * 
	 * @param listener
	 *            {@link InputListener}.
	 */
	public void setInputListener(InputListener listener) {
		this.listener = listener;
	}

	/*
	 * ================ InputContext =================================
	 */

	@Override
	public Composite getParent() {
		return this.parent;
	}

	@Override
	public Object getInitialValue() {
		return this.initialValue;
	}

	@Override
	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		this.setAttribute(name, value);
	}

	@Override
	public void notifyValueChanged(Object value) {
		try {

			// Translate value if necessary
			if (this.translator != null) {
				value = this.translator.translate(value);
			}

			// Notify the value changed
			if (this.listener != null) {
				this.listener.notifyValueChanged(value);
			}

		} catch (InvalidValueException ex) {
			// Notify invalid value
			this.notifyValueInvalid(ex.getMessage());
		} catch (RuntimeException ex) {
			// Notify translate value
			this.notifyValueInvalid(ex.getMessage() + " ["
					+ ex.getClass().getSimpleName() + "]");
		}
	}

	@Override
	public void notifyValueInvalid(String message) {
		// Notify listener
		if (this.listener != null) {
			this.listener.notifyValueInvalid(message);
		}
	}

}