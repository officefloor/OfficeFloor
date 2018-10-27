/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * {@link TeamSource} that causes back pressure to be applied.
 * 
 * @author Daniel Sagenschneider
 */
public class BackPressureTeamSource extends AbstractTeamSource implements Team {

	/**
	 * {@link RejectedExecutionException} thrown to indicate back pressure.
	 */
	public static final RejectedExecutionException BACK_PRESSURE_EXCEPTION = new RejectedExecutionException(
			"Testing back pressure from overloaded Team");

	/**
	 * Indicates the number of back pressure failures.
	 */
	private static final AtomicInteger backPressureFailures = new AtomicInteger(0);

	/**
	 * Obtains the number of back pressure {@link Escalation} instances that have
	 * occurred.
	 * 
	 * @return Number of back pressure {@link Escalation} instances that have
	 *         occurred.
	 */
	public static int getBackPressureEscalationCount() {
		return backPressureFailures.get();
	}

	/**
	 * Resets the back pressure {@link Escalation} count.
	 * 
	 * @param count Count to reset the {@link Escalation} count.
	 */
	public static void resetBackPressureEscalationCount(int count) {
		backPressureFailures.set(count);
	}

	/*
	 * ============== TeamSource =================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {
		return this;
	}

	/*
	 * ================ Team ======================
	 */

	@Override
	public void startWorking() {
		// nothing to start
	}

	@Override
	public void assignJob(Job job) {

		// Increment the number of back pressure failures
		backPressureFailures.incrementAndGet();

		// Always applies back pressure
		throw BACK_PRESSURE_EXCEPTION;
	}

	@Override
	public void stopWorking() {
		// nothing to stop
	}

}
