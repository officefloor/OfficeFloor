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
package net.officefloor.frame.impl.execute.administrator;

import java.util.List;

import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.GovernanceManager;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link GovernanceManager} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceManagerImpl implements GovernanceManager {

	/**
	 * {@link AdministratorContext}.
	 */
	private final AdministratorContext adminContext;

	/**
	 * Index of {@link Governance} within the {@link ThreadState}.
	 */
	private final int governanceIndex;

	/**
	 * Listing of the actioned {@link Governance}.
	 */
	private final List<FunctionState> actionedGovernances;

	/**
	 * Initiate.
	 * 
	 * @param adminContext
	 *            {@link AdministratorContext}. {@link GovernanceContainer}.
	 * @param governanceIndex
	 *            Index of {@link Governance} within the {@link ThreadState}.
	 * @param actionedGovernances
	 *            Listing of the actioned {@link Governance}.
	 */
	public GovernanceManagerImpl(AdministratorContext adminContext, int governanceIndex,
			List<FunctionState> actionedGovernances) {
		this.adminContext = adminContext;
		this.governanceIndex = governanceIndex;
		this.actionedGovernances = actionedGovernances;
	}

	/*
	 * ===================== GovernanceManager =====================
	 */

	@Override
	public void activateGovernance() {
		FunctionState activate = this.getGovernanceContainer().activateGovernance();
		this.actionedGovernances.add(activate);
	}

	@Override
	public void enforceGovernance() {
		FunctionState enforce = this.getGovernanceContainer().enforceGovernance();
		this.actionedGovernances.add(enforce);
	}

	@Override
	public void disregardGovernance() {
		FunctionState disregard = this.getGovernanceContainer().disregardGovernance();
		this.actionedGovernances.add(disregard);

	}

	/**
	 * Obtains the {@link GovernanceContainer}.
	 * 
	 * @return {@link GovernanceContainer}.
	 */
	private GovernanceContainer<?, ?> getGovernanceContainer() {

		// Obtain the governance container
		GovernanceContainer<?, ?> container = this.adminContext.getThreadState()
				.getGovernanceContainer(this.governanceIndex);

		// Return the governance container
		return container;
	}

}