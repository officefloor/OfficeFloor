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
package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.manage.Office;

/**
 * {@link OfficeSection} of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSection extends OfficeSubSection {

	/**
	 * Obtains the {@link OfficeSectionInput} instances for this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionInput} instances for this
	 *         {@link OfficeSection}.
	 */
	OfficeSectionInput[] getOfficeSectionInputs();

	/**
	 * Obtains the {@link OfficeSectionOutput} instances for this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionOutput} instances for this
	 *         {@link OfficeSection}.
	 */
	OfficeSectionOutput[] getOfficeSectionOutputs();

	/**
	 * Obtains the {@link OfficeSectionObject} instances required by this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionObject} instances required by this
	 *         {@link OfficeSection}.
	 */
	OfficeSectionObject[] getOfficeSectionObjects();

}