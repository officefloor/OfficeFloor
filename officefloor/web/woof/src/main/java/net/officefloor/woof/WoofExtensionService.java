package net.officefloor.woof;

import java.util.ServiceLoader;

import net.officefloor.web.build.WebArchitect;

/**
 * {@link ServiceLoader} service that enables extending functionality over and
 * above the {@link WoofLoader} by direct configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofExtensionService {

	/**
	 * Extends the {@link WebArchitect}.
	 * 
	 * @param context
	 *            {@link WoofContext}.
	 * @throws Exception
	 *             If fails to extend.
	 */
	void extend(WoofContext context) throws Exception;

}