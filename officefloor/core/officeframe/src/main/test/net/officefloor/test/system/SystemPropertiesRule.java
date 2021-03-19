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

package net.officefloor.test.system;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} for specifying {@link System} properties.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemPropertiesRule extends AbstractSystemPropertiesOverride<SystemPropertiesRule> implements TestRule {

	/**
	 * Instantiate.
	 * 
	 * @param nameValuePairs Initial {@link System} property name/value pairs.
	 */
	public SystemPropertiesRule(String... nameValuePairs) {
		super(nameValuePairs);
	}

	/*
	 * ================ TestRule ================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				SystemPropertiesRule.this.run(() -> base.evaluate());
			}
		};
	}

}
