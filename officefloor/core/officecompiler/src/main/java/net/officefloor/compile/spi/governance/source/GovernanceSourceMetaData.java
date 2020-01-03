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

package net.officefloor.compile.spi.governance.source;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Meta-data of the {@link GovernanceSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceSourceMetaData<I, F extends Enum<F>> {

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? extends I, F> getGovernanceFactory();

	/**
	 * Obtains the {@link Class} that the {@link ManagedObject} must provide as
	 * an extension interface to be governed.
	 * 
	 * @return Extension interface for the {@link ManagedObject}.
	 */
	Class<I> getExtensionInterface();

	/**
	 * Obtains the list of {@link GovernanceFlowMetaData} instances should this
	 * {@link GovernanceSource} require instigating a {@link Flow}.
	 * 
	 * @return Meta-data of {@link Flow} instances instigated by this
	 *         {@link GovernanceSource}.
	 */
	GovernanceFlowMetaData<F>[] getFlowMetaData();

	/**
	 * Obtains the potential {@link Escalation} types from the
	 * {@link Governance}.
	 * 
	 * @return Potential {@link Escalation} types from the {@link Governance}.
	 */
	Class<?>[] getEscalationTypes();

}
