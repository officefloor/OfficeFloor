/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.test;

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

/**
 * Provides ability to skip via {@link ExtensionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class JUnit5Skip {

	/**
	 * Undertakes best attempt to cause skip of test via the
	 * {@link ExtensionContext}.
	 *
	 * @param context       {@link ExtensionContext}.
	 * @param skipMessage   Message for skipping.
	 * @param optionalCause Optional cause.
	 * @return {@link RuntimeException} to throw to complete the skip.
	 */
	public static RuntimeException skip(ExtensionContext context, String skipMessage, Throwable optionalCause) {

		// Create skip exception
		TestAbortedException skipException = new TestAbortedException(skipMessage, optionalCause);

		// Undertake skip via replacing throwable in execution context
		try {

			// Obtain throwable collector
			Field throwableCollectorField = context.getClass().getDeclaredField("throwableCollector");
			throwableCollectorField.setAccessible(true);
			Object throwableCollector = throwableCollectorField.get(context);

			// Replace the throwable with skip
			Field throwableField = throwableCollector.getClass().getSuperclass().getDeclaredField("throwable");
			throwableField.setAccessible(true);
			throwableField.set(throwableCollector, skipException);

		} catch (Exception ex) {
			// No throwable collector, so propagate skip as best attempt
			throw skipException;
		}

		// Return the exception
		return skipException;
	}

	/**
	 * All access via static methods.
	 */
	private JUnit5Skip() {
	}
}
