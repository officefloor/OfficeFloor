package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Constructor;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;

/**
 * {@link MethodObjectInstanceFactory} using the default {@link Constructor}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultConstructorMethodObjectInstanceFactory implements MethodObjectInstanceFactory {

	/**
	 * Arguments for the default {@link Constructor}.
	 */
	private static final Object[] DEFAULT_CONSTRUCTION_ARGUMENTS = new Object[0];

	/**
	 * Default constructor for {@link Class}.
	 */
	private final Constructor<?> constructor;

	/**
	 * Instantiate.
	 * 
	 * @param clazz {@link Class} to instantiate via default {@link Constructor}.
	 * @throws Exception If fails to obtain default {@link Constructor}.
	 */
	public DefaultConstructorMethodObjectInstanceFactory(Class<?> clazz) throws Exception {
		this.constructor = clazz.getConstructor(new Class[0]);
	}

	/*
	 * =================== MethodObjectInstanceFactory =======================
	 */

	@Override
	public Object createInstance(ManagedFunctionContext<Indexed, Indexed> context) throws Exception {
		return this.constructor.newInstance(DEFAULT_CONSTRUCTION_ARGUMENTS);
	}

}