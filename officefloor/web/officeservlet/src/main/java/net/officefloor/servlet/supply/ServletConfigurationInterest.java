package net.officefloor.servlet.supply;

import javax.servlet.Servlet;

/**
 * Interest in continuing {@link Servlet} configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletConfigurationInterest {

	/**
	 * Completes interest.
	 * 
	 * @throws Exception If fails to complete interest.
	 */
	void completeInterest() throws Exception;

}