package net.officefloor.nosql.dynamodb.test;

import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the {@link DynamoDbRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbRuleTest extends AbstractDynamoDbTestCase {

	@Rule
	public final DynamoDbRule dynamo = new DynamoDbRule();

	@Test
	public void test() throws Exception {
		this.doTest(this.dynamo.getDynamoDb());
	}

}