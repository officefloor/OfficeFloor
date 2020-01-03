package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceFactory} triggering failures.
 * 
 * @author Daniel Sagenschneider
 */
public class FailServiceFactory implements ServiceFactory<Class<FailServiceFactory>> {

	/**
	 * Failure to instantiate this.
	 */
	public static Throwable instantiateFailure = null;

	/**
	 * Failure to create the service.
	 */
	public static Throwable createServiceFailure = null;

	/**
	 * Resets for next test.
	 */
	public static void reset() {
		instantiateFailure = null;
		createServiceFailure = null;
	}

	/**
	 * Instantiate (with possible failure).
	 */
	public FailServiceFactory() throws Throwable {
		if (instantiateFailure != null) {
			throw instantiateFailure;
		}
	}

	/*
	 * ================= ServiceFactory ===================
	 */

	@Override
	public Class<FailServiceFactory> createService(ServiceContext context) throws Throwable {

		// Throw possible create error
		if (createServiceFailure != null) {
			throw createServiceFailure;
		}

		// Return successfully
		return FailServiceFactory.class;
	}

}