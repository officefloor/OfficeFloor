package net.officefloor.compile;

import org.junit.Assert;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * Missing {@link ServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MissingServiceFactory implements ServiceFactory<Object> {

	/**
	 * Obtains the issue message.
	 * 
	 * @return Issue message.
	 */
	public static String getIssueDescription() {
		return "No services configured for " + MissingServiceFactory.class.getName();
	}

	/*
	 * =============== ServiceFactory =====================
	 */

	@Override
	public Throwable createService(ServiceContext context) throws Throwable {
		Assert.fail("Should not be creating service");
		return null;
	}

}