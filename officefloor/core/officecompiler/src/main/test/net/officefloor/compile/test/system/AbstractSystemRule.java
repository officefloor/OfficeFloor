/*-
 * #%L
 * HTTP Server
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

package net.officefloor.compile.test.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Abstract {@link TestRule} for modifying {@link System} for tests.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSystemRule<I extends AbstractSystemRule<I>> implements TestRule {

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
	 * Overwrite name value pairs.
	 */
	private final List<String> nameValuePairs = new ArrayList<>();

	/**
	 * Instantiate.
	 * 
	 * @param nameValuePairs Initial name/value pairs.
	 */
	public AbstractSystemRule(String... nameValuePairs) {
		this.nameValuePairs.addAll(Arrays.asList(nameValuePairs));
	}

	/**
	 * Allow builder pattern for loading properties.
	 * 
	 * @param name  Name.
	 * @param value Value.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public I property(String name, String value) {
		this.nameValuePairs.add(name);
		this.nameValuePairs.add(value);
		return (I) this;
	}

	/**
	 * Runs {@link ContextRunnable} with configured {@link System} properties.
	 *
	 * @param <T>      Possible {@link Throwable} from logic.
	 * @param runnable {@link ContextRunnable}.
	 * @throws T Possible {@link Throwable}.
	 */
	public <T extends Throwable> void run(ContextRunnable<T> runnable) throws T {

		// Load the System properties
		List<String> clear = new LinkedList<>();
		Properties reset = new Properties();
		for (int i = 0; i < this.nameValuePairs.size(); i += 2) {

			// Obtain the property name / value
			String name = this.nameValuePairs.get(i);
			String value = this.nameValuePairs.get(i + 1);

			// Obtain property value for reset
			String originalValue = this.get(name);
			if (originalValue == null) {
				clear.add(name);
			} else {
				reset.setProperty(name, originalValue);
			}

			// Specify the property
			this.set(name, value);
		}

		try {

			// Undertake logic
			runnable.run();

		} finally {
			// Reset properties
			for (String name : reset.stringPropertyNames()) {
				String value = reset.getProperty(name);
				this.set(name, value);
			}
			for (String name : clear) {
				this.clear(name);
			}
		}
	}

	/*
	 * ============== abstract methods ==========
	 */

	/**
	 * Obtains the value.
	 * 
	 * @param name Name of value.
	 * @return Value.
	 */
	protected abstract String get(String name);

	/**
	 * Specifies the value.
	 * 
	 * @param name  Name for value.
	 * @param value Value.
	 */
	protected abstract void set(String name, String value);

	/**
	 * Clears the value.
	 * 
	 * @param name Name of value.
	 */
	protected abstract void clear(String name);

	/*
	 * ================ TestRule ================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				AbstractSystemRule.this.run(() -> base.evaluate());
			}
		};
	}

}