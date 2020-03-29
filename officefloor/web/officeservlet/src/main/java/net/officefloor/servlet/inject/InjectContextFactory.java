package net.officefloor.servlet.inject;

import java.lang.reflect.Field;
import java.util.Map;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;

/**
 * Factory for the {@link InjectContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class InjectContextFactory {

	/**
	 * Listing of {@link SupplierThreadLocal} instances with indexes corresponding
	 * to dependency index.
	 */
	private final SupplierThreadLocal<?>[] supplierThreadLocals;

	/**
	 * {@link Map} of {@link Class} to its respective {@link InjectField} instances.
	 */
	private final Map<Class<?>, InjectField[]> classToFields;

	/**
	 * Instantiate.
	 * 
	 * @param supplierThreadLocals Listing of {@link SupplierThreadLocal} instances
	 *                             with indexes corresponding to dependency index.
	 * @param classToFields        {@link Map} of {@link Class} to its respective
	 *                             {@link InjectField} instances.
	 */
	InjectContextFactory(SupplierThreadLocal<?>[] supplierThreadLocals, Map<Class<?>, InjectField[]> classToFields) {
		this.supplierThreadLocals = supplierThreadLocals;
		this.classToFields = classToFields;
	}

	/**
	 * Creates the {@link InjectContext}.
	 * 
	 * @return {@link InjectContext}.
	 */
	public InjectContext createInjectContext() {
		return new InjectContext(this.supplierThreadLocals);
	}

	/**
	 * Injects the dependencies onto the {@link Object}.
	 * 
	 * @param object {@link Object} to receive dependencies.
	 * @return Input {@link Object} to allow easy factory creation.
	 * @throws IllegalArgumentException If failure to inject.
	 * @throws IllegalAccessException   If failure to inject.
	 */
	public <T> T injectDependencies(T object) throws IllegalArgumentException, IllegalAccessException {

		// Obtain the fields
		InjectField[] injections = this.classToFields.get(object.getClass());
		if (injections == null) {
			return object; // nothing registered to inject
		}

		// Load the dependencies
		for (InjectField injection : injections) {
			Field field = injection.field;
			field.setAccessible(true);
			field.set(object, injection.dependency);
		}

		// Return the object
		return object;
	}

}