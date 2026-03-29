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

/**
 * Team of workers to execute the assigned {@link Job} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface Team {

	/**
	 * Indicates for the {@link Team} to start working.
	 */
	void startWorking();

	/**
	 * Assigns a {@link Job} to be executed by this {@link Team}.
	 * 
	 * @param job {@link Job}.
	 * @throws TeamOverloadException Indicating the {@link Team} is overloaded and
	 *                               that back pressure should be applied to
	 *                               gracefully handle overload.
	 * @throws Exception             For other {@link Exception} instances to again
	 *                               indicate back pressure.
	 */
	void assignJob(Job job) throws TeamOverloadException, Exception;

	/**
	 * <p>
	 * Indicates for the {@link Team} to stop working.
	 * <p>
	 * This method should block and only return control when the {@link Team} has
	 * stopped working and is no longer assigned {@link Job} instances to complete.
	 */
	void stopWorking();

}
