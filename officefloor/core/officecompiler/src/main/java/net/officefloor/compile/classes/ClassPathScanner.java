package net.officefloor.compile.classes;

import java.io.IOException;

/**
 * <p>
 * Allows enhancing the {@link Class} path scanning with custom scanning.
 * <p>
 * This is useful if specific {@link ClassLoader} instances are used that
 * require custom logic to scan them.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassPathScanner {

	/**
	 * Scans the class path.
	 * 
	 * @param context {@link ClassPathScannerContext}.
	 * @throws IOException If failure in scanning the class path.
	 */
	void scan(ClassPathScannerContext context) throws IOException;

}