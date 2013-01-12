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
package net.officefloor.plugin.web.http.security.type;

import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

/**
 * <code>Type definition</code> of a {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityType<S, C, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Obtains the type for security.
	 * 
	 * @return Type for security.
	 */
	Class<S> getSecurityClass();

	/**
	 * Obtains the type for credentials.
	 * 
	 * @return Type for credentials. May be <code>null</code> if no application
	 *         specific behaviour is required to provide credentials.
	 */
	Class<C> getCredentialsClass();

	/**
	 * Obtains the {@link HttpSecurityDependencyType} definitions of the
	 * required dependencies for the {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecurityDependencyType} definitions of the required
	 *         dependencies for the {@link HttpSecuritySource}.
	 */
	HttpSecurityDependencyType<D>[] getDependencyTypes();

	/**
	 * Obtains the {@link HttpSecurityFlowType} definitions of the
	 * {@link JobSequence} instances required to be linked for the
	 * {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecurityFlowType} definitions of the
	 *         {@link JobSequence} instances required to be linked for the
	 *         {@link HttpSecuritySource}.
	 */
	HttpSecurityFlowType<?>[] getFlowTypes();

}