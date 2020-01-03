package net.officefloor.compile;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceFactory} configured and available to provide service.
 * 
 * @author Daniel Sagenschneider
 */
public class AvailableServiceFactory implements ServiceFactory<Object> {

	/**
	 * Service.
	 */
	private static final Object service = new Object();

	/**
	 * Obtains the service that will be created.
	 * 
	 * @return Service that will be created.
	 */
	public static Object getService() {
		return service;
	}

	/*
	 * ================= ServiceFactory =====================
	 */

	@Override
	public Object createService(ServiceContext context) throws Throwable {
		return service;
	}

}