package net.officefloor.frame.api.source;

import java.util.ServiceLoader;

/**
 * Generic factory to be loaded by the {@link ServiceLoader}. This will be
 * provided the {@link ServiceContext} to create the specific service.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServiceFactory<S> {

	/**
	 * Creates the service.
	 * 
	 * @param context
	 *            {@link ServiceContext}.
	 * @return Service.
	 * @throws Throwable
	 *             If fails to create the service.
	 */
	S createService(ServiceContext context) throws Throwable;

}