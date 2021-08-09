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

package net.officefloor.compile.governance;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.governance.source.GovernanceSourceProperty;
import net.officefloor.compile.spi.governance.source.GovernanceSourceSpecification;

/**
 * Loads the {@link GovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link GovernanceSourceSpecification} for the {@link GovernanceSource}.
	 * 
	 * @param <I>                   Extension interface type.
	 * @param <F>                   Flow key type.
	 * @param <GS>                  {@link GovernanceSource} type.
	 * @param governanceSourceClass Class of the {@link GovernanceSource}.
	 * @return {@link PropertyList} of the {@link GovernanceSourceProperty}
	 *         instances of the {@link GovernanceSourceSpecification} or
	 *         <code>null</code> if issue, which is reported to the
	 *         {@link CompilerIssues}.
	 */
	<I, F extends Enum<F>, GS extends GovernanceSource<I, F>> PropertyList loadSpecification(
			Class<GS> governanceSourceClass);

	/**
	 * Loads and returns the {@link GovernanceType} from the
	 * {@link GovernanceSource} class.
	 * 
	 * @param <I>                   Extension interface type.
	 * @param <F>                   Flow key type.
	 * @param <GS>                  {@link GovernanceSource} type.
	 * @param governanceSourceClass Class of the {@link GovernanceSource}.
	 * @param properties            {@link PropertyList} containing the properties
	 *                              to source the {@link GovernanceType}.
	 * @return {@link GovernanceType} or <code>null</code> if issues, which is
	 *         reported to the {@link CompilerIssues}.
	 */
	<I, F extends Enum<F>, GS extends GovernanceSource<I, F>> GovernanceType<I, F> loadGovernanceType(
			Class<GS> governanceSourceClass, PropertyList properties);

	/**
	 * Loads and returns the {@link GovernanceType} from the
	 * {@link GovernanceSource}.
	 * 
	 * @param <I>              Extension interface type.
	 * @param <F>              Flow key type.
	 * @param <GS>             {@link GovernanceSource} type.
	 * @param governanceSource {@link GovernanceSource}.
	 * @param properties       {@link PropertyList} containing the properties to
	 *                         source the {@link GovernanceType}.
	 * @return {@link GovernanceType} or <code>null</code> if issues, which is
	 *         reported to the {@link CompilerIssues}.
	 */
	<I, F extends Enum<F>, GS extends GovernanceSource<I, F>> GovernanceType<I, F> loadGovernanceType(
			GS governanceSource, PropertyList properties);

}
