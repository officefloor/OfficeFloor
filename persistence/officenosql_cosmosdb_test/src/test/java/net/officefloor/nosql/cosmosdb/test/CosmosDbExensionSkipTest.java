package net.officefloor.nosql.cosmosdb.test;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;
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
	 * Indicates if test is run.
	 */
	private boolean isTestRun = false;

	/**
	 * Should not be invoked as testing {@link Extension} handling.
	 */
	@Test
	public void skippedTest() throws Exception {
		this.isTestRun = true;
		throw new Exception("Failing test to ensure skipped");
	}

	@AfterEach
	public void confirmExecuted() {
		assertTrue("Test should be run", this.isTestRun);
	}

}