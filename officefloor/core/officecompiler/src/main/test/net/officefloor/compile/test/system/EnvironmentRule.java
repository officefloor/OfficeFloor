package net.officefloor.compile.test.system;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.rules.TestRule;

/**
 * {@link TestRule} for specifying environment ( {@link System#getenv()} ) for
 * tests.
 * 
 * @author Daniel Sagenschneider
 */
public class EnvironmentRule extends AbstractSystemRule<EnvironmentRule> {

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
	 * Obtains the environment {@link Map}.
	 * 
	 * @return Environment {@link Map}.
	 */
	@SuppressWarnings("unchecked")
	protected static void changeEnvironment(EnvironmentChange change) {

		// Lazy load
		if (environmentMap == null) {

			// Obtain the unmodifiable map for environment
			try {
				Map<String, String> unmodifiableMap = System.getenv();
				Class<?> clazz = unmodifiableMap.getClass();
				Field m = clazz.getDeclaredField("m");
				m.setAccessible(true);
				environmentMap = (Map<String, String>) m.get(unmodifiableMap);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Unable to access modifiable map for environment", ex);
			} catch (NoSuchFieldException ex) {
				throw new IllegalStateException("Environment not available for being modifiable", ex);
			}

			// Windows has additional map for environment
			try {
				Class<?> processEnvironmentClass = EnvironmentRule.class.getClassLoader()
						.loadClass("java.lang.ProcessEnvironment");
				Field theCaseInsensitiveEnvironment = processEnvironmentClass
						.getDeclaredField("theCaseInsensitiveEnvironment");
				theCaseInsensitiveEnvironment.setAccessible(true);
				windowsAdditionalMap = (Map<String, String>) theCaseInsensitiveEnvironment
						.get(theCaseInsensitiveEnvironment);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Unable to access modifiable windows map for environment", ex);
			} catch (NoSuchFieldException | ClassNotFoundException ex) {
				// Not windows
				windowsAdditionalMap = null;
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
	public EnvironmentRule(String... nameValuePairs) {
		super(nameValuePairs);
	}

	/*
	 * ================ AbstractSystemRule ==================
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