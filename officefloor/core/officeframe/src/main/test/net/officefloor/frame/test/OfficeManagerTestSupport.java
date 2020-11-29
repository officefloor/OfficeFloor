package net.officefloor.frame.test;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.extension.ExtensionContext;

import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * {@link TestSupport} to provide the {@link OfficeManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagerTestSupport implements TestSupport {

	/**
	 * Obtains the {@link OfficeManager}.
	 * 
	 * @return {@link OfficeManager}.
	 */
	public OfficeManager getOfficeManager() {
		return fail("TODO obtain OfficeManager");
	}

	/*
	 * ===================== TestSupport =======================
	 */

	@Override
	public void init(ExtensionContext context) throws Exception {

		// Set up to capture the Office Manager
		ConstructTestSupport construct = TestSupportExtension.getTestSupport(ConstructTestSupport.class, context);

		// TODO set up to capture OfficeManager
	}

}
