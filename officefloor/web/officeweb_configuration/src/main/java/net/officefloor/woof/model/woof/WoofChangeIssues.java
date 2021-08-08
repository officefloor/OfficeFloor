/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;

/**
 * Allows the WoOF {@link Change} to report an issue when it is
 * applying/reverting.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofChangeIssues {

	/**
	 * Adds an issue.
	 * 
	 * @param message
	 *            Message.
	 */
	void addIssue(String message);

	/**
	 * Adds an issue.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	void addIssue(String message, Throwable cause);

}
