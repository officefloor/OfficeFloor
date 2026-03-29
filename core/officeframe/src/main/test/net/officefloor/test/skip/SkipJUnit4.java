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

package net.officefloor.test.skip;

import static org.junit.Assume.assumeFalse;

import net.officefloor.test.SkipUtil;

/**
 * JUnit 4 skip logic.
 * 
 * @author Daniel Sagenschneider
 */
public class SkipJUnit4 extends SkipUtil {

	/**
	 * Invoke within test to skip if Stress test.
	 */
	public static void skipStress() {
		assumeFalse(isSkipStressTests());
	}

	/**
	 * Invoke within test to skip if Docker not available.
	 */
	public static void skipDocker() {
		assumeFalse(isSkipTestsUsingDocker());
	}

	/**
	 * Invoke within test to skip if GCloud not available.
	 */
	public static void skipGCloud() {
		assumeFalse(isSkipTestsUsingGCloud());
	}

	/**
	 * All access via static methods.
	 */
	private SkipJUnit4() {
		// All access via static methods
	}
}
