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
package net.officefloor.server.http;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} for specifying {@link System} properties.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemPropertiesRule implements TestRule {

	/**
	 * Context {@link Runnable}.
	 * 
	 * @param <T> Possible {@link Throwable} from logic.
	 */
	public static interface ContextRunnable<T extends Throwable> {

		/**
		 * Undertakes the logic.
		 * 
		 * @throws T Possible failure.
		 */
		void run() throws T;
	}

	/**
	 * Overwrite {@link System} properties.
	 */
	private final String[] systemPropertyNameValuePairs;

	/**
	 * Instantiate.
	 * 
	 * @param systemPropertyNameValuePairs {@link System} property name/value pairs.
	 */
	public SystemPropertiesRule(String... systemPropertyNameValuePairs) {
		this.systemPropertyNameValuePairs = systemPropertyNameValuePairs;
	}

	/**
	 * Runs {@link ContextRunnable} with configured {@link System} properties.
	 *
	 * @param          <T> Possible {@link Throwable} from logic.
	 * @param runnable {@link ContextRunnable}.
	 * @throws T Possible {@link Throwable}.
	 */
	public <T extends Throwable> void run(ContextRunnable<T> runnable) throws T {

		// Load the System properties
		List<String> clear = new LinkedList<>();
		Properties reset = new Properties();
		for (int i = 0; i < this.systemPropertyNameValuePairs.length; i += 2) {

			// Obtain the property name / value
			String name = this.systemPropertyNameValuePairs[i];
			String value = this.systemPropertyNameValuePairs[i + 1];

			// Obtain property value for reset
			String originalValue = System.getProperty(name);
			if (originalValue == null) {
				clear.add(name);
			} else {
				reset.setProperty(name, originalValue);
			}

			// Specify the property
			System.setProperty(name, value);
		}

		try {

			// Undertake logic
			runnable.run();

		} finally {
			// Reset properties
			for (String name : reset.stringPropertyNames()) {
				String value = reset.getProperty(name);
				System.setProperty(name, value);
			}
			for (String name : clear) {
				System.clearProperty(name);
			}
		}
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