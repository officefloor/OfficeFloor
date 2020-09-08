/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
