/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.plugin.web.template.build;

import net.officefloor.compile.spi.office.OfficeSectionOutput;

/**
 * Web template.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplate {

	/**
	 * Specifies the logic {@link Class}.
	 * 
	 * @param logicClass
	 *            Logic {@link Class}.
	 */
	void setLogicClass(Class<?> logicClass);

	/**
	 * Obtains the {@link OfficeSectionOutput} from the {@link WebTemplate}.
	 * 
	 * @param outputName
	 *            {@link OfficeSectionOutput} name.
	 * @return {@link OfficeSectionOutput} for the name.
	 */
	OfficeSectionOutput getOutput(String outputName);

}