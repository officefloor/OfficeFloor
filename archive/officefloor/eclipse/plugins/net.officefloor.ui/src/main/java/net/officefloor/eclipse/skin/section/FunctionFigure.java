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
package net.officefloor.eclipse.skin.section;

import org.eclipse.draw2d.IFigure;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.section.FunctionModel;

/**
 * {@link OfficeFloorFigure} for the {@link FunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionFigure extends OfficeFloorFigure {

	/**
	 * Indicates a change in the {@link FunctionModel} name.
	 * 
	 * @param functionName
	 *            Name to display for the {@link FunctionModel}.
	 */
	void setFunctionName(String functionName);

	/**
	 * Flags on display whether is public.
	 * 
	 * @param isPublic
	 *            <code>true</code> if public.
	 */
	void setIsPublic(boolean isPublic);

	/**
	 * <p>
	 * Obtains the {@link IFigure} containing the {@link FunctionModel} name.
	 * <p>
	 * This is to allow placement of the editor in changing the
	 * {@link FunctionModel} name.
	 * 
	 * @return {@link IFigure} containing the {@link FunctionModel} name.
	 */
	IFigure getFunctionNameFigure();

}