package net.officefloor.tutorial.dynamodbhttpserver;

import net.officefloor.nosql.dynamodb.DynamoEntityLocator;

// START SNIPPET: tutorial
public class DynamoDbEntities implements DynamoEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { Post.class };
	}

}
// END SNIPPET: tutorial