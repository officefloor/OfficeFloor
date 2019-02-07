/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.server.http.HttpException;

/**
 * <p>
 * Source for obtaining {@link HttpSecurity}.
 * <p>
 * As security is specific to applications, both the security object and
 * credentials are specified by the application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySource<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must be
	 * able to return the specification immediately after a default constructor
	 * instantiation.
	 * 
	 * @return Specification of this.
	 */
	HttpSecuritySourceSpecification getSpecification();

	/**
	 * Initialises the {@link HttpSecuritySource}.
	 * 
	 * @param context {@link HttpSecuritySourceContext} to use in initialising.
	 * @return Meta-data to describe this.
	 * @throws Exception Should the {@link HttpSecuritySource} fail to configure
	 *                   itself from the input properties.
	 */
	HttpSecuritySourceMetaData<A, AC, C, O, F> init(HttpSecuritySourceContext context) throws Exception;

	/**
	 * <p>
	 * Called once after {@link #init(HttpSecuritySourceContext)} to indicate this
	 * {@link HttpSecuritySource} should start execution.
	 * <p>
	 * On invocation of this method, {@link ProcessState} instances may be invoked
	 * via the {@link HttpSecurityExecuteContext}.
	 * 
	 * @param context {@link HttpSecurityExecuteContext} to use in starting this
	 *                {@link HttpSecuritySource}.
	 * @throws Exception Should the {@link HttpSecuritySourceSource} fail to start
	 *                   execution.
	 */
	void start(HttpSecurityExecuteContext<F> context) throws Exception;

	/**
	 * Sources the {@link HttpSecurity}.
	 * 
	 * @param context {@link HttpSecurity}.
	 * @return {@link HttpSecurity}.
	 * @throws HttpException If fails to source the {@link HttpSecurity}.
	 */
	HttpSecurity<A, AC, C, O, F> sourceHttpSecurity(HttpSecurityContext context) throws HttpException;

	/**
	 * <p>
	 * Called to notify that the {@link OfficeFloor} is being closed.
	 * <p>
	 * On return from this method, no further {@link ProcessState} instances may be
	 * invoked.
	 */
	void stop();

}