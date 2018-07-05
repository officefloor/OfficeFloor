/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.build;

import net.officefloor.web.accept.AcceptNegotiator;

/**
 * Builds the {@link AcceptNegotiator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AcceptNegotiatorBuilder<H> {

	/**
	 * Adds a handler.
	 * 
	 * @param contentType
	 *            <code>Content-Type</code> handled by the handler. This may
	 *            include wild cards. For example: <code>image/*</code>
	 * @param handler
	 *            Handler.
	 */
	void addHandler(String contentType, H handler);

	/**
	 * Builds the {@link AcceptNegotiator}.
	 * 
	 * @return {@link AcceptNegotiator}.
	 * @throws NoAcceptHandlersException
	 *             If no handlers configured.
	 */
	AcceptNegotiator<H> build() throws NoAcceptHandlersException;

}