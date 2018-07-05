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
package net.officefloor.web.session.spi;

import net.officefloor.web.session.HttpSession;

/**
 * <p>
 * Generates the {@link HttpSession} Id.
 * <p>
 * Typically a default {@link HttpSessionIdGenerator} is provided by the
 * {@link HttpSession} and this need not be provided. This interface however
 * enables customising the generation of the {@link HttpSession} Id.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpSessionIdGenerator {

	/**
	 * <p>
	 * Generates the session Id.
	 * <p>
	 * This method may return without the session Id being specified on the
	 * {@link FreshHttpSession}. In this case it is expected that the session Id
	 * will be populated some time in the near future.
	 *
	 * @param session
	 *            {@link FreshHttpSession} to be populated with a new session
	 *            Id.
	 */
	void generateSessionId(FreshHttpSession session);

}