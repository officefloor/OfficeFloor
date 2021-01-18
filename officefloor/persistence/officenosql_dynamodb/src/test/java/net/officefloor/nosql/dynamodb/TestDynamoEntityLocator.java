package net.officefloor.nosql.dynamodb;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link DynamoEntityLocator} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class TestDynamoEntityLocator implements DynamoEntityLocatorServiceFactory, DynamoEntityLocator {

	@Override
	public DynamoEntityLocator createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { TestEntity.class };
	}

}