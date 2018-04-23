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
 * Builds the child group.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChildrenGroupBuilder<R extends Model, O> {

	/**
	 * Adds a child {@link Model}.
	 * 
	 * @param modelPrototype
	 *            {@link Model} prototype to determine {@link Class} of the
	 *            {@link Model} and used in visual validation.
	 * @param viewFactory
	 *            {@link AdaptedModelVisualFactory} to create the view for the
	 *            {@link AdaptedChild}.
	 * @return {@link AdaptedParentBuilder} to build the adapter over the
	 *         {@link Model}.
	 */
	<M extends Model, E extends Enum<E>> AdaptedChildBuilder<R, O, M, E> addChild(M modelPrototype,
			AdaptedModelVisualFactory<M> viewFactory);

}