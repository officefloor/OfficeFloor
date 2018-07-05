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

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceType<E, F extends Enum<F>> {

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? extends E, F> getGovernanceFactory();

	/**
	 * Obtains the extension type that the {@link ManagedObject} instances are to
	 * provide to be enable {@link Governance} over them.
	 * 
	 * @return Extension type that the {@link ManagedObject} instances are to
	 *         provide to be enable {@link Governance} over them.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the {@link GovernanceFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link GovernanceActivity}.
	 * 
	 * @return {@link GovernanceFlowType} definitions for the possible {@link Flow}
	 *         instances instigated by the {@link GovernanceActivity}.
	 */
	GovernanceFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link GovernanceEscalationType} definitions for the possible
	 * {@link EscalationFlow} instances by the {@link GovernanceActivity}.
	 * 
	 * @return {@link GovernanceEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link GovernanceActivity}.
	 */
	GovernanceEscalationType[] getEscalationTypes();

}