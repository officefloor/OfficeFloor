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
package net.officefloor.compile.test.administration;

import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.compile.administration.AdministrationGovernanceType;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Builder of the {@link AdministrationType} to validate the loaded
 * {@link AdministrationType} from the {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationTypeBuilder<F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Adds a {@link AdministrationFlowType}.
	 * 
	 * @param flowName
	 *            Name of the {@link Flow}.
	 * @param argumentType
	 *            Argument type.
	 * @param index
	 *            Index of the {@link Flow}.
	 * @param flowKey
	 *            Key of the {@link Flow}.
	 */
	void addFlow(String flowName, Class<?> argumentType, int index, F flowKey);

	/**
	 * Adds an {@link AdministrationEscalationType}.
	 * 
	 * @param escalationName
	 *            Name of {@link AdministrationEscalationType}.
	 * @param escalationType
	 *            Type of {@link Escalation}.
	 */
	void addEscalation(String escalationName, Class<? extends Throwable> escalationType);

	/**
	 * Adds an {@link AdministrationGovernanceType}.
	 * 
	 * @param governanceName
	 *            Name of {@link Governance}.
	 * @param index
	 *            Index of the {@link Governance}.
	 * @param governanceKey
	 *            Key of the {@link Governance}.
	 */
	void addGovernance(String governanceName, int index, G governanceKey);

}