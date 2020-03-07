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
	 * Obtains the environment {@link Map}.
	 * 
	 * @return Environment {@link Map}.
	 */
	@SuppressWarnings("unchecked")
	protected static Map<String, String> getEnvironmentMap() {
		if (environmentMap == null) {

			// Obtain the unmodifiable map for environment
			Map<String, String> unmodifiableMap = System.getenv();
			Class<?> clazz = unmodifiableMap.getClass();
			try {
				Field m = clazz.getDeclaredField("m");
				m.setAccessible(true);
				environmentMap = (Map<String, String>) m.get(unmodifiableMap);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to access modifiable map for environment", e);
			} catch (NoSuchFieldException e) {
				throw new IllegalStateException("Environment not available for being modifiable", e);
			}

		}
		return environmentMap;
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
		getEnvironmentMap().put(name, value);
	}

	@Override
	protected void clear(String name) {
		getEnvironmentMap().remove(name);
	}

}