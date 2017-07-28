/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.security;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Meta-data of the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySourceMetaData<S, C, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Obtains the {@link Class} of the security object.
	 * 
	 * @return {@link Class} of the security object.
	 */
	Class<S> getSecurityClass();

	/**
	 * <p>
	 * Obtains the {@link Class} of the credentials object to be provided by the
	 * application.
	 * <p>
	 * An instance of this {@link Class} is to be provided as a parameter to the
	 * {@link ManagedFunction} that attempts authentication. This allows application
	 * specific behaviour to obtain the credentials (such as a login page).
	 * <p>
	 * Should the security protocol be application agnostic (such as client
	 * security keys) this should be <code>null</code>.
	 * 
	 * @return {@link Class} of the credentials object or <code>null</code> if
	 *         no application specific behaviour.
	 */
	Class<C> getCredentialsClass();

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
	 * @return Meta-data of application {@link Flow} instances instigated
	 *         by this {@link HttpSecuritySource}.
	 */
	HttpSecurityFlowMetaData<F>[] getFlowMetaData();

}