package net.officefloor.maven.test;

import net.officefloor.nosql.dynamodb.DynamoEntityLocator;

/**
 * {@link DynamoEntityLocator} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbEntities implements DynamoEntityLocator {

	/*
	 * ================= DynamoEntityLocator ===============
	 */

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { MessageEntity.class };
	}

}