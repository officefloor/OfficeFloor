package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;

/**
 * {@link ManagedFunctionFactory} for the {@link MethodFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodFunctionFactory implements ManagedFunctionFactory<Indexed, Indexed> {

	/**
	 * {@link MethodObjectInstanceFactory}. Will be <code>null</code> if static
	 * {@link Method}.
	 */
	private final MethodObjectInstanceFactory methodObjectInstanceFactory;

	/**
	 * Method to invoke for this {@link ManagedFunction}.
	 */
	private final Method method;

	/**
	 * Parameters.
	 */
	private final MethodParameterFactory[] parameters;

	/**
	 * {@link MethodReturnTranslator} or <code>null</code>.
	 */
	private MethodReturnTranslator<Object, Object> returnTranslator;

	/**
	 * Initiate.
	 * 
	 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
	 *                                    be <code>null</code> if static
	 *                                    {@link Method}.
	 * @param method                      {@link Method} to invoke for the
	 *                                    {@link ManagedFunction}.
	 * @param parameters                  {@link MethodParameterFactory} instances.
	 */
	public MethodFunctionFactory(MethodObjectInstanceFactory methodObjectInstanceFactory, Method method,
			MethodParameterFactory[] parameters) {
		this.methodObjectInstanceFactory = methodObjectInstanceFactory;
		this.method = method;
		this.parameters = parameters;
	}

	/**
	 * Specifies the {@link MethodReturnTranslator}.
	 * 
	 * @param returnTranslator {@link MethodReturnTranslator}.
	 */
	public void setMethodReturnTranslator(MethodReturnTranslator<Object, Object> returnTranslator) {
		this.returnTranslator = returnTranslator;
	}

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @return {@link Method}.
	 */
	public Method getMethod() {
		return this.method;
	}

	/*
	 * =============== ManagedFunctionFactory ===============
	 */

	@Override
	public MethodFunction createManagedFunction() {
		return new MethodFunction(this.methodObjectInstanceFactory, this.method, this.parameters,
				this.returnTranslator);
	}

}