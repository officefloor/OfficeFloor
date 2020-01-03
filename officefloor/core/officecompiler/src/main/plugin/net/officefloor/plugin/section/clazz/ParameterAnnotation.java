package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Method;

/**
 * {@link Parameter} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterAnnotation {

	/**
	 * Parameter type.
	 */
	private final Class<?> parameterType;

	/**
	 * Index within the {@link Method} for the parameter.
	 */
	private final int parameterIndex;

	/**
	 * Instantiate.
	 * 
	 * @param parameterType  Parameter type.
	 * @param parameterIndex Index within the {@link Method} for the parameter.
	 */
	public ParameterAnnotation(Class<?> parameterType, int parameterIndex) {
		this.parameterType = parameterType;
		this.parameterIndex = parameterIndex;
	}

	/**
	 * Obtains the parameter type.
	 * 
	 * @return Parameter type.
	 */
	public Class<?> getParameterType() {
		return parameterType;
	}

	/**
	 * Obtains index within the {@link Method} for the parameter.
	 * 
	 * @return Index within the {@link Method} for the parameter.
	 */
	public int getParameterIndex() {
		return parameterIndex;
	}

}