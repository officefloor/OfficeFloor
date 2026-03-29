package net.officefloor.test;

import net.officefloor.frame.api.manage.UnknownObjectException;

/**
 * {@link TestDependencyService} providing an {@link Object} instance.
 */
public class ObjectTestDependencyService<T, O extends T> implements TestDependencyService {

	/**
	 * Type for dependency.
	 */
	private final Class<T> type;

	/**
	 * Object for dependency.
	 */
	private final Object object;

	/**
	 * Instantiate using {@link Object} type.
	 * 
	 * @param object {@link Object}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ObjectTestDependencyService(O object) {
		this((Class) object.getClass(), object);
	}

	/**
	 * Instantiate specifying more generic {@link Object} type.
	 * 
	 * @param type   Type for dependency.
	 * @param object Object as the dependency.
	 */
	public ObjectTestDependencyService(Class<T> type, O object) {
		this.type = type;
		this.object = object;
	}

	/*
	 * ================= TestDependencyService ==================
	 */

	@Override
	public boolean isObjectAvailable(TestDependencyServiceContext context) {
		return context.getObjectType().isAssignableFrom(this.type);
	}

	@Override
	public Object getObject(TestDependencyServiceContext context) throws UnknownObjectException, Throwable {
		return this.object;
	}

}