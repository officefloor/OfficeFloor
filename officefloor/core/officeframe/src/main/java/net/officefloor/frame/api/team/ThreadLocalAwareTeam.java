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

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Enables a {@link Team} to execute the {@link Job} with the {@link Thread}
 * invoking the {@link ProcessState}.
 * <p>
 * An example use is for embedding {@link OfficeFloor} within an Application
 * Server and associating the {@link Thread} invoking the {@link ProcessState}
 * for {@link ThreadLocal} instances of the Application Server.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalAwareTeam extends Team {

	/**
	 * <p>
	 * Indicates if {@link ThreadLocalAwareTeam}.
	 * <p>
	 * Allows for implementing the interface without being thread-local aware.
	 * 
	 * @return <code>true</code> if {@link ThreadLocalAwareTeam}.
	 */
	default boolean isThreadLocalAware() {
		return true;
	}

	/**
	 * Sets the {@link ThreadLocalAwareContext} for the {@link Team}.
	 * 
	 * @param context {@link ThreadLocalAwareContext} for the {@link Team}.
	 */
	void setThreadLocalAwareness(ThreadLocalAwareContext context);

}
