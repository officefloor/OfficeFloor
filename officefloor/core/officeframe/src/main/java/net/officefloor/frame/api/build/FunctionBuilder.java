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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Builds a {@link FunctionState} provides linking to other
 * {@link FunctionState} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionBuilder<F extends Enum<F>> {

	/**
	 * <p>
	 * Specifies the {@link Team} by its {@link Office} registered name that that is
	 * responsible for this node.
	 * <p>
	 * Should this not be specified, any {@link Team} will be used.
	 * 
	 * @param officeTeamName
	 *            Name of the {@link Team} within the {@link Office}.
	 */
	void setResponsibleTeam(String officeTeamName);

	/**
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction} of
	 * the {@link Flow}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow}.
	 * @param functionName
	 *            Name of the initial {@link ManagedFunction} for the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}. May be
	 *            <code>null</code> to indicate no argument.
	 * @param isSpawnThreadState
	 *            <code>true</code> to instigate the {@link Flow} in a spawned
	 *            {@link ThreadState}.
	 */
	void linkFlow(F key, String functionName, Class<?> argumentType, boolean isSpawnThreadState);

	/**
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction} of
	 * the {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow}.
	 * @param functionName
	 *            Name of the initial {@link ManagedFunction} for the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}. May be
	 *            <code>null</code> to indicate no argument.
	 * @param isSpawnThreadState
	 *            <code>true</code> to instigate the {@link Flow} in a spawned
	 *            {@link ThreadState}.
	 */
	void linkFlow(int flowIndex, String functionName, Class<?> argumentType, boolean isSpawnThreadState);

	/**
	 * <p>
	 * Adds an {@link EscalationFlow} to the {@link EscalationProcedure} for the
	 * {@link ManagedFunction}.
	 * <p>
	 * The order in which the {@link EscalationFlow} instances are added is the
	 * order in which they are checked for handling escalation. Only one
	 * {@link EscalationFlow} is used to handle escalation and the first one
	 * covering the cause will be used. This is similar to
	 * <code>try ... catch</code> blocks.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link EscalationFlow}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction} to handle the
	 *            {@link Escalation}.
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause, String functionName);

}
