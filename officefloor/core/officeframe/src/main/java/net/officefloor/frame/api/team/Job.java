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

package net.officefloor.frame.api.team;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link Job} executed by a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Job extends Runnable {

	/**
	 * <p>
	 * Obtains the {@link ProcessIdentifier} for the {@link ProcessState} containing
	 * this {@link Job}.
	 * <p>
	 * This allows the {@link Team} executing the {@link Job} to be aware of the
	 * {@link ProcessState} context in which the {@link Job} is to be executed. This
	 * is particular relevant for {@link TeamOversight} provided by the
	 * {@link Executive}.
	 * <p>
	 * An example use would be embedding {@link OfficeFloor} within an Application
	 * Server and using this {@link ProcessIdentifier} and a
	 * {@link ThreadLocalAwareTeam} to know the invoking {@link Thread} for
	 * interaction with {@link ThreadLocal} instances of the Application Server.
	 * 
	 * @return Identifier for the {@link ProcessState} containing this {@link Job}
	 * 
	 * @see ThreadLocalAwareTeam
	 */
	ProcessIdentifier getProcessIdentifier();

	/**
	 * Enables a {@link Team} to cancel the {@link Job} should it be overloaded with
	 * {@link Job} instances.
	 * 
	 * @param cause Reason by {@link Team} for canceling the {@link Job}.
	 */
	void cancel(Throwable cause);

}
