/*-
 * #%L
 * OfficeCompiler
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
