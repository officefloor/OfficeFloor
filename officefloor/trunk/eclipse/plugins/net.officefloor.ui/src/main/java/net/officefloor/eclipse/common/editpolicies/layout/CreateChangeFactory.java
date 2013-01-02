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
package net.officefloor.eclipse.common.editpolicies.layout;

import org.eclipse.draw2d.geometry.Point;

import net.officefloor.model.change.Change;

/**
 * <p>
 * Factory to create a {@link Change} to add a new {@link Object}.
 * <p>
 * Typically this will be from dragging an {@link Object} into the diagram.
 * 
 * @author Daniel Sagenschneider
 */
public interface CreateChangeFactory<O> {

	/**
	 * Creates a {@link Change} to add a new {@link Object}.
	 * 
	 * @param newObject
	 *            New {@link Object} to add.
	 * @param location
	 *            Location to add the {@link Object}.
	 * @return {@link Change} to add the new {@link Object}.
	 */
	Change<?> createChange(Object newObject, Point location);

}