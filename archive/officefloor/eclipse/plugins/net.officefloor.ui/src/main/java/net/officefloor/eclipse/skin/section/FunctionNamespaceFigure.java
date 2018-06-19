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
import net.officefloor.model.section.FunctionNamespaceModel;

/**
 * {@link OfficeFloorFigure} for the {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionNamespaceFigure extends OfficeFloorFigure {

	/**
	 * Indicates a change in the {@link FunctionNamespaceModel} name.
	 * 
	 * @param functionNamespaceName
	 *            Name to display for the {@link FunctionNamespaceModel}.
	 */
	void setFunctionNamespaceName(String functionNamespaceName);

	/**
	 * <p>
	 * Obtains the {@link IFigure} containing the {@link FunctionNamespaceModel}
	 * name.
	 * <p>
	 * This is to allow placement of the editor in changing the
	 * {@link FunctionNamespaceModel} name.
	 * 
	 * @return {@link IFigure} containing the {@link FunctionNamespaceModel}
	 *         name.
	 */
	IFigure getFunctionNamespaceNameFigure();

}