package net.officefloor.tutorial.cosmosdbhttpserver;

import com.azure.cosmos.CosmosDatabase;

import net.officefloor.nosql.cosmosdb.CosmosEntityLocator;

/**
 * {@link CosmosDatabase} entity instances.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class CosmosDbEntities implements CosmosEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { Post.class };
	}

}
// END SNIPPET: tutorial