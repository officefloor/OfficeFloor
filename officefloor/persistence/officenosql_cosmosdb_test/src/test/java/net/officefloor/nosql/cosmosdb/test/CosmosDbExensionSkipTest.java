package net.officefloor.nosql.cosmosdb.test;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.test.OfficeFloorExtension;
import net.officefloor.test.system.SystemPropertiesExtension;

/**
 * Ensure able to skip {@link RegisterExtension} failures. This is typically on
 * {@link OfficeFloorExtension} (or equivalent {@link Extension}) failing start
 * up of {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbExensionSkipTest {

	/**
	 * Flag to skip {@link CosmosDbExtension} failures.
	 */
	@Order(1)
	public @RegisterExtension final SystemPropertiesExtension skip = new SystemPropertiesExtension(
			CosmosDbExtension.PROPERTY_SKIP_FAILED_COSMOS, "true");

	/**
	 * Should catch and skip
	 */
	@Order(2)
	public @RegisterExtension final CosmosDbExtension cosmos = new CosmosDbExtension();

	/**
	 * Cause failure.
	 */
	@Order(3)
	public @RegisterExtension final BeforeEachCallback failBefore = new BeforeEachCallback() {

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			throw new Exception("Failing test to ensure skipped");
		}
	};

	/**
	 * Should not be invoked as testing {@link Extension} handling.
	 */
	@Test
	public void skippedTest() {
		fail("Should not be invoked");
	}

}