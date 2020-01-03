package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Configuration for {@link HttpSecuritySectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityConfiguration<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link HttpSecurity}.
	 * 
	 * @return Name of the {@link HttpSecurity}.
	 */
	String getHttpSecurityName();

	/**
	 * Obtains the {@link HttpSecurity}.
	 * 
	 * @return {@link HttpSecurity}.
	 */
	HttpSecurity<A, AC, C, O, F> getHttpSecurity();

	/**
	 * Obtains the {@link Flow} key {@link Enum} {@link Class}.
	 * 
	 * @return {@link Flow} key {@link Enum} {@link Class}.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityType}.
	 */
	HttpSecurityType<A, AC, C, O, F> getHttpSecurityType();

}