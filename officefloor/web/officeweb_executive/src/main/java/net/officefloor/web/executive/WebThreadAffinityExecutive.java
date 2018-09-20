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
package net.officefloor.web.executive;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.internal.structure.Execution;

/**
 * Web {@link Thread} affinity {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebThreadAffinityExecutive implements Executive {

	/*
	 * =================== Executive ======================
	 */

	@Override
	public Object createProcessIdentifier() {
		// TODO Auto-generated method stub
		return Executive.super.createProcessIdentifier();
	}

	@Override
	public <T extends Throwable> void manageExecution(Execution<T> execution) throws T {
		// TODO Auto-generated method stub
		Executive.super.manageExecution(execution);
	}

	@Override
	public ExecutionStrategy[] getExcutionStrategies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TeamOversight[] getTeamOversights() {
		// TODO Auto-generated method stub
		return Executive.super.getTeamOversights();
	}

}