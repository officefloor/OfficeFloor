package net.officefloor.nosql.objectify;

/**
 * Configured {@link ObjectifyEntityLocator} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockObjectifyEntityLocator implements ObjectifyEntityLocator {

	/*
	 * ================ ObjectifyEntityLocator ===================
	 */

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { LocatedEntity.class };
	}

}