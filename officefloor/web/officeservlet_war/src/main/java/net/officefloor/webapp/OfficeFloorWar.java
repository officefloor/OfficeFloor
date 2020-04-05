package net.officefloor.webapp;

import java.util.Arrays;

import net.officefloor.OfficeFloorMain;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.servlet.supply.ServletWoofExtensionService;

/**
 * Enables {@link OfficeFloor} to run a WAR application.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorWar extends OfficeFloorMain {

	/**
	 * {@link Property} name for the web application (WAR) path.
	 */
	public static final String PROPERTY_WAR_PATH = ApplicationOfficeFloorSource.OFFICE_NAME + "."
			+ ServletWoofExtensionService.PROPERTY_WAR_PATH;

	/**
	 * Run WAR.
	 * 
	 * @param args Arguments with last being path to WAR.
	 * @throws Exception If fails to run.
	 */
	public static void main(String... args) throws Exception {

		// Ensure odd number of arguments
		if ((args.length % 2) != 1) {
			throw new IllegalArgumentException(
					"USAGE: java " + OfficeFloorWar.class.getName() + " [property name/value pairs] <WAR path>");
		}

		// Split arguments
		String warPath = args[args.length - 1];
		String[] mainArgs = Arrays.copyOf(args, args.length - 1);

		// Run the application
		OfficeFloorMain.mainWithDefaults(new String[] { PROPERTY_WAR_PATH, warPath }, mainArgs);
	}

}