package net.officefloor.compile.classes;

/**
 * Context for the {@link ClassPathScanner}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassPathScannerContext {

	/**
	 * Obtains the name of the {@link Package} to scan for entries.
	 * 
	 * @return Name of the {@link Package} to scan for entries.
	 */
	String getPackageName();

	/**
	 * Obtains the {@link ClassLoader}.
	 * 
	 * @return {@link ClassLoader}.
	 */
	ClassLoader getClassLoader();

	/**
	 * Adds a {@link Class} path entry for the package.
	 * 
	 * @param entryPath {@link Class} path entry for the package. This is required
	 *                  to be full path to the entry.
	 */
	void addEntry(String entryPath);

}