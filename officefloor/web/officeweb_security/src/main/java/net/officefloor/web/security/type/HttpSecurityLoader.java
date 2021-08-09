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
