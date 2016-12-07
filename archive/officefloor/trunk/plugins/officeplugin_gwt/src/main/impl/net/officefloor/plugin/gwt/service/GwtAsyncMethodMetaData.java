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
package net.officefloor.plugin.gwt.service;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Meta-data for a GWT async method.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtAsyncMethodMetaData {

	/**
	 * Name of the {@link Method}.
	 */
	private final String methodName;

	/**
	 * Parameter type.
	 */
	private final Class<?> parameterType;

	/**
	 * Return type.
	 */
	private final Class<?> returnType;

	/**
	 * Error message.
	 */
	private final String error;

	/**
	 * Initiate by extracting GWT async details from the {@link Method}.
	 * 
	 * @param method
	 *            {@link Method}.
	 */
	public GwtAsyncMethodMetaData(Method method) {
		this.methodName = method.getName();

		// Obtain the parameters
		Class<?>[] parameters = method.getParameterTypes();
		Type[] generics = method.getGenericParameterTypes();

		// Determine the parameter type
		Class<?> paramType = null;
		Class<?> asyncCallback;
		Type asyncType;
		switch (parameters.length) {
		case 1:
			asyncCallback = parameters[0];
			asyncType = generics[0];
			break;
		case 2:
			paramType = parameters[0];
			asyncCallback = parameters[1];
			asyncType = generics[1];
			break;
		default:
			// Incorrect number of parmeters
			this.error = "Method signature must be void " + this.methodName
					+ "([X parameter,] AsyncCallback<Y> callback)";
			this.parameterType = null;
			this.returnType = null;
			return; // error
		}

		// Ensure async call back is actually an AsyncCallback
		if (!(AsyncCallback.class.equals(asyncCallback))) {
			this.error = "Last parameter must be "
					+ AsyncCallback.class.getSimpleName();
			this.parameterType = null;
			this.returnType = null;
			return; // error
		}

		// Ensure parameter is not a call back
		if (paramType != null) {
			if (AsyncCallback.class.isAssignableFrom(paramType)) {
				this.error = "May only have one "
						+ AsyncCallback.class.getSimpleName() + " parameter";
				this.parameterType = null;
				this.returnType = null;
				return; // error
			}
		}

		// Obtain AsyncCallback parameterized type
		Class<?> type = null;
		if (asyncType instanceof Class) {
			// Not parameterized
			type = Object.class;

		} else if (asyncType instanceof ParameterizedType) {
			// Parameterized type
			ParameterizedType parameterized = (ParameterizedType) asyncType;
			Type generic = parameterized.getActualTypeArguments()[0];

			// Determine generic type
			if (generic instanceof Class) {
				// Raw type
				type = (Class<?>) generic;

			} else if (generic instanceof ParameterizedType) {
				// Parameterized type
				Type rawType = ((ParameterizedType) generic).getRawType();
				if (rawType instanceof Class) {
					type = (Class<?>) rawType;
				}

			} else if (generic instanceof GenericArrayType) {
				// Array
				GenericArrayType array = (GenericArrayType) generic;
				Type genericComponentType = array.getGenericComponentType();
				Class<?> componentType = null;
				if (genericComponentType instanceof Class) {
					componentType = (Class<?>) genericComponentType;
				} else if (genericComponentType instanceof ParameterizedType) {
					Type rawType = ((ParameterizedType) genericComponentType)
							.getRawType();
					if (rawType instanceof Class) {
						componentType = (Class<?>) rawType;
					}
				}
				if (componentType != null) {
					type = Array.newInstance(componentType, 0).getClass();
				}
			}
		}

		// If unable to obtain type, must be wildcard
		if (type == null) {
			this.error = "Return type can not be a wildcard - "
					+ asyncType.toString();
			this.parameterType = null;
			this.returnType = null;
			return; // error
		}

		// Specify the details
		this.parameterType = paramType;
		this.returnType = type;
		this.error = null;
	}

	/**
	 * Obtains the {@link Method} name.
	 * 
	 * @return Name of the {@link Method}.
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	 * Obtains the parameter type.
	 * 
	 * @return Parameter type. May be <code>null</code> if no parameter.
	 */
	public Class<?> getParameterType() {
		return this.parameterType;
	}

	/**
	 * Obtains the required return type.
	 * 
	 * @return Required return type.
	 */
	public Class<?> getReturnType() {
		return this.returnType;
	}

	/**
	 * Obtains the error if not able to extract meta-data.
	 * 
	 * @return Error if not able to extract meta-data.
	 */
	public String getError() {
		return this.error;
	}

}