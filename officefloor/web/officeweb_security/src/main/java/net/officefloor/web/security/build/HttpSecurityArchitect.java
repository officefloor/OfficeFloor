package net.officefloor.web.security.build;

import java.io.Serializable;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Builds the {@link HttpSecurityBuilder} instances for the
 * {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityArchitect {

	/**
	 * Adds a {@link HttpSecurity}.
	 *
	 * @param <A>
	 *            Authentication type.
	 * @param <AC>
	 *            Access control type.
	 * @param <C>
	 *            Credentials type.
	 * @param <O>
	 *            Dependency key type.
	 * @param <F>
	 *            {@link Flow} key type.
	 * @param securityName
	 *            Name of the {@link HttpSecurityBuilder}. This name is use to
	 *            qualify dependency injection, should this particular
	 *            {@link HttpSecurityBuilder} be required.
	 * @param httpSecuritySourceClassName
	 *            Name of the {@link HttpSecuritySource} {@link Class}.
	 * @return {@link HttpSecurityBuilder}.
	 */
	<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityBuilder addHttpSecurity(
			String securityName, String httpSecuritySourceClassName);

	/**
	 * Adds a {@link HttpSecurity}.
	 * 
	 * @param <A>
	 *            Authentication type.
	 * @param <AC>
	 *            Access control type.
	 * @param <C>
	 *            Credentials type.
	 * @param <O>
	 *            Dependency key type.
	 * @param <F>
	 *            {@link Flow} key type.
	 * @param securityName
	 *            Name of the {@link HttpSecurityBuilder}. This name is use to
	 *            qualify dependency injection, should this particular
	 *            {@link HttpSecurityBuilder} be required.
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 * @return {@link HttpSecurityBuilder}.
	 */
	<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityBuilder addHttpSecurity(
			String securityName, HttpSecuritySource<A, AC, C, O, F> httpSecuritySource);

	/**
	 * Creates a {@link HttpSecurer}.
	 * 
	 * @param securable
	 *            {@link HttpSecurable} to provide the access configuration. May be
	 *            <code>null</code> to just require authentication.
	 * @return {@link HttpSecurer}.
	 */
	HttpSecurer createHttpSecurer(HttpSecurable securable);

	/**
	 * Informs the {@link WebArchitect} of the necessary security. This is to be
	 * invoked once all security is configured.
	 */
	void informWebArchitect();

}