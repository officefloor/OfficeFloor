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
package net.officefloor.plugin.woof;

import java.util.ServiceLoader;

import net.officefloor.plugin.web.http.application.WebArchitect;

/**
 * {@link ServiceLoader} service that enables extending functionality over and
 * above the {@link WoofLoader} by directly configuring the
 * {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofApplicationExtensionService {

	/**
	 * Extends the {@link WebArchitect}.
	 * 
	 * @param context
	 *            {@link WoofApplicationExtensionServiceContext}.
	 * @throws Exception
	 *             If fails to extend the {@link WebArchitect}.
	 */
	void extendApplication(WoofApplicationExtensionServiceContext context)
			throws Exception;

}