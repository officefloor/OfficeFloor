package net.officefloor.web.security.build;

import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Explorer of the execution tree from the {@link HttpSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityExplorer {

	/**
	 * Explores the {@link HttpSecurity}.
	 * 
	 * @param context {@link HttpSecurityExplorerContext}.
	 * @throws Exception If fails in exploring.
	 */
	public void explore(HttpSecurityExplorerContext context) throws Exception;
}