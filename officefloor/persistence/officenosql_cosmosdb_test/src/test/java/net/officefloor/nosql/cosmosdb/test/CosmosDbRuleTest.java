package net.officefloor.nosql.cosmosdb.test;

import org.junit.ClassRule;
import org.junit.Test;

import net.officefloor.test.skip.SkipJUnit4;

/**
 * Tests the {@link CosmosDbRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbRuleTest extends AbstractCosmosDbTestCase {

	public static final @ClassRule CosmosDbRule cosmos = new CosmosDbRule();

	@Test
	public void synchronous() {
		SkipJUnit4.skipDocker();
		this.doSynchronousTest(cosmos.getCosmosClient());
	}

	@Test
	public void asynchronous() {
		SkipJUnit4.skipDocker();
		this.doAsynchronousTest(cosmos.getCosmosAsyncClient());
	}
}
