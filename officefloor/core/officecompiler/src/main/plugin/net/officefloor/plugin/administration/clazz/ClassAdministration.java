package net.officefloor.plugin.administration.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.Indexed;

/**
 * {@link Administration} that delegates to {@link Method} instances of an
 * {@link Object} to do administration.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministration
		implements AdministrationFactory<Object, Indexed, Indexed>, Administration<Object, Indexed, Indexed> {

	/**
	 * Default constructor arguments.
	 */
	private static final Object[] DEFAULT_CONSTRUCTOR_ARGS = new Object[0];

	/**
	 * {@link Constructor} for the {@link Object} providing the administration
	 * {@link Method}.
	 */
	private final Constructor<?> constructor;

	/**
	 * {@link Method} to invoke on the {@link Object} for this
	 * {@link Administration}.
	 */
	private final Method administrationMethod;

	/**
	 * {@link AdministrationParameterFactory} instances.
	 */
	private final AdministrationParameterFactory[] parameterFactories;

	/**
	 * Initiate.
	 * 
	 * @param constructor
	 *            {@link Constructor} for the {@link Object} providing the
	 *            administration {@link Method}.
	 * @param administrationMethod
	 *            {@link Method} to invoke on the {@link Object} for this
	 *            {@link Administration}.
	 * @param parameterFactories
	 *            {@link AdministrationParameterFactory} instances.
	 */
	public ClassAdministration(Constructor<?> constructor, Method administrationMethod,
			AdministrationParameterFactory[] parameterFactories) {
		this.constructor = constructor;
		this.administrationMethod = administrationMethod;
		this.parameterFactories = parameterFactories;
	}

	/*
	 * ============== AdministrationFactory ===============================
	 */

	@Override
	public Administration<Object, Indexed, Indexed> createAdministration() throws Throwable {
		return this;
	}

	/*
	 * ================== Administration ==================================
	 */

	@Override
	public void administer(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {

		// Create the parameters
		Object[] parameters = new Object[this.parameterFactories.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.parameterFactories[i].createParameter(context);
		}

		try {
			// Obtain the object
			Object object = (constructor != null ? constructor.newInstance(DEFAULT_CONSTRUCTOR_ARGS) : null);

			// Invoke the method to administer
			this.administrationMethod.invoke(object, parameters);

		} catch (InvocationTargetException ex) {
			// Propagate cause
			throw ex.getCause();
		}
	}

}