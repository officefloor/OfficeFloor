/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.model.section.SubSectionInputModel;

import org.eclipse.draw2d.IFigure;

/**
 * Context for the {@link SubSectionInputModel} {@link IFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SubSectionInputFigureContext {

	/**
	 * Obtains the {@link SubSectionInputModel} name.
	 * 
	 * @return {@link SubSectionInputModel} name.
	 */
	String getSubSectionInputName();

	/**
	 * Indicates if {@link SubSectionInputModel} is public.
	 * 
	 * @return <code>true</code> if public.
	 */
	boolean isPublic();

	/**
	 * Flags if public.
	 * 
	 * @param isPublic
	 *            <code>true</code> if public.
	 */
	void setIsPublic(boolean isPublic);

}