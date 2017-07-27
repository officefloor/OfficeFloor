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

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.model.AbstractModel;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.OfficeFunctionModel;

/**
 * Links the {@link AdministrationModel} to the {@link OfficeFunctionModel}
 * administration join point.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFunctionAdministrationJoinPointModel extends AbstractModel {

	/**
	 * {@link OfficeFunctionModel}.
	 */
	private final OfficeFunctionModel function;

	/**
	 * Flag indicating if pre {@link Administration} rather than post
	 * {@link Administration}.
	 */
	private final boolean isPreRatherThanPost;

	/**
	 * Initiate.
	 * 
	 * @param function
	 *            {@link OfficeFunctionModel}.
	 * @param isPreRatherThanPost
	 *            Flag indicating if pre {@link Administration} rather than post
	 *            {@link Administration}.
	 */
	public AbstractFunctionAdministrationJoinPointModel(OfficeFunctionModel function, boolean isPreRatherThanPost) {
		this.function = function;
		this.isPreRatherThanPost = isPreRatherThanPost;
	}

	/**
	 * Obtains the {@link OfficeFunctionModel}.
	 * 
	 * @return {@link OfficeFunctionModel}.
	 */
	public OfficeFunctionModel getFunction() {
		return this.function;
	}

	/**
	 * Indicates if pre {@link Administration} rather than post
	 * {@link Administration}.
	 * 
	 * @return <code>true</code> if pre {@link Administration}, otherwise
	 *         <code>false</code> if post {@link Administration}.
	 */
	public boolean isPreRatherThanPost() {
		return this.isPreRatherThanPost;
	}

	/**
	 * Triggers a
	 * {@link FunctionAdministrationJoinPointEvent#CHANGE_ADMINISTRATION} event.
	 */
	public void triggerDutyChangeEvent() {
		this.firePropertyChange(FunctionAdministrationJoinPointEvent.CHANGE_ADMINISTRATION.name(), "OLD", "NEW");
	}

}