/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
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