/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.woof.servlet;

import javax.servlet.Servlet;

import net.officefloor.plugin.woof.WoofApplicationExtensionService;
import net.officefloor.plugin.woof.WoofApplicationExtensionServiceContext;

/**
 * {@link WoofApplicationExtensionService} to chain in a {@link Servlet}
 * container servicer.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContainerWoofApplicationExtensionService implements
		WoofApplicationExtensionService {

	/*
	 * ==================== WoofApplicationExtensionService ==================
	 */

	@Override
	public void extendApplication(WoofApplicationExtensionServiceContext context)
			throws Exception {
		// TODO implement WoofApplicationExtensionService.extendApplication
		throw new UnsupportedOperationException(
				"TODO implement WoofApplicationExtensionService.extendApplication");
	}

}