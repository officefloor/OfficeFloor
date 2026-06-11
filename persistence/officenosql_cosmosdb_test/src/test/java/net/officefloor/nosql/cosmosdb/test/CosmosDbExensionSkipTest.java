/*-
 * #%L
 * CosmosDB Persistence Testing
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
