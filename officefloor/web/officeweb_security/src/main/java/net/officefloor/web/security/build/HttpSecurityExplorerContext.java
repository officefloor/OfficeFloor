package net.officefloor.web.security.build;

import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Context for the {@link HttpSecurityExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityExplorerContext {

	/**
	 * Obtains the name of the {@link HttpSecurity}.
	 * 
	 * @return Name of the {@link HttpSecurity}.
	 */
	String getHttpSecurityName();

	/**
	 * Obtains the {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecuritySource}.
	 */
	HttpSecuritySource<?, ?, ?, ?, ?> getHttpSecuritySource();

	/**
	 * Obtains the {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityType}.
	 */
	HttpSecurityType<?, ?, ?, ?, ?> getHttpSecurityType();

	/**
	 * Obtains the {@link ExecutionManagedFunction} for the
	 * {@link HttpSecurityFlowType}.
	 * 
	 * @param flowType {@link HttpSecurityFlowType}.
	 * @return {@link ExecutionManagedFunction} for the
	 *         {@link HttpSecurityFlowType}.
	 */
	ExecutionManagedFunction getManagedFunction(HttpSecurityFlowType<?> flowType);

}