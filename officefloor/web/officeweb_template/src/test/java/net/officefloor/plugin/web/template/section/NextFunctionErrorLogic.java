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
package net.officefloor.plugin.web.template.section;

import net.officefloor.plugin.section.clazz.NextFunction;

/**
 * Logic containing {@link NextFunction} on section method.
 * 
 * @author Daniel Sagenschneider
 */
public class NextFunctionErrorLogic {

	/**
	 * Section method with disallowed {@link NextFunction}.
	 * 
	 * @return Should not be called as invalid to have {@link NextFunction}
	 *         annotation.
	 */
	@NextFunction("NotAllowed")
	public Object getSection() {
		return null;
	}

}