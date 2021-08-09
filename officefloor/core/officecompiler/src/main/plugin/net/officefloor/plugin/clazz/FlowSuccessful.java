/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link FlowCallback} that propagates failures and only handles success.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FlowSuccessful extends FlowCallback {

	/**
	 * Default implementation of {@link FlowCallback} to escalate and then invoke
	 * successful handling.
	 */
	default void run(Throwable escalation) throws Throwable {

		// Ensure propagate flow failure
		if (escalation != null) {
			throw escalation;
		}

		// Successful flow
		this.run();
	}

	/**
	 * Invoked on completion of successful {@link Flow}.
	 * 
	 * @throws Throwable Possible failure in handling completion.
	 */
	void run() throws Throwable;
}
