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

package net.officefloor.server.google.function.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link Extension} for Google {@link HttpFunction}.
 */
public class GoogleHttpFunctionExtension extends AbstractGoogleHttpFunctionJUnit<GoogleHttpFunctionExtension>
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates whether to start/stop for each test.
	 */
	private boolean isEach = true;

	/**
	 * Instantiate with {@link HttpFunction}.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public GoogleHttpFunctionExtension(Class<?> httpFunctionClass) {
		super(httpFunctionClass);
	}

	/**
	 * Instantiate using the {@link OfficeFloor} {@link HttpFunction}.
	 */
	public GoogleHttpFunctionExtension() {
	}

	/*
	 * ================= Extension ====================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Start only once for all tests
		this.isEach = false;

		// Start the server
		this.openHttpServer();
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Start the server if for each
		if (this.isEach) {
			this.openHttpServer();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Stop the server if for each
		if (this.isEach) {
			this.teardownHttpFunction();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Stop server if all
		if (!this.isEach) {
			this.teardownHttpFunction();
		}

		// Reset to all
		this.isEach = false;
	}

}
