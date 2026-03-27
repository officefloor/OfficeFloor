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

import java.util.Map;

import net.officefloor.test.module.ModuleAccessible;

/**
 * Abstract functionality for overriding the {@link System#getenv(String)}
 * values in tests.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractEnvironmentOverride<I extends AbstractExternalOverride<I>>
		extends AbstractExternalOverride<I> {

	/**
	 * Cached environment map.
	 */
	private static Map<String, String> environmentMap = null;

	/**
	 * Windows additional cached map.
	 */
	private static Map<String, String> windowsAdditionalMap = null;

	/**
	 * Change to environment.
	 */
	@FunctionalInterface
	public static interface EnvironmentChange {

		/**
		 * Changes the environment.
		 * 
		 * @param environment Mutable environment.
		 */
		void change(Map<String, String> environment);
	}

	/**
	 * Undertakes environment change.
	 * 
	 * @param change {@link EnvironmentChange}.
	 */
	@SuppressWarnings("unchecked")
	protected static void changeEnvironment(EnvironmentChange change) {

		// Lazy load
		if (environmentMap == null) {

			// Obtain the unmodifiable map for environment
			final String message = "Environment overrides used in testing";
			Map<String, String> unmodifiableMap = System.getenv();
			environmentMap = (Map<String, String>) ModuleAccessible.getFieldValue(unmodifiableMap, "m", message);

			// Windows has additional map for environment
			Class<?> processEnvironmentClass = null;
			try {
				processEnvironmentClass = AbstractEnvironmentOverride.class.getClassLoader()
						.loadClass("java.lang.ProcessEnvironment");
			} catch (ClassNotFoundException ex) {
				// Not windows
			}
			final String theCaseInsensitiveEnvironment = "theCaseInsensitiveEnvironment";
			if ((processEnvironmentClass != null)
					&& (ModuleAccessible.isFieldAvailable(processEnvironmentClass, theCaseInsensitiveEnvironment))) {
				windowsAdditionalMap = (Map<String, String>) ModuleAccessible.getFieldValue(null,
						processEnvironmentClass, theCaseInsensitiveEnvironment, message);
			}
		}

		// Undertake changes
		change.change(environmentMap);
		if (windowsAdditionalMap != null) {
			change.change(windowsAdditionalMap);
		}
	}

	/**
	 * Instantiate.
	 * 
	 * @param nameValuePairs Initial environment variable name/value pairs.
	 */
	public AbstractEnvironmentOverride(String... nameValuePairs) {
		super(nameValuePairs);
	}

	/*
	 * ================ AbstractExternalOverride ==================
	 */

	@Override
	protected String get(String name) {
		return System.getenv(name);
	}

	@Override
	protected void set(String name, String value) {
		changeEnvironment((env) -> env.put(name, value));
	}

	@Override
	protected void clear(String name) {
		changeEnvironment((env) -> env.remove(name));
	}

}
