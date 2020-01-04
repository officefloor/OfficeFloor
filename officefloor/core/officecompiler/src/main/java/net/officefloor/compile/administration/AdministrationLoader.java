/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.administration;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceProperty;
import net.officefloor.compile.spi.administration.source.AdministrationSourceSpecification;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Loads the {@link AdministrationType} from the {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link AdministrationSourceSpecification} for the
	 * {@link AdministrationSource}.
	 * 
	 * @param <E>                       Extension interface type.
	 * @param <F>                       {@link Enum} for the {@link Flow} keys.
	 * @param <G>                       {@link Enum} for the {@link Governance}
	 *                                  keys.
	 * @param <AS>                      {@link AdministrationSource} type.
	 * @param administrationSourceClass {@link AdministrationSource} class.
	 * @return {@link PropertyList} of the {@link AdministrationSourceProperty}
	 *         instances of the {@link AdministrationSourceSpecification} or
	 *         <code>null</code> if issues, which are reported to the
	 *         {@link CompilerIssues}.
	 */
	<E, F extends Enum<F>, G extends Enum<G>, AS extends AdministrationSource<E, F, G>> PropertyList loadSpecification(
			Class<AS> administrationSourceClass);

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link AdministrationSourceSpecification} for the
	 * {@link AdministrationSource}.
	 * 
	 * @param <E>                  Extension interface type.
	 * @param <F>                  {@link Enum} for the {@link Flow} keys.
	 * @param <G>                  {@link Enum} for the {@link Governance} keys.
	 * @param administrationSource {@link AdministrationSource} instance.
	 * @return {@link PropertyList} of the {@link AdministrationSourceProperty}
	 *         instances of the {@link AdministrationSourceSpecification} or
	 *         <code>null</code> if issues, which are reported to the
	 *         {@link CompilerIssues}.
	 */
	<E, F extends Enum<F>, G extends Enum<G>> PropertyList loadSpecification(
			AdministrationSource<E, F, G> administrationSource);

	/**
	 * Loads and returns the {@link AdministrationType} sourced from the
	 * {@link AdministrationSource}.
	 * 
	 * @param <E>                       Extension interface type.
	 * @param <F>                       {@link Enum} for the {@link Flow} keys.
	 * @param <G>                       {@link Enum} for the {@link Governance}
	 *                                  keys.
	 * @param <AS>                      {@link AdministrationSource} type.
	 * @param administrationSourceClass Class of the {@link AdministrationSource}.
	 * @param propertyList              {@link PropertyList} containing the
	 *                                  properties to source the
	 *                                  {@link AdministrationType}.
	 * @return {@link AdministrationType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<E, F extends Enum<F>, G extends Enum<G>, AS extends AdministrationSource<E, F, G>> AdministrationType<E, F, G> loadAdministrationType(
			Class<AS> administrationSourceClass, PropertyList propertyList);

	/**
	 * Loads and returns the {@link AdministrationType} sourced from the
	 * {@link AdministrationSource}.
	 * 
	 * @param <E>                  Extension interface type.
	 * @param <F>                  {@link Enum} for the {@link Flow} keys.
	 * @param <G>                  {@link Enum} for the {@link Governance} keys.
	 * @param administrationSource {@link AdministrationSource} instance.
	 * @param propertyList         {@link PropertyList} containing the properties to
	 *                             source the {@link AdministrationType}.
	 * @return {@link AdministrationType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<E, F extends Enum<F>, G extends Enum<G>> AdministrationType<E, F, G> loadAdministrationType(
			AdministrationSource<E, F, G> administrationSource, PropertyList propertyList);

}
