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

package net.officefloor.test.system;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * {@link Extension} for specifying environment ( {@link System#getenv()} ) for
 * tests.
 * 
 * @author Daniel Sagenschneider
 */
public class EnvironmentExtension extends AbstractEnvironmentOverride<EnvironmentExtension>
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * {@link Namespace}.
	 */
	private final static Namespace NAMESPACE = Namespace.create(EnvironmentExtension.class);

	/**
	 * Instantiate.
	 * 
	 * @param nameValuePairs Initial environment variable name/value pairs.
	 */
	public EnvironmentExtension(String... nameValuePairs) {
		super(nameValuePairs);
	}

	/**
	 * Obtains the {@link Store}.
	 * 
	 * @param context {@link ExtensionContext}.
	 * @return {@link Store}.
	 */
	private Store getStore(ExtensionContext context) {
		return context.getStore(NAMESPACE);
	}

	/*
	 * ======================== Extension ===========================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Undertake override
		OverrideReset overrideReset = this.override();

		// Register state to reset
		this.getStore(context).put(context.getRequiredTestClass(), new EnvironmentOverrideState(true, overrideReset));
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Determine if override all
		EnvironmentOverrideState override = this.getStore(context).get(context.getRequiredTestClass(),
				EnvironmentOverrideState.class);
		if (override != null) {
			// Overriding before all tests
			return;
		}

		// Override on each test
		OverrideReset overrideReset = this.override();

		// Register state to reset
		this.getStore(context).put(context.getRequiredTestClass(), new EnvironmentOverrideState(false, overrideReset));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Obtain the override reset
		EnvironmentOverrideState override = this.getStore(context).get(context.getRequiredTestClass(),
				EnvironmentOverrideState.class);
		if (override.isAfterAll) {
			return; // reset after all tests
		}

		// Reset after each test
		override.overrideReset.resetOverrides();

		// Remove state
		this.getStore(context).remove(context.getRequiredTestClass());
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Obtain the override reset
		EnvironmentOverrideState override = this.getStore(context).get(context.getRequiredTestClass(),
				EnvironmentOverrideState.class);
		if (override == null) {
			return; // reset after each
		}

		// Reset if after all
		if (override.isAfterAll) {
			override.overrideReset.resetOverrides();
		}

		// Remove state
		this.getStore(context).remove(context.getRequiredTestClass());
	}

	/**
	 * Override state for the environment.
	 */
	private static class EnvironmentOverrideState {

		/**
		 * Indicates if override after all tests complete.
		 */
		private final boolean isAfterAll;

		/**
		 * {@link OverrideReset}.
		 */
		private final OverrideReset overrideReset;

		/**
		 * Instantiate.
		 * 
		 * @param isAfterAll    Indicates if override after all tests complete.
		 * @param overrideReset {@link OverrideReset}.
		 */
		private EnvironmentOverrideState(boolean isAfterAll, OverrideReset overrideReset) {
			this.isAfterAll = isAfterAll;
			this.overrideReset = overrideReset;
		}
	}

}
