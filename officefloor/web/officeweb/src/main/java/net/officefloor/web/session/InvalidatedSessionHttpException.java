/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
