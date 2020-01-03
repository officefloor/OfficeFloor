package net.officefloor.woof;

import net.officefloor.woof.objects.WoofObjectsLoader;

/**
 * Mock dependency for integration testing of {@link WoofObjectsLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockDependency {

	/**
	 * Obtains the message.
	 * 
	 * @return Message.
	 */
	public String getMessage() {
		return "TEST";
	}

}