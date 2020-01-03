package net.officefloor.compile;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceFactory} that fails to create the service.
 * 
 * @author Daniel Sagenschneider
 */
public class FailServiceFactory implements ServiceFactory<Throwable> {

	/**
	 * Failure.
	 */
	private static final Throwable failure = new Throwable("TEST");

	/**
	 * Obtains the error issue description.
	 * 
	 * @return Error issue description.
	 */
	public static String getIssueDescription() {
		return "Failed to create service from " + FailServiceFactory.class.getName();
	}

	/**
	 * Obtains the failure thrown on creating the service.
	 * 
	 * @return Failure thrown on creating the service.
	 */
	public static Throwable getCause() {
		return failure;
	}

	/*
	 * =================== ServiceFactory ==================
	 */

	@Override
	public Throwable createService(ServiceContext context) throws Throwable {
		throw failure;
	}

}