/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.dialog.input;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Handle for the {@link Input}.
 * 
 * @author Daniel
 */
public class InputHandler<T> implements InputContext {

	/**
	 * {@link Input} being handled.
	 */
	@SuppressWarnings("unchecked")
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
		this(parent, input, null, null);
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
		this(parent, input, null, null);
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
	 * ===========================================================
	 * PropertyInputContext
	 * ===========================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.input.InputContext#getParent()
	 */
	@Override
	public Composite getParent() {
		return this.parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.input.InputContext#getInitialValue()
	 */
	@Override
	public Object getInitialValue() {
		return this.initialValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.input.InputContext#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.input.InputContext#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public void setAttribute(String name, Object value) {
		this.setAttribute(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.input.InputContext#notifyValueChanged(java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.input.InputContext#notifyValueInvalid(java.lang.String)
	 */
	@Override
	public void notifyValueInvalid(String message) {
		// Notify listener
		if (this.listener != null) {
			this.listener.notifyValueInvalid(message);
		}
	}

}
