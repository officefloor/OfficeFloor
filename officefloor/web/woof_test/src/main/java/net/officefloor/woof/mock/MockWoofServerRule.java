/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.woof.mock;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} for running the {@link MockWoofServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerRule extends MockWoofServer implements TestRule {

	/**
	 * {@link MockWoofServerConfigurer} instances.
	 */
	private final MockWoofServerConfigurer[] configurers;

	/**
	 * Instantiate.
	 * 
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 */
	public MockWoofServerRule(MockWoofServerConfigurer... configurers) {

		// Ensure always have at least one configurer to load WoOF
		this.configurers = configurers != null ? configurers
				: new MockWoofServerConfigurer[] { (woofContext, compiler) -> {
				} };
	}

	/**
	 * =============== MockWoofServer =====================
	 */

	@Override
	public MockWoofServerRule timeout(int timeout) {
		super.timeout(timeout);
		return this;
	}

	/*
	 * =================== TestRule =======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				try (MockWoofServer server = MockWoofServer.open(MockWoofServerRule.this,
						MockWoofServerRule.this.configurers)) {
					base.evaluate();
				}
			}
		};
	}

}