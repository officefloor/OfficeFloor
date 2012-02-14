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

package net.officefloor.eclipse.skin.desk;

import net.officefloor.model.desk.WorkTaskObjectModel;

import org.eclipse.draw2d.IFigure;

/**
 * Context for the {@link WorkTaskObjectModel} {@link IFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkTaskObjectFigureContext {

	/**
	 * Obtains the object type for the {@link WorkTaskObjectModel}.
	 * 
	 * @return Object type for the {@link WorkTaskObjectModel}.
	 */
	String getObjectType();

	/**
	 * Flags if this {@link WorkTaskObjectModel} is the parameter.
	 * 
	 * @return <code>true</code> if the {@link WorkTaskObjectModel} is the
	 *         parameter.
	 */
	boolean isParameter();

	/**
	 * Flags whether the {@link WorkTaskObjectModel} is the parameter.
	 * 
	 * @param isParameter
	 *            Flag indicating if the parameter.
	 */
	void setIsParameter(boolean isParameter);

}