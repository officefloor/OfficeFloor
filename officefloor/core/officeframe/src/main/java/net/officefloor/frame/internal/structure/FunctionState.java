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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.spi.team.Team;

/**
 * Node within the graph of {@link FunctionState} instances to execute.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionState extends LinkedListSetEntry<FunctionState, Flow> {

	@Override
	default Flow getLinkedListSetOwner() {
		throw new IllegalStateException(this.getClass().getName()
				+ " must override getLinkedListSetOwner to be added to a " + LinkedListSet.class.getName());
	}

	/**
	 * <p>
	 * Obtains the {@link TeamManagement} responsible for this
	 * {@link FunctionState}.
	 * <p>
	 * By default, {@link FunctionState} may be executed by any
	 * {@link TeamManagement}.
	 * 
	 * @return {@link TeamManagement} responsible for this
	 *         {@link FunctionState}. May be <code>null</code> to indicate any
	 *         {@link Team} may execute the {@link FunctionState}.
	 */
	default TeamManagement getResponsibleTeam() {
		return null; // any team by default
	}

	/**
	 * <p>
	 * Obtains the {@link ThreadState} for this {@link FunctionState}.
	 * <p>
	 * This provides access to the {@link ThreadState} that this
	 * {@link FunctionState} resides within.
	 * 
	 * @return {@link ThreadState} for this {@link FunctionState}.
	 */
	ThreadState getThreadState();

	/**
	 * Indicates if the {@link FunctionState} requires {@link ThreadState}
	 * safety.
	 * 
	 * @return <code>true</code> should {@link FunctionState} require
	 *         {@link ThreadState} safety.
	 */
	default boolean isRequireThreadStateSafety() {
		return false; // no thread safety required by default
	}

	/**
	 * Executes the {@link FunctionState}.
	 * 
	 * @return Next {@link FunctionState} to be executed. May be
	 *         <code>null</code> to indicate no further {@link FunctionState}
	 *         instances to execute.
	 * @throws Throwable
	 *             Possible failure of {@link FunctionState} logic.
	 */
	FunctionState execute() throws Throwable;

	/**
	 * Cancels this {@link FunctionState} returning an optional
	 * {@link FunctionState} to clean up this {@link FunctionState}.
	 * 
	 * @return Optional clean up {@link FunctionState}. May be
	 *         <code>null</code>.
	 */
	default FunctionState cancel() {
		return null; // no clean up by default
	}

	/**
	 * Handles {@link Escalation} from the {@link ManagedFunction}.
	 * 
	 * @param escalation
	 *            {@link Escalation}.
	 * @return Optional {@link FunctionState} to handle the {@link Escalation}.
	 */
	default FunctionState handleEscalation(Throwable escalation) {
		return this.getThreadState().handleEscalation(escalation);
	}

}