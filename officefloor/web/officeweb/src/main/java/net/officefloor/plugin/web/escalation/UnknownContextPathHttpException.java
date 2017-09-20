/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.escalation;

import net.officefloor.server.http.HttpRequest;

/**
 * Indicates the context path for the {@link HttpRequest} is incorrect for the
 * application.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownContextPathHttpException extends NotFoundHttpException {

	/**
	 * Initiate.
	 * 
	 * @param expectedContextPath
	 *            Expected context path for the application.
	 * @param requestUri
	 *            Request URI.
	 */
	public UnknownContextPathHttpException(String expectedContextPath, String requestUri) {
		super("Incorrect context path for application [context=" + expectedContextPath + ", request=" + requestUri
				+ "]");
	}

}