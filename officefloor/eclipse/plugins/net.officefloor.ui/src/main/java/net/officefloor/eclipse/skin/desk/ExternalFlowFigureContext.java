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
package net.officefloor.eclipse.skin.desk;

import org.eclipse.draw2d.IFigure;

import net.officefloor.model.desk.ExternalFlowModel;

/**
 * Context for the {@link ExternalFlowModel} {@link IFigure}.
 *
 * @author Daniel Sagenschneider
 */
public interface ExternalFlowFigureContext {

	/**
	 * Obtains the {@link ExternalFlowModel} name.
	 *
	 * @return {@link ExternalFlowModel} name.
	 */
	String getExternalFlowName();

	/**
	 * Obtains the argument type name.
	 *
	 * @return Argument type name.
	 */
	String getArgumentTypeName();

}
