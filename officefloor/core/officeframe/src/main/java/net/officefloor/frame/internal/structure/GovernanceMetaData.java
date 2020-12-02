/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;

/**
 * Meta-data of the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceMetaData<E, F extends Enum<F>> extends ManagedFunctionLogicMetaData {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? super E, F> getGovernanceFactory();

	/**
	 * Creates the {@link GovernanceContainer}.
	 * 
	 * @param threadState     {@link ThreadState}.
	 * @param governanceIndex Index of the {@link Governance} within the
	 *                        {@link ThreadState}.
	 * @return {@link GovernanceContainer}.
	 */
	GovernanceContainer<E> createGovernanceContainer(ThreadState threadState, int governanceIndex);

	/**
	 * Creates the {@link ManagedFunctionContainer} for the
	 * {@link GovernanceActivity}.
	 * 
	 * @param activity {@link GovernanceActivity}.
	 * @return {@link ManagedFunctionLogic} for the {@link GovernanceActivity}.
	 */
	ManagedFunctionLogic createGovernanceFunctionLogic(GovernanceActivity<F> activity);

}
