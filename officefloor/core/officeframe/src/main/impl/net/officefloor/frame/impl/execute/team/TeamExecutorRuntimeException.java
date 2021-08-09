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

package net.officefloor.frame.impl.execute.team;

/**
 * {@link TeamExecutor} {@link RuntimeException}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamExecutorRuntimeException extends RuntimeException {

	/**
	 * Serialise identifier.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 * 
	 * @param cause Cause.
	 */
	public TeamExecutorRuntimeException(Throwable cause) {
		super(cause);
	}
}
