package net.officefloor.nosql.dynamodb.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link Extension} for DynamoDb.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbExtension extends AbstractDynamoDbJunit
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates if run DynamoDb for each test.
	 */
	private boolean isEach = true;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public DynamoDbExtension() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public DynamoDbExtension(Configuration configuration) {
		super(configuration);
	}

	/*
	 * ================== Extension ===================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Start
		this.startAmazonDynamoDb();

		// Shutdown after all tests
		this.isEach = false;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Determine if start for each
		if (this.isEach) {
			this.startAmazonDynamoDb();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Stop if for each
		if (this.isEach) {
			this.stopAmazonDynamoDb();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Stop if after all
		if (!this.isEach) {
			this.stopAmazonDynamoDb();
		}
	}

}