/*-
 * #%L
 * PostgreSQL Persistence Testing
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

package net.officefloor.jdbc.postgresql.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link Extension} to run PostgreSql.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlExtension extends AbstractPostgreSqlJUnit
		implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	/**
	 * Indicates if run PostgreSql for each test.
	 */
	private boolean isEach = true;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public PostgreSqlExtension() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public PostgreSqlExtension(Configuration configuration) {
		super(configuration);
	}

	/*
	 * ================== Extension ===================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

		// Start
		this.startPostgreSql();

		// Shutdown after all tests
		this.isEach = false;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {

		// Determine if start for each
		if (this.isEach) {
			this.startPostgreSql();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

		// Stop if for each
		if (this.isEach) {
			this.stopPostgreSql();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

		// Stop if after all
		if (!this.isEach) {
			this.stopPostgreSql();
		}
	}

}
