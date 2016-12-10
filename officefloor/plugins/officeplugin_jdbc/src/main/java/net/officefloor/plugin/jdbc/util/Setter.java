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
package net.officefloor.plugin.jdbc.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Setter {@link Method} wrapper.
 * 
 * @author Daniel Sagenschneider
 */
public class Setter<B> {

	/**
	 * Class containing this.
	 */
	private final Class<B> clazz;

	/**
	 * Setter method.
	 */
	private final Method method;

	/**
	 * Initiate.
	 * 
	 * @param clazz
	 *            Class for the {@link Method}.
	 * @param method
	 *            Setter method.
	 */
	public Setter(Class<B> clazz, Method method) {
		this.clazz = clazz;
		this.method = method;
	}

	/**
	 * Obtains the property name for this setter.
	 * 
	 * @return Property name for this setter.
	 */
	public String getPropertyName() {
		String propertyName = this.method.getName();
		propertyName = propertyName.substring("set".length());
		propertyName = propertyName.substring(0, 1).toLowerCase()
				+ propertyName.substring(1);
		return propertyName;
	}

	/**
	 * Sets the value onto the bean.
	 * 
	 * @param bean
	 *            Bean to have the value loaded.
	 * @param value
	 *            Value to set on the bean.
	 * @throws Exception
	 *             If fails to set the value.
	 */
	public void setValue(B bean, Object value) throws Exception {

		// Obtain the method to load
		Method setMethod;
		if (bean.getClass() == this.clazz) {
			setMethod = this.method;
		} else {
			// Obtain method from sub type
			setMethod = bean.getClass().getMethod(this.method.getName(),
					this.method.getParameterTypes());
		}

		try {
			// Set the value onto the object
			setMethod.invoke(bean, value);
		} catch (InvocationTargetException ex) {
			// Throw bean failure
			Throwable cause = ex.getCause();
			if (cause instanceof Exception) {
				throw (Exception) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				// Can not throw, so indicate via invocation failure
				throw ex;
			}
		}
	}

	/**
	 * Sets the property value onto the bean.
	 * 
	 * @param bean
	 *            Bean to have the value loaded.
	 * @param value
	 *            Value to set on the bean.
	 * @throws Exception
	 *             If fails to set property value.
	 */
	public void setPropertyValue(B bean, String value) throws Exception {

		// Transform the value to set on bean
		Object loadValue;
		Class<?> parameterType = this.method.getParameterTypes()[0];
		if (String.class.isAssignableFrom(parameterType)) {
			loadValue = value;
		} else if (Integer.class.isAssignableFrom(parameterType)
				|| int.class.isAssignableFrom(parameterType)) {
			loadValue = Integer.valueOf(value);
		} else if (Boolean.class.isAssignableFrom(parameterType)
				|| boolean.class.isAssignableFrom(parameterType)) {
			loadValue = Boolean.valueOf(value);
		} else {
			// Unknown property type, so can not load
			throw new IllegalArgumentException("Unknown property value type "
					+ parameterType.getName());
		}

		// Set the property value on the bean
		this.setValue(bean, loadValue);
	}

}
