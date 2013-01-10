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

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceProperty;
import net.officefloor.plugin.web.http.security.HttpSecuritySourceSpecification;

/**
 * Loads the {@link HttpSecurityType} from the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link HttpSecuritySourceSpecification} for the
	 * {@link HttpSecuritySource}.
	 * 
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @return {@link PropertyList} of the {@link HttpSecuritySourceProperty}
	 *         instances of the {@link HttpSecuritySourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<S, C, D extends Enum<D>, F extends Enum<F>, HS extends HttpSecuritySource<S, C, D, F>> PropertyList loadSpecification(
			Class<HS> httpSecuritySourceClass);

	/**
	 * Loads and returns the {@link HttpSecurityType} for the
	 * {@link HttpSecuritySource}.
	 * 
	 * @param httpSecuritySourceClass
	 *            Class of the {@link HttpSecuritySource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link HttpSecurityType}.
	 * @return {@link HttpSecurityType} or <code>null</code> if issues, which
	 *         are reported to the {@link CompilerIssues}.
	 */
	<S, C, D extends Enum<D>, F extends Enum<F>, HS extends HttpSecuritySource<S, C, D, F>> HttpSecurityType<S, C, D, F> loadHttpSecurityType(
			Class<HS> httpSecuritySourceClass, PropertyList propertyList);

	/**
	 * Loads and returns the {@link HttpSecurityType} for the
	 * {@link HttpSecuritySource}.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource} instance to use.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link HttpSecurityType}.
	 * @return {@link HttpSecurityType} or <code>null</code> if issues, which
	 *         are reported to the {@link CompilerIssues}.
	 */
	<S, C, D extends Enum<D>, F extends Enum<F>> HttpSecurityType<S, C, D, F> loadHttpSecurityType(
			HttpSecuritySource<S, C, D, F> httpSecuritySource,
			PropertyList propertyList);

}