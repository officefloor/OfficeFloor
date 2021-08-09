/*-
 * #%L
 * OfficeFrame
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
