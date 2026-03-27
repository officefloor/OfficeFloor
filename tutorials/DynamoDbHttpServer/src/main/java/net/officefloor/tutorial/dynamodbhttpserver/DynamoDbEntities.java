package net.officefloor.tutorial.dynamodbhttpserver;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import net.officefloor.nosql.dynamodb.DynamoEntityLocator;

/**
 * {@link DynamoDBMapper} entity instances.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class DynamoDbEntities implements DynamoEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { Post.class };
	}

}
// END SNIPPET: tutorial