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

import java.util.concurrent.RejectedExecutionException;

/**
 * <p>
 * Indicates the {@link Team} is overloaded.
 * <p>
 * By convention {@link Team} instances should throw this to indicate back
 * pressure, as load on the {@link Team} is too high.
 * <p>
 * This is similar to {@link RejectedExecutionException}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamOverloadException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 * @param cause   Cause.
	 */
	public TeamOverloadException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 */
	public TeamOverloadException(String message) {
		super(message);
	}

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public TeamOverloadException(Throwable cause) {
		super(cause);
	}

}
