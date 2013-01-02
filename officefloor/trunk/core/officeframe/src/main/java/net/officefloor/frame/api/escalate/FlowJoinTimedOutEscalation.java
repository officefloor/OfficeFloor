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
package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.JobSequence;

/**
 * {@link Escalation} of a {@link JobSequence} not completing in the timeout of a
 * {@link Task} joining to it.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowJoinTimedOutEscalation extends Escalation {

	/**
	 * Token provided to the join on the {@link JobSequence}.
	 */
	private final Object token;

	/**
	 * Initiate.
	 * 
	 * @param token
	 *            Token provided to the join on the {@link JobSequence}.
	 */
	public FlowJoinTimedOutEscalation(Object token) {
		this.token = token;
	}

	/**
	 * Obtains the token provided to the join on the {@link JobSequence}.
	 * 
	 * @return Token provided to the join on the {@link JobSequence}.
	 */
	public Object getToken() {
		return this.token;
	}

}