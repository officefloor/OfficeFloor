package net.officefloor.test;

import net.officefloor.frame.api.manage.UnknownObjectException;

/**
 * Service providing additional test dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public interface TestDependencyService {

	/**
	 * Indicates if able to provide object.
	 * 
	 * @param context {@link TestDependencyServiceContext}.
	 * @return <code>true</code> if able to provide object.
	 */
	boolean isObjectAvailable(TestDependencyServiceContext context);

	/**
	 * Obtains the dependency object.
	 * 
	 * @param context {@link TestDependencyServiceContext}.
	 * @return Object.
	 * @throws UnknownObjectException If unknown bound object name.
	 * @throws Throwable              If failure in obtaining the bound object.
	 */
	Object getObject(TestDependencyServiceContext context) throws UnknownObjectException, Throwable;

}