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
package net.officefloor.frame.integrate.stress;

import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFunctionBuilder.ReflectiveFunctionBuilder;

/**
 * Stress test for managed {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedGovernanceStressTest extends
		AbstractGovernanceStressTestCase {

	/**
	 * Ensures no issues arising in stress {@link Governance} with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressGovernance_OnePersonTeam() throws Throwable {
		this.doTest(new OnePersonTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 100));
	}

	/**
	 * Ensures no issues arising in stress {@link Governance} with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressGovernance_LeaderFollowerTeam() throws Throwable {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 3, 100));
	}

	/**
	 * Ensures no issues arising in stress {@link Governance} with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressGovernance_ExecutorFixedTeam() throws Throwable {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST",
				MockTeamSource.createTeamIdentifier(), 3));
	}

	@Override
	protected boolean configure(ReflectiveFunctionBuilder commitTask,
			ReflectiveFunctionBuilder rollbackTask, ReflectiveFunctionBuilder tidyUpTask) {

		// Configure governance on tasks
		commitTask.getBuilder().addGovernance(GOVERNANCE_NAME);
		rollbackTask.getBuilder().addGovernance(GOVERNANCE_NAME);
		tidyUpTask.getBuilder().addGovernance(GOVERNANCE_NAME);

		// Managed Governance
		return true;
	}

}