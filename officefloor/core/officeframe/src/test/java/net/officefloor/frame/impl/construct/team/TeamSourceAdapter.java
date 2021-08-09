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

package net.officefloor.frame.impl.construct.team;

import junit.framework.TestCase;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.TeamSourceSpecification;

/**
 * Adapter providing empty {@link TeamSource} methods.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class TeamSourceAdapter implements TeamSource, Team {

	/*
	 * ==================== TeamSource ==================================
	 */

	@Override
	public TeamSourceSpecification getSpecification() {
		return null;
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {
		return this;
	}

	/*
	 * ==================== TeamSource ==================================
	 */

	@Override
	public void startWorking() {
		TestCase.fail("Should not be invoked");
	}

	@Override
	public void assignJob(Job job) {
		TestCase.fail("Should not be invoked");
	}

	@Override
	public void stopWorking() {
		TestCase.fail("Should not be invoked");
	}

}
