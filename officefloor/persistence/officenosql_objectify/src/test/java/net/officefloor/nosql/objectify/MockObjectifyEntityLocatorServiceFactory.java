package net.officefloor.nosql.objectify;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ObjectifyEntityLocatorServiceFactory} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockObjectifyEntityLocatorServiceFactory
		implements ObjectifyEntityLocatorServiceFactory, ObjectifyEntityLocator {

	/*
	 * =================== ObjectifyEntityLocatorServiceFactory ===============
	 */

	@Override
	public ObjectifyEntityLocator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== ObjectifyEntityLocator ========================
	 */

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { ServiceRegisteredEntity.class };
	}

}