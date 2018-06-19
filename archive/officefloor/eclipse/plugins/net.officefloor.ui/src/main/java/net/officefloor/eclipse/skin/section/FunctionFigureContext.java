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

import net.officefloor.model.section.FunctionModel;

/**
 * Context for the {@link FunctionModel} {@link IFigure}.
 *
 * @author Daniel Sagenschneider
 */
public interface FunctionFigureContext {

	/**
	 * Obtains the {@link FunctionModel} name.
	 *
	 * @return {@link FunctionModel} name.
	 */
	String getFunctionName();

	/**
	 * Indicates if the {@link FunctionModel} is public.
	 *
	 * @return <code>true</code> if public.
	 */
	boolean isPublic();

	/**
	 * Flags whether the {@link FunctionModel} is public.
	 *
	 * @param isPublic
	 *            <code>true</code> if to be public.
	 */
	void setIsPublic(boolean isPublic);

	/**
	 * Obtains the parameter type name.
	 *
	 * @return Parameter type name.
	 */
	String getParameterTypeName();

	/**
	 * Obtains the return type name.
	 *
	 * @return Return type name.
	 */
	String getReturnTypeName();

	/**
	 * Obtains the documentation of the {@link FunctionModel}.
	 *
	 * @return Documentation of the {@link FunctionModel}.
	 */
	String getFunctionDocumentation();

}