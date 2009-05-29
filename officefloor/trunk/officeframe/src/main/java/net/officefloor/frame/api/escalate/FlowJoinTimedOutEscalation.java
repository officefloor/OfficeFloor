/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link Escalation} of a {@link Flow} not completing in the timeout of a
 * {@link Task} joining to it.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowJoinTimedOutEscalation extends Escalation {

	/**
	 * Token provided to the join on the {@link Flow}.
	 */
	private final Object token;

	/**
	 * Initiate.
	 * 
	 * @param token
	 *            Token provided to the join on the {@link Flow}.
	 */
	public FlowJoinTimedOutEscalation(Object token) {
		this.token = token;
	}

	/**
	 * Obtains the token provided to the join on the {@link Flow}.
	 * 
	 * @return Token provided to the join on the {@link Flow}.
	 */
	public Object getToken() {
		return this.token;
	}

}