package net.officefloor.test.system;

/**
 * Abstract functionality for overriding the {@link System#getProperty(String)}
 * values in tests.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractSystemPropertiesOverride<I extends AbstractExternalOverride<I>>
		extends AbstractExternalOverride<I> {

	/**
	 * Instantiate.
	 * 
	 * @param nameValuePairs Initial {@link System} property name/value pairs.
	 */
	public AbstractSystemPropertiesOverride(String... nameValuePairs) {
		super(nameValuePairs);
	}

	/*
	 * ==================== AbstractExternalOverride =====================
	 */

	@Override
	protected String get(String name) {
		return System.getProperty(name);
	}

	@Override
	protected void set(String name, String value) {
		System.setProperty(name, value);
	}

	@Override
	protected void clear(String name) {
		System.clearProperty(name);
	}

}