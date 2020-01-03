package net.officefloor.gef.editor.style;

import java.util.Arrays;

/**
 * System based {@link StyleRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemStyleRegistry extends AbstractStyleRegistry {

	/**
	 * Instantiate.
	 */
	public SystemStyleRegistry() {

		// Obtain the package path for the URL protocol
		String protocolPackageFull = Handler.class.getPackage().getName();
		final String protocolPackageName = protocolPackageFull.substring(0, protocolPackageFull.lastIndexOf('.'));

		// Determine if already include the package
		final String PROPERTY_NAME = "java.protocol.handler.pkgs";
		String existingPackages = System.getProperty(PROPERTY_NAME, "");

		// Determine if already contains package
		String[] packages = existingPackages.split("|");
		if (!Arrays.stream(packages).anyMatch((packageName) -> packageName.equals(protocolPackageName))) {

			// Load in the protocol package
			String newPackages = (((existingPackages == null) || (existingPackages.trim().length() == 0)) ? ""
					: existingPackages + "|") + protocolPackageName;
			System.setProperty(PROPERTY_NAME, newPackages);
		}
	}

}