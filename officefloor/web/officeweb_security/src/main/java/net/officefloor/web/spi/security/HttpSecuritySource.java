/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * @throws Exception Should the {@link HttpSecuritySource} fail to start
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
