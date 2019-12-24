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