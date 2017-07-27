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

import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.frame.spi.administration.Duty;

/**
 * Instance of an {@link OfficeTaskType}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeTaskInstance {

	/**
	 * {@link OfficeTaskType} selected.
	 */
	private final OfficeTaskType officeTaskType;

	/**
	 * {@link OfficeSectionType} from which the {@link OfficeTaskType} was selected.
	 */
	private final OfficeSectionType officeSectionType;

	/**
	 * Flag indicating if pre rather than post {@link Duty} being linked to
	 * {@link OfficeTaskType}.
	 */
	private final boolean isPreRatherThanPostDuty;

	/**
	 * Initiate.
	 * 
	 * @param officeTaskType
	 *            {@link OfficeTaskType} selected.
	 * @param officeSectionType
	 *            {@link OfficeSectionType} from which the {@link OfficeTaskType} was
	 *            selected.
	 * @param isPreRatherThanPostDuty
	 *            Flag indicating if pre rather than post {@link Duty} being
	 *            linked to {@link OfficeTaskType}.
	 */
	OfficeTaskInstance(OfficeTaskType officeTaskType, OfficeSectionType officeSectionType,
			boolean isPreRatherThanPostDuty) {
		this.officeTaskType = officeTaskType;
		this.officeSectionType = officeSectionType;
		this.isPreRatherThanPostDuty = isPreRatherThanPostDuty;
	}

	/**
	 * Obtains the selected {@link OfficeTaskType}.
	 * 
	 * @return Selected {@link OfficeTaskType}.
	 */
	public OfficeTaskType getOfficeTaskType() {
		return this.officeTaskType;
	}

	/**
	 * Obtains the {@link OfficeSectionType} from which the {@link OfficeTaskType} was
	 * selected.
	 * 
	 * @return {@link OfficeSectionType} from which the {@link OfficeTaskType} was
	 *         selected.
	 */
	public OfficeSectionType getOfficeSectionType() {
		return this.officeSectionType;
	}

	/**
	 * Indicates if pre rather than post {@link Duty} being linked to
	 * {@link OfficeTaskType}.
	 * 
	 * @return <code>true</code> if pre {@link Duty}, otherwise
	 *         <code>false</code> if post {@link Duty}.
	 */
	public boolean isPreRatherThanPostDuty() {
		return this.isPreRatherThanPostDuty;
	}

}