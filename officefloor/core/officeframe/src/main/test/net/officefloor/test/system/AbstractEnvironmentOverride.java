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
