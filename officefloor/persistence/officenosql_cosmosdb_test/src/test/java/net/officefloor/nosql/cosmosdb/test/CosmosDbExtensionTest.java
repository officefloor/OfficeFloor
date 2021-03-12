package net.officefloor.nosql.cosmosdb.test;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link CosmosDbExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbExtensionTest extends AbstractCosmosDbTestCase {

	public final @RegisterExtension CosmosDbExtension cosmos = new CosmosDbExtension();

	@UsesDockerTest
	public void synchronous() {
		this.doSynchronousTest(this.cosmos.getCosmosClient());
	}

	@UsesDockerTest
	public void asynchronous() {
		this.doAsynchronousTest(this.cosmos.getCosmosAsyncClient());
	}

}