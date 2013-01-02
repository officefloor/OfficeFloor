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
package net.officefloor.eclipse.office.models;

import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.model.AbstractModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.OfficeTaskModel;

/**
 * Links the {@link DutyModel} to the {@link OfficeTaskModel} administration
 * join point.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTaskAdministrationJoinPointModel extends
		AbstractModel {

	/**
	 * {@link OfficeTaskModel}.
	 */
	private final OfficeTaskModel task;

	/**
	 * Flag indicating if pre {@link Duty} rather than post {@link Duty}.
	 */
	private final boolean isPreRatherThanPost;

	/**
	 * Initiate.
	 * 
	 * @param task
	 *            {@link OfficeTaskModel}.
	 * @param isPreRatherThanPost
	 *            Flag indicating if pre {@link Duty} rather than post
	 *            {@link Duty}.
	 */
	public AbstractTaskAdministrationJoinPointModel(OfficeTaskModel task,
			boolean isPreRatherThanPost) {
		this.task = task;
		this.isPreRatherThanPost = isPreRatherThanPost;
	}

	/**
	 * Obtains the {@link OfficeTaskModel}.
	 * 
	 * @return {@link OfficeTaskModel}.
	 */
	public OfficeTaskModel getTask() {
		return this.task;
	}

	/**
	 * Indicates if pre {@link Duty} rather than post {@link Duty}.
	 * 
	 * @return <code>true</code> if pre {@link Duty}, otherwise
	 *         <code>false</code> if post {@link Duty}.
	 */
	public boolean isPreRatherThanPost() {
		return this.isPreRatherThanPost;
	}

	/**
	 * Triggers a {@link TaskAdministrationJoinPointEvent#CHANGE_DUTIES} event.
	 */
	public void triggerDutyChangeEvent() {
		this.firePropertyChange(TaskAdministrationJoinPointEvent.CHANGE_DUTIES
				.name(), "OLD", "NEW");
	}
}