package net.officefloor.compile.impl.classes;

import java.io.IOException;

import net.officefloor.compile.classes.ClassPathScanner;
import net.officefloor.compile.classes.ClassPathScannerContext;
import net.officefloor.compile.classes.ClassPathScannerServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ClassPathScanner} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockClassPathScanner implements ClassPathScannerServiceFactory, ClassPathScanner {

	/**
	 * Mock package path. Also, invalid path to confirm handled gracefully.
	 */
	public static final String MOCK_PACKAGE_PATH = "mock package path";

	/**
	 * Mock entry path added.
	 */
	public static final String MOCK_ENTRY_PATH = "MOCK";

	/*
	 * ====================== ClassPathScannerServiceFactory =====================
	 */

	@Override
	public ClassPathScanner createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ============================ ClassPathScanner ==============================
	 */

	@Override
	public void scan(ClassPathScannerContext context) throws IOException {
		if (MOCK_PACKAGE_PATH.equals(context.getPackageName())) {

			// Add mock entry
			context.addEntry(MOCK_ENTRY_PATH);
		}
	}

}