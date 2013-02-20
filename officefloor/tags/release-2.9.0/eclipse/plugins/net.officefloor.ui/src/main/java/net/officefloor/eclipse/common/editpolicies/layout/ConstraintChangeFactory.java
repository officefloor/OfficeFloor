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

import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Factory to create a {@link Change} to change constraint on a {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConstraintChangeFactory<M> {

	/**
	 * Creates the {@link Change}.
	 * 
	 * @param target
	 *            {@link Model} to have its constraints changed.
	 * @param constraint
	 *            New constraint for {@link Model}.
	 * @return {@link Change} to change constraint of {@link Model}.
	 */
	Change<M> createChange(M target, Rectangle constraint);

}