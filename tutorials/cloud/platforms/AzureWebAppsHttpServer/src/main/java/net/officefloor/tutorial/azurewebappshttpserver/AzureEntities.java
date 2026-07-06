package net.officefloor.tutorial.azurewebappshttpserver;

import net.officefloor.nosql.cosmosdb.CosmosEntityLocator;

// START SNIPPET: tutorial
public class AzureEntities implements CosmosEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { Post.class };
	}

}
// END SNIPPET: tutorial