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
package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAccessControl;

/**
 * Factory for the creation of the {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAccessControlFactory<AC extends Serializable> {

	/**
	 * Creates {@link HttpAccessControl} from the custom access control.
	 * 
	 * @param accessControl
	 *            Custom access control.
	 * @return {@link HttpAccessControl} adapting the custom access control.
	 */
	HttpAccessControl createHttpAccessControl(AC accessControl) throws HttpException;

}