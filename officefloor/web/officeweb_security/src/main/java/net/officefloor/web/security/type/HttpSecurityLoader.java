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
package net.officefloor.web.security.type;

import java.io.Serializable;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceProperty;
import net.officefloor.web.spi.security.HttpSecuritySourceSpecification;

/**
 * Loads the {@link HttpSecurityType} from the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link HttpSecuritySourceSpecification} for the {@link HttpSecuritySource}.
	 *
	 * @param <A>
	 *            Authentication type.
	 * @param <AC>
	 *            Access control type.
	 * @param <C>
	 *            Credentials type.
	 * @param <O>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param <S>
	 *            {@link HttpSecuritySource} type.
	 * @param httpSecuritySourceClass
	 *            {@link HttpSecuritySource} {@link Class}.
	 * @return {@link PropertyList} of the {@link HttpSecuritySourceProperty}
	 *         instances of the {@link HttpSecuritySourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, S extends HttpSecuritySource<A, AC, C, O, F>> PropertyList loadSpecification(
			Class<S> httpSecuritySourceClass);

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link HttpSecuritySourceSpecification} for the {@link HttpSecuritySource}.
	 *
	 * @param <A>
	 *            Authentication type.
	 * @param <AC>
	 *            Access control type.
	 * @param <C>
	 *            Credentials type.
	 * @param <O>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 * @return {@link PropertyList} of the {@link HttpSecuritySourceProperty}
	 *         instances of the {@link HttpSecuritySourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> PropertyList loadSpecification(
			HttpSecuritySource<A, AC, C, O, F> httpSecuritySource);

	/**
	 * <p>
	 * Loads and returns the {@link HttpSecurityType} for the
	 * {@link HttpSecuritySource}.
	 * <p>
	 * This method will also initialise the {@link HttpSecuritySource}.
	 * 
	 * @param <A>
	 *            Authentication type.
	 * @param <AC>
	 *            Access control type.
	 * @param <C>
	 *            Credentials type.
	 * @param <O>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param <S>
	 *            {@link HttpSecuritySource} type.
	 * @param httpSecuritySourceClass
	 *            {@link HttpSecuritySource} {@link Class}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link HttpSecurityType}.
	 * @return {@link HttpSecurityType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, S extends HttpSecuritySource<A, AC, C, O, F>> HttpSecurityType<A, AC, C, O, F> loadHttpSecurityType(
			Class<S> httpSecuritySourceClass, PropertyList propertyList);

	/**
	 * <p>
	 * Loads and returns the {@link HttpSecurityType} for the
	 * {@link HttpSecuritySource}.
	 * <p>
	 * This method will also initialise the {@link HttpSecuritySource}.
	 * 
	 * @param <A>
	 *            Authentication type.
	 * @param <AC>
	 *            Access control type.
	 * @param <C>
	 *            Credentials type.
	 * @param <O>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link HttpSecurityType}.
	 * @return {@link HttpSecurityType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityType<A, AC, C, O, F> loadHttpSecurityType(
			HttpSecuritySource<A, AC, C, O, F> httpSecuritySource, PropertyList propertyList);

}