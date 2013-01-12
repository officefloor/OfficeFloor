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

import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectAdapterSource;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

/**
 * {@link HttpSecurityLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityLoaderImpl implements HttpSecurityLoader {

	/**
	 * {@link ManagedObjectLoader}.
	 */
	private final ManagedObjectLoader managedObjectLoader;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectLoader
	 *            {@link ManagedObjectLoader}.
	 */
	public HttpSecurityLoaderImpl(ManagedObjectLoader managedObjectLoader) {
		this.managedObjectLoader = managedObjectLoader;
	}

	/*
	 * ======================== HttpSecurityLoader ==========================
	 */

	@Override
	public <S, C, D extends Enum<D>, F extends Enum<F>, HS extends HttpSecuritySource<S, C, D, F>> PropertyList loadSpecification(
			HS httpSecuritySource) {
		return HttpSecurityManagedObjectAdapterSource.loadSpecification(
				httpSecuritySource, this.managedObjectLoader);
	}

	@Override
	public <S, C, D extends Enum<D>, F extends Enum<F>, HS extends HttpSecuritySource<S, C, D, F>> HttpSecurityType<S, C, D, F> loadHttpSecurityType(
			Class<HS> httpSecuritySourceClass, PropertyList propertyList) {
		// TODO implement HttpSecurityLoader.loadHttpSecurityType
		throw new UnsupportedOperationException(
				"TODO implement HttpSecurityLoader.loadHttpSecurityType");
	}

	@Override
	public <S, C, D extends Enum<D>, F extends Enum<F>> HttpSecurityType<S, C, D, F> loadHttpSecurityType(
			HttpSecuritySource<S, C, D, F> httpSecuritySource,
			PropertyList propertyList) {
		// TODO implement HttpSecurityLoader.loadHttpSecurityType
		throw new UnsupportedOperationException(
				"TODO implement HttpSecurityLoader.loadHttpSecurityType");
	}

}