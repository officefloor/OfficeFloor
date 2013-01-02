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
package net.officefloor.eclipse.wizard.officetask;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.frame.spi.administration.Duty;

/**
 * Instance of an {@link OfficeTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeTaskInstance {

	/**
	 * {@link OfficeTask} selected.
	 */
	private final OfficeTask officeTask;

	/**
	 * {@link OfficeSection} from which the {@link OfficeTask} was selected.
	 */
	private final OfficeSection officeSection;

	/**
	 * Flag indicating if pre rather than post {@link Duty} being linked to
	 * {@link OfficeTask}.
	 */
	private final boolean isPreRatherThanPostDuty;

	/**
	 * Initiate.
	 * 
	 * @param officeTask
	 *            {@link OfficeTask} selected.
	 * @param officeSection
	 *            {@link OfficeSection} from which the {@link OfficeTask} was
	 *            selected.
	 * @param isPreRatherThanPostDuty
	 *            Flag indicating if pre rather than post {@link Duty} being
	 *            linked to {@link OfficeTask}.
	 */
	OfficeTaskInstance(OfficeTask officeTask, OfficeSection officeSection,
			boolean isPreRatherThanPostDuty) {
		this.officeTask = officeTask;
		this.officeSection = officeSection;
		this.isPreRatherThanPostDuty = isPreRatherThanPostDuty;
	}

	/**
	 * Obtains the selected {@link OfficeTask}.
	 * 
	 * @return Selected {@link OfficeTask}.
	 */
	public OfficeTask getOfficeTask() {
		return this.officeTask;
	}

	/**
	 * Obtains the {@link OfficeSection} from which the {@link OfficeTask} was
	 * selected.
	 * 
	 * @return {@link OfficeSection} from which the {@link OfficeTask} was
	 *         selected.
	 */
	public OfficeSection getOfficeSection() {
		return this.officeSection;
	}

	/**
	 * Indicates if pre rather than post {@link Duty} being linked to
	 * {@link OfficeTask}.
	 * 
	 * @return <code>true</code> if pre {@link Duty}, otherwise
	 *         <code>false</code> if post {@link Duty}.
	 */
	public boolean isPreRatherThanPostDuty() {
		return this.isPreRatherThanPostDuty;
	}

}