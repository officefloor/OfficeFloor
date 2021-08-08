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

package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * <p>
 * {@link Escalation} from managing a {@link ManagedFunction}.
 * <p>
 * This enables generic handling of {@link ManagedFunction} {@link Escalation}
 * failures.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ManagedFunctionEscalation extends Escalation {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String managedFunctionName;

	/**
	 * Initiate.
	 * 
	 * @param managedFunctionName Name of the {@link ManagedFunction}.
	 */
	public ManagedFunctionEscalation(String managedFunctionName) {
		this.managedFunctionName = managedFunctionName;
	}

	/**
	 * Allows for a cause of the {@link Escalation}.
	 * 
	 * @param managedFunctionName Name of the {@link ManagedFunction}.
	 * @param cause               Cause of the {@link Escalation}.
	 */
	public ManagedFunctionEscalation(String managedFunctionName, Throwable cause) {
		super(cause);
		this.managedFunctionName = managedFunctionName;
	}

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	public String getManagedFunctionName() {
		return this.managedFunctionName;
	}

}
