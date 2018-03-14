/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.editor;

import net.officefloor.model.Model;

/**
 * Builder for the {@link AdaptedParent}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedParent<M extends Model> extends AdaptedChild<M> {

	/**
	 * Changes the location of the {@link Model}.
	 * 
	 * @param x
	 *            X.
	 * @param y
	 *            Y.
	 */
	void changeLocation(int x, int y);

}