/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.spring.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.spring.SpringSupplierSource;

/**
 * Captures the {@link ConfigurableApplicationContext} from
 * {@link SpringSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringRule implements TestRule {

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext applicationContext = null;

	/**
	 * Obtains the {@link ConfigurableApplicationContext}.
	 * 
	 * @return {@link ConfigurableApplicationContext}.
	 */
	public ConfigurableApplicationContext getApplicationContext() {

		// Ensure have application context
		if (this.applicationContext == null) {
			throw new IllegalStateException(
					"Must be in " + this.getClass().getSimpleName() + " context for accessing Spring beans");
		}

		// Return the application context
		return this.applicationContext;
	}

	/**
	 * Obtains the Spring bean by name.
	 * 
	 * @param name Name of bean.
	 * @return Bean.
	 */
	public Object getBean(String name) {
		return this.getApplicationContext().getBean(name);
	}

	/**
	 * Obtains the Spring bean by type.
	 * 
	 * @param requiredType Required type.
	 * @return Bean.
	 */
	public <B> B getBean(Class<B> requiredType) {
		return this.getApplicationContext().getBean(requiredType);
	}

	/*
	 * ===================== TestRule =============================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					// Capture the Spring context
					SpringSupplierSource.captureApplicationContext(
							(context) -> SpringRule.this.applicationContext = context, () -> {

								// Evaluate the test
								base.evaluate();
								return null;
							});
				} finally {
					// Clear the application context
					SpringRule.this.applicationContext = null;
				}
			}
		};
	}

}