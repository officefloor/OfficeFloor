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

import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkTaskModel;

import org.eclipse.draw2d.IFigure;

/**
 * Context for the {@link WorkTaskModel} {@link IFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkTaskFigureContext {

	/**
	 * Obtains the name of the {@link WorkTaskModel}.
	 * 
	 * @return Name of the {@link WorkTaskModel}.
	 */
	String getWorkTaskName();

	/**
	 * Creates a {@link TaskModel} from the {@link WorkTaskModel}.
	 */
	void createAsNewTask();

}