package net.officefloor.nosql.cosmosdb.test;

import org.junit.Rule;
import org.junit.jupiter.api.Test;

import net.officefloor.test.skip.SkipJUnit4;

/**
 * Tests the {@link CosmosDbRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbRuleTest extends AbstractCosmosDbTestCase {

	public final @Rule CosmosDbRule cosmos = new CosmosDbRule();

	@Test
	public void synchronous() {
		SkipJUnit4.skipDocker();
		this.doSynchronousTest(this.cosmos.getCosmosClient());
	}
}
