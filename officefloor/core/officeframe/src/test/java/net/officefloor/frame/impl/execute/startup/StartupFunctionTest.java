/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.frame.impl.execute.startup;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure startup {@link ManagedFunction} instances are invoked.
 *
 * @author Daniel Sagenschneider
 */
public class StartupFunctionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure invoke startup function.
	 */
	public void testStartupFunction() throws Exception {

		// Construct the function
		TestWork work = new TestWork();
		this.constructFunction(work, "startup").buildParameter();

		// Construct startup
		MockParameter parameter = new MockParameter();
		this.getOfficeBuilder().addStartupFunction("startup", parameter);

		// Open the office
		this.constructOfficeFloor().openOfficeFloor();

		// Ensure the startup function is invoked
		assertSame("Should have invoked startup function", parameter, work.parameter);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public MockParameter parameter = null;

		public void startup(MockParameter parameter) {
			this.parameter = parameter;
		}
	}

	private class MockParameter {
	}

}
