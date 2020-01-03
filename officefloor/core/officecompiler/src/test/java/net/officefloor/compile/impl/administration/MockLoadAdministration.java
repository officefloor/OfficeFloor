package net.officefloor.compile.impl.administration;

import org.junit.Assert;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.plugin.administration.clazz.ClassAdministrationSource;

/**
 * Class for {@link ClassAdministrationSource} that enables validating loading a
 * {@link AdministrationType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadAdministration {

	/**
	 * Mock extension interface.
	 */
	public static class MockExtensionInterface {
	}

	/**
	 * Administration method.
	 * 
	 * @param interfaces
	 *            Extension interfaces.
	 */
	public void admin(MockExtensionInterface[] interfaces) {
	}

	/**
	 * Validates the {@link AdministrationType} is correct for this class
	 * object.
	 * 
	 * @param administrationType
	 *            {@link AdministrationType}
	 */
	public static void assertAdministrationType(AdministrationType<?, ?, ?> administrationType) {
		Assert.assertEquals("Incorrect extension interface", MockExtensionInterface.class,
				administrationType.getExtensionType());
	}

}