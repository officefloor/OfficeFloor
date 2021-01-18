package net.officefloor.nosql.dynamodb.test;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link DynamoDbExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbExtensionTest extends AbstractDynamoDbTestCase {

	@RegisterExtension
	public final DynamoDbExtension dynamo = new DynamoDbExtension();

	@UsesDockerTest
	public void test() throws Exception {
		this.doTest(this.dynamo.getAmazonDynamoDb());
	}

}