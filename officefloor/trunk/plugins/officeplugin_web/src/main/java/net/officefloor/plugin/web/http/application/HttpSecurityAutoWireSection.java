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
package net.officefloor.plugin.web.http.application;

import net.officefloor.autowire.AutoWireSection;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

/**
 * Allows wiring the flows of the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityAutoWireSection extends AutoWireSection {

	/**
	 * Obtains the {@link Class} of the {@link HttpSecuritySource}.
	 * 
	 * @return {@link Class} of the {@link HttpSecuritySource}.
	 */
	Class<? extends HttpSecuritySource<?, ?, ?, ?>> getHttpSecuritySourceClass();

	/**
	 * Obtains the time in milliseconds before timing out authentication.
	 * 
	 * @return Time in milliseconds before timing out authentication.
	 */
	long getSecurityTimeout();

	/**
	 * Specifies the time in milliseconds before timing out authentication.
	 * 
	 * @param timeout
	 *            Time in milliseconds before timing out authentication.
	 */
	void setSecurityTimeout(long timeout);

}