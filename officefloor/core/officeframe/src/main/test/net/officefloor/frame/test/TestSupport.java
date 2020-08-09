package net.officefloor.frame.test;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * <p>
 * Test support object.
 * <p>
 * {@link TestSupportExtension} will invoke this to enable the
 * {@link TestSupport} instance to initialise itself.
 * 
 * @author Daniel Sagenschneider
 */
public interface TestSupport {

	/**
	 * Intialise.
	 * 
	 * @param context {@link ExtensionContext}.
	 * @throws Exception If fails to init.
	 */
	void init(ExtensionContext context) throws Exception;

}