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
package net.officefloor.eclipse.wizard.officefunction;

import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.frame.api.administration.Administration;

/**
 * Instance of an {@link OfficeFunctionType}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFunctionInstance {

	/**
	 * {@link OfficeFunctionType} selected.
	 */
	private final OfficeFunctionType officeFunctionType;

	/**
	 * {@link OfficeSectionType} from which the {@link OfficeFunctionType} was
	 * selected.
	 */
	private final OfficeSectionType officeSectionType;

	/**
	 * Flag indicating if pre rather than post {@link Administration} being
	 * linked to {@link OfficeFunctionType}.
	 */
	private final boolean isPreRatherThanPostAdministration;

	/**
	 * Initiate.
	 * 
	 * @param officeFunctionType
	 *            {@link OfficeFunctionType} selected.
	 * @param officeSectionType
	 *            {@link OfficeSectionType} from which the
	 *            {@link OfficeFunctionType} was selected.
	 * @param isPreRatherThanPostAdministration
	 *            Flag indicating if pre rather than post {@link Administration}
	 *            being linked to {@link OfficeFunctionType}.
	 */
	OfficeFunctionInstance(OfficeFunctionType officeFunctionType, OfficeSectionType officeSectionType,
			boolean isPreRatherThanPostAdministration) {
		this.officeFunctionType = officeFunctionType;
		this.officeSectionType = officeSectionType;
		this.isPreRatherThanPostAdministration = isPreRatherThanPostAdministration;
	}

	/**
	 * Obtains the selected {@link OfficeFunctionType}.
	 * 
	 * @return Selected {@link OfficeFunctionType}.
	 */
	public OfficeFunctionType getOfficeFunctionType() {
		return this.officeFunctionType;
	}

	/**
	 * Obtains the {@link OfficeSectionType} from which the
	 * {@link OfficeFunctionType} was selected.
	 * 
	 * @return {@link OfficeSectionType} from which the
	 *         {@link OfficeFunctionType} was selected.
	 */
	public OfficeSectionType getOfficeSectionType() {
		return this.officeSectionType;
	}

	/**
	 * Indicates if pre rather than post {@link Administration} being linked to
	 * {@link OfficeFunctionType}.
	 * 
	 * @return <code>true</code> if pre {@link Administration}, otherwise
	 *         <code>false</code> if post {@link Administration}.
	 */
	public boolean isPreRatherThanPostAdministration() {
		return this.isPreRatherThanPostAdministration;
	}

}