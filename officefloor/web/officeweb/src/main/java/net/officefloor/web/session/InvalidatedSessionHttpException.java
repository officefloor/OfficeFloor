/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.session;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;

/**
 * Indicates the {@link HttpSession} is currently invalidated and can not be
 * used. This can occur:
 * <ol>
 * <li>after the {@link HttpSession} has been invalidated with no further
 * {@link HttpSession} required (in other words not creating another
 * {@link HttpSession})</li>
 * <li>during {@link HttpSession} invalidation as another {@link HttpSession} is
 * being created</li>
 * <li>failure in invalidating the {@link HttpSession}</li>
 * </ol>
 *
 * @author Daniel Sagenschneider
 */
public class InvalidatedSessionHttpException extends HttpException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 */
	public InvalidatedSessionHttpException() {
		super(HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Initiate with cause.
	 *
	 * @param cause Cause of {@link HttpSession} being invalid.
	 */
	public InvalidatedSessionHttpException(Throwable cause) {
		super(HttpStatus.UNAUTHORIZED, cause);
	}

}
