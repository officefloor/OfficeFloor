package net.officefloor.tutorial.cosmosasyncdbhttpserver;

import net.officefloor.nosql.cosmosdb.CosmosEntityLocator;

// START SNIPPET: tutorial
public class CosmosAsyncDbEntities implements CosmosEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { Post.class };
	}

}
// END SNIPPET: tutorial