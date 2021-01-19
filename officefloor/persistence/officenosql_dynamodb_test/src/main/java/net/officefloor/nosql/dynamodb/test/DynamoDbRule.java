package net.officefloor.nosql.dynamodb.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

/**
 * {@link AmazonDynamoDB} {@link TestRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbRule extends AbstractDynamoDbJunit implements TestRule {

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public DynamoDbRule() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public DynamoDbRule(Configuration configuration) {
		super(configuration);
	}

	/*
	 * ====================== TestRule ==========================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Start DynamoDb
				DynamoDbRule.this.startAmazonDynamoDb();
				try {

					// Run the test
					base.evaluate();

				} finally {
					// Stop DynamoDb
					DynamoDbRule.this.stopAmazonDynamoDb();
				}
			}
		};
	}

}
