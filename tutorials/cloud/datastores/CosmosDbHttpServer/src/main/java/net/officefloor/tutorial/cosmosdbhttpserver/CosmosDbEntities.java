package net.officefloor.tutorial.cosmosdbhttpserver;

import net.officefloor.nosql.cosmosdb.CosmosEntityLocator;

// START SNIPPET: tutorial
public class CosmosDbEntities implements CosmosEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { Post.class };
	}

}
// END SNIPPET: tutorial