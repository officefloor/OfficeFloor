/*-
 * #%L
 * Google Function Testing
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

package net.officefloor.server.google.function.officefloor;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Reference to the {@link OfficeFloor} {@link HttpFunction} implementation.
 */
public class OfficeFloorHttpFunctionReference {

	/**
	 * {@link Class} name of the {@link OfficeFloor} implementing
	 * {@link HttpFunction}.
	 */
	public static final String OFFICEFLOOR_HTTP_FUNCTION_CLASS_NAME = "net.officefloor.server.google.function.OfficeFloorHttpFunction";

	/**
	 * Obtains the {@link OfficeFloor} implementing {@link HttpFunction}.
	 * 
	 * @return {@link OfficeFloor} implementing {@link HttpFunction}.
	 */
	public static Class<?> getOfficeFloorHttpFunctionClass() {
		try {

			// Attempt to load the default OfficeFloor HttpFunction
			return OfficeFloorHttpFunctionReference.class.getClassLoader()
					.loadClass(OFFICEFLOOR_HTTP_FUNCTION_CLASS_NAME);

		} catch (Exception ex) {
			JUnitAgnosticAssert.fail(ex);
			throw new IllegalStateException("fail should propagate the failure");
		}
	}

}
