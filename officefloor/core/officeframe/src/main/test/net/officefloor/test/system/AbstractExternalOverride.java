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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Abstract functionality for modifying {@link System} for tests.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractExternalOverride<I extends AbstractExternalOverride<I>> {

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
	public AbstractExternalOverride(String... nameValuePairs) {
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

		// Load the overrides
		OverrideReset reset = this.override();
		try {

			// Undertake logic
			runnable.run();

		} finally {
			// Reset overrides
			reset.resetOverrides();
		}
	}

	/**
	 * Overrides the external values.
	 * 
	 * @return {@link OverrideReset} to reset the overrides.
	 */
	protected OverrideReset override() {

		// Load the overrides
		OverrideReset overrides = new OverrideReset();
		for (int i = 0; i < this.nameValuePairs.size(); i += 2) {

			// Obtain the property name / value
			String name = this.nameValuePairs.get(i);
			String value = this.nameValuePairs.get(i + 1);

			// Obtain property value for reset
			String originalValue = this.get(name);
			if (originalValue == null) {
				overrides.clear.add(name);
			} else {
				overrides.reset.setProperty(name, originalValue);
			}

			// Specify the property
			this.set(name, value);
		}

		// Return the overrides reset
		return overrides;
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

	/**
	 * Contains state for reseting the overrides.
	 */
	protected final class OverrideReset {

		/**
		 * Property/Variable names to clear.
		 */
		private final List<String> clear = new LinkedList<>();

		/**
		 * Property/Variable name/values to reset.
		 */
		private final Properties reset = new Properties();

		/**
		 * Resets the external overrides.
		 */
		public void resetOverrides() {

			// Reset overrides
			for (String name : reset.stringPropertyNames()) {
				String value = reset.getProperty(name);
				AbstractExternalOverride.this.set(name, value);
			}
			for (String name : clear) {
				AbstractExternalOverride.this.clear(name);
			}
		}
	}

}
