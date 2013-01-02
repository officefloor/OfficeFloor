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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.util.LogUtil;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Handler for the {@link Input} that populates a property on a bean.
 *
 * @author Daniel Sagenschneider
 */
public class PropertyInputHandler {

	/**
	 * Bean to be populated.
	 */
	private final Object bean;

	/**
	 * Mutator {@link Method} on the bean.
	 */
	private final Method mutator;

	/**
	 * Delegate {@link InputHandler}.
	 */
	private final InputHandler<?> delegateInputHandler;

	/**
	 * Initialise.
	 *
	 * @param parent
	 *            Parent {@link Composite}.
	 * @param input
	 *            {@link Input}.
	 * @param propertyName
	 *            Name of the property.
	 * @param bean
	 *            Bean to be populated.
	 * @param valueTranslatorRegistry
	 *            {@link ValueTranslatorRegistry}.
	 * @param translator
	 *            {@link ValueTranslator} to use or <code>null</code> to source
	 *            from {@link ValueTranslatorRegistry}.
	 */
	public PropertyInputHandler(Composite parent,
			Input<? extends Control> input, String propertyName, Object bean,
			ValueTranslatorRegistry valueTranslatorRegistry,
			ValueTranslator translator) {
		this.bean = bean;

		// Transform property name to use as method name
		String methodSuffix = propertyName.substring(0, 1).toUpperCase()
				+ propertyName.substring(1);
		methodSuffix = methodSuffix.replace(" ", "");

		// Obtain the set and get method names
		String setMethodName = "set" + methodSuffix;
		String getMethodName = "get" + methodSuffix;

		// Obtain the mutator and (possibly accessor)
		List<Method> setMethods = new LinkedList<Method>();
		Method getMethod = null;
		for (Method method : this.bean.getClass().getMethods()) {

			// Ignore non-public methods
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}

			// Determine if appropriate mutator
			if ((Void.TYPE.equals(method.getReturnType()))
					&& (setMethodName.equals(method.getName()))
					&& (method.getParameterTypes().length == 1)) {
				setMethods.add(method);
			}

			// Determine if the accessor
			if ((!Void.TYPE.equals(method.getReturnType()))
					&& (getMethodName.equals(method.getName()))
					&& (method.getParameterTypes().length == 0)) {
				getMethod = method;
			}
		}

		// Obtain the accessor details
		Class<?> propertyType = null;
		Object initialValue;
		if (getMethod == null) {
			// No initial value
			initialValue = null;
		} else {
			// Obtain the type of the property
			propertyType = getMethod.getReturnType();
			try {
				// Obtain the initial value
				initialValue = getMethod.invoke(this.bean, new Object[0]);
			} catch (Exception ex) {
				// Failed to get initial value
				initialValue = null;
			}
		}

		// Obtain the set method details
		Method setMethod = null;
		if (setMethods.size() == 0) {
			// Indicate must have mutator for property
			LogUtil.logError("No mutator for property '" + propertyName
					+ "' on bean " + this.bean.getClass().getName());
		} else if (setMethods.size() == 1) {
			// Only mutator so use
			setMethod = setMethods.get(0);
		} else {
			// Select appropriate mutator (match accessor)
			if (propertyType != null) {
				for (Method method : setMethods) {
					if (propertyType.equals(method.getParameterTypes()[0])) {
						setMethod = method;
					}
				}
			}

			// No match by accessor then try for String mutator
			if (setMethod == null) {
				for (Method method : setMethods) {
					if (String.class.equals(method.getParameterTypes()[0])) {
						setMethod = method;
					}
				}
			}
		}

		// Ensure have the mutator
		if (setMethod != null) {
			// Ensure have the property type
			if (propertyType == null) {
				propertyType = setMethod.getParameterTypes()[0];
			}
		} else {
			// Must have mutator
			LogUtil.logError("Can not determine mutator for property '"
					+ propertyName + "' from bean "
					+ this.bean.getClass().getName());
		}
		this.mutator = setMethod;

		// Ensure have a translator
		if (translator == null) {
			translator = valueTranslatorRegistry
					.getValueTranslator(propertyType);
		}
		if (translator == null) {
			// Must have translator
			LogUtil.logError("Can not obtain "
					+ ValueTranslator.class.getSimpleName() + " for property '"
					+ propertyName + "' of type " + propertyType.getName()
					+ " for bean " + this.bean.getClass().getName());
		}

		// Create the delegate input handler
		this.delegateInputHandler = new InputHandler<Object>(parent, input,
				initialValue, translator);
	}

	/**
	 * Obtains the {@link Control}.
	 *
	 * @return {@link Control}.
	 */
	public Control getControl() {
		return this.delegateInputHandler.getControl();
	}

	/**
	 * Specifies the {@link InputListener}.
	 *
	 * @param listener
	 *            {@link InputListener}.
	 */
	public void setInputListener(InputListener listener) {
		this.delegateInputHandler.setInputListener(listener);
	}

	/**
	 * Populates the value on the property of the bean.
	 *
	 * @return <code>true</code> if non-null value populated (or if
	 *         {@link String} that is not empty).
	 * @throws InvalidValueException
	 *             If invalid value or failure populating the property value.
	 */
	public boolean populatePropertyOnBean() throws InvalidValueException {

		// Obtain the value
		Object value = this.delegateInputHandler.getValue();

		// Populate value onto the bean
		try {
			this.mutator.invoke(this.bean, new Object[] { value });
		} catch (InvocationTargetException ex) {
			// Obtain the cause
			Throwable cause = ex.getCause();
			throw new InvalidValueException(cause.getMessage() + " ["
					+ cause.getClass().getSimpleName() + "]");
		} catch (Exception ex) {
			// Indicate failure to specify value
			throw new InvalidValueException(ex.getMessage() + " ["
					+ ex.getClass().getSimpleName() + "]");
		}

		// Check for empty string
		if (value instanceof String) {
			String text = (String) value;
			if (text.trim().length() == 0) {
				return false;
			}
		}

		// Return whether a value was populated
		return (value != null);
	}

}
