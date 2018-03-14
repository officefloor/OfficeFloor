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
 * Provides means to build the adapted model.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedBuilderContext {

	/**
	 * Adds an {@link AdaptedParent}.
	 * 
	 * @param modelClass
	 *            {@link Class} of the {@link Model}.
	 * @param viewFactory
	 *            {@link ViewFactory} to create the view for the
	 *            {@link AdaptedParent}.
	 * @return {@link AdaptedParentBuilder} to build the {@link AdaptedParent} over
	 *         the {@link Model}.
	 */
	<M extends Model, E extends Enum<E>> AdaptedParentBuilder<M, E> addParent(Class<M> modelClass,
			ViewFactory<M, AdaptedParent<M>> viewFactory);

}