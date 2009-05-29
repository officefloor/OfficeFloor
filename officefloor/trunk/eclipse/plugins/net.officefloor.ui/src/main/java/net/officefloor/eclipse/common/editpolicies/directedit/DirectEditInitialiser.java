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
package net.officefloor.eclipse.common.editpolicies.directedit;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Provides the necessary information to initialise a direct edit.
 * 
 * @author Daniel Sagenschneider
 */
public interface DirectEditInitialiser {

	/**
	 * Obtains the initial value.
	 * 
	 * @return Initial value.
	 */
	String getInitialValue();

	/**
	 * Obtains the {@link IFigure} that is used to determine the location of the
	 * direct edit. This is typically the {@link Label} containing the value
	 * being directly edited.
	 * 
	 * @return {@link IFigure} to use to determine the location.
	 */
	IFigure getLocationFigure();
}