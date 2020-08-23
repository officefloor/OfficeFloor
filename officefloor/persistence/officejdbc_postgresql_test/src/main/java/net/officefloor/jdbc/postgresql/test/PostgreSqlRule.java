/*-
 * #%L
 * PostgreSQL Persistence Testing
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

package net.officefloor.jdbc.postgresql.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.jdbc.postgresql.test.AbstractPostgreSqlJUnit.Configuration;

/**
 * {@link TestRule} to run PostgreSql.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlRule extends AbstractPostgreSqlJUnit implements TestRule {

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public PostgreSqlRule() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public PostgreSqlRule(Configuration configuration) {
		super(configuration);
	}

	/*
	 * ================== TestRule ===================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Start PostgreSql
				PostgreSqlRule.this.startPostgreSql();
				try {

					// Run the test
					base.evaluate();

				} finally {
					// Stop PostgreSql
					PostgreSqlRule.this.stopPostgreSql();
				}
			}
		};
	}

}
