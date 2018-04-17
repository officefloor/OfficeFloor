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
 * Provides a new parent {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ParentModelProvider<R extends Model, O, M extends Model> {

	/**
	 * Triggers providing a new parent {@link Model}.
	 * 
	 * @param context
	 *            {@link ParentModelProviderContext}.
	 * @throws Exception
	 *             Possible {@link Exception} in creating the new
	 *             {@link AdaptedParent}.
	 */
	void provideNewParentModel(ParentModelProviderContext<R, O, M> context) throws Exception;

}