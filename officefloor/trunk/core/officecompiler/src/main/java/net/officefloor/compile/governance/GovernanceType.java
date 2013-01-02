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
package net.officefloor.compile.governance;

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceType<I, F extends Enum<F>> {

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? extends I, F> getGovernanceFactory();

	/**
	 * Obtains the extension interface that the {@link ManagedObject} instances
	 * are to provide to be enable {@link Governance} over them.
	 * 
	 * @return Extension interface that the {@link ManagedObject} instances are
	 *         to provide to be enable {@link Governance} over them.
	 */
	Class<I> getExtensionInterface();

	/**
	 * Obtains the {@link GovernanceFlowType} definitions for the possible
	 * {@link JobSequence} instances instigated by the
	 * {@link GovernanceActivity}.
	 * 
	 * @return {@link GovernanceFlowType} definitions for the possible
	 *         {@link JobSequence} instances instigated by the
	 *         {@link GovernanceActivity}.
	 */
	GovernanceFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link GovernanceEscalationType} definitions for the possible
	 * {@link EscalationFlow} instances by the {@link GovernanceActivity}.
	 * 
	 * @return {@link GovernanceEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the
	 *         {@link GovernanceActivity}.
	 */
	GovernanceEscalationType[] getEscalationTypes();

}