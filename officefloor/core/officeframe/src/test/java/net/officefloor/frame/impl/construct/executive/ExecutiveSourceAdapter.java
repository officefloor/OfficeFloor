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
package net.officefloor.frame.impl.construct.executive;

import junit.framework.TestCase;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;

/**
 * Adapter providing empty {@link ExecutiveSource} methods.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class ExecutiveSourceAdapter implements ExecutiveSource, Executive {

	/*
	 * =============== ExecutiveSource =====================
	 */

	@Override
	public ExecutiveSourceSpecification getSpecification() {
		return null;
	}

	@Override
	public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
		return this;
	}

	/*
	 * ================== Executive ========================
	 */

	@Override
	public Object createProcessIdentifier() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public ExecutionStrategy[] getExcutionStrategies() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public Team createTeam(TeamSource teamSource, TeamSourceContext context) throws Exception {
		TestCase.fail("Should not be invoked");
		return null;
	}

}
