/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;

/**
 * Meta-data of the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySourceMetaData<A, AC extends Serializable, C, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Obtains the {@link Class} of the authentication object.
	 * 
	 * @return {@link Class} of the authentication object.
	 */
	Class<A> getAuthenticationType();

	/**
	 * Obtains the {@link HttpAuthenticationFactory} to adapt the custom
	 * authentication into a {@link HttpAuthentication}.
	 * 
	 * @return {@link HttpAuthenticationFactory} to adapt the custom
	 *         authentication into a {@link HttpAuthentication}.
	 */
	HttpAuthenticationFactory<A, C> getHttpAuthenticationFactory();

	/**
	 * <p>
	 * Obtains the {@link Class} of the credentials object to be provided by the
	 * application.
	 * <p>
	 * An instance of this {@link Class} is to be provided as a parameter to the
	 * {@link ManagedFunction} that attempts authentication. This allows
	 * application specific behaviour to obtain the credentials (such as a login
	 * page).
	 * <p>
	 * Should the security protocol be application agnostic (such as client
	 * security keys) this should be <code>null</code>.
	 * 
	 * @return {@link Class} of the credentials object or <code>null</code> if
	 *         no application specific behaviour.
	 */
	Class<C> getCredentialsType();

	/**
	 * Obtains the {@link Class} of the access control object.
	 * 
	 * @return {@link Class} of the access control object.
	 */
	Class<AC> getAccessControlType();

	/**
	 * Obtains the {@link HttpAccessControlFactory} to adapt the custom access
	 * control into a {@link HttpAccessControl}.
	 * 
	 * @return {@link HttpAccessControlFactory} to adapt the custom access
	 *         control into a {@link HttpAccessControl}.
	 */
	HttpAccessControlFactory<AC> getHttpAccessControlFactory();

	/**
	 * Obtains the list of {@link HttpSecurityDependencyMetaData} instances
	 * required by this {@link HttpSecuritySource}.
	 * 
	 * @return Meta-data of the required dependencies for this
	 *         {@link HttpSecuritySource}.
	 */
	HttpSecurityDependencyMetaData<D>[] getDependencyMetaData();

	/**
	 * Obtains the list of {@link HttpSecurityFlowMetaData} instances should
	 * this {@link HttpSecuritySource} require application specific behaviour.
	 * 
	 * @return Meta-data of application {@link Flow} instances instigated by
	 *         this {@link HttpSecuritySource}.
	 */
	HttpSecurityFlowMetaData<F>[] getFlowMetaData();

}
