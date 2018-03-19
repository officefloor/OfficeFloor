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

import net.officefloor.eclipse.editor.models.ChangeExecutor;
import net.officefloor.model.Model;

/**
 * Context for the {@link ParentModelProvider}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ParentModelProviderContext<R extends Model> {

	/**
	 * Obtains the root {@link Model}.
	 * 
	 * @return Root {@link Model}.
	 */
	R getRootModel();

	/**
	 * Obtains the {@link ChangeExecutor}.
	 * 
	 * @return {@link ChangeExecutor}.
	 */
	ChangeExecutor getChangeExecutor();

	/**
	 * Obtains the X location for the {@link AdaptedParent}.
	 * 
	 * @return X location.
	 */
	int getX();

	/**
	 * Obtains the Y location for the {@link AdaptedParent}.
	 * 
	 * @return Y location for the {@link AdaptedParent}.
	 */
	int getY();

	/**
	 * Convenience method to position the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model} to be positioned.
	 * @return Input {@link Model}.
	 */
	<M extends Model> M position(M model);

}