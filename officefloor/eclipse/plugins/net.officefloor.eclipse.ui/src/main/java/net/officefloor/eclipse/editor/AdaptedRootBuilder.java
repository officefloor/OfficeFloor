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

import java.util.List;
import java.util.function.Function;

import net.officefloor.model.Model;

/**
 * Builds the root {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedRootBuilder<R extends Model> {

	/**
	 * Adds an {@link AdaptedParent}.
	 * 
	 * @param modelClass
	 *            {@link Class} of the {@link Model}.
	 * @param getParents
	 *            {@link Function} to obtain the parent {@link Model} instances.
	 * @param viewFactory
	 *            {@link ViewFactory} to create the view for the
	 *            {@link AdaptedParent}.
	 * @param changeParentEvents
	 *            {@link Enum} events on root {@link Model} about change in parent
	 *            {@link Model} listing.
	 * @return {@link AdaptedParentBuilder} to build the {@link AdaptedParent} over
	 *         the {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	<M extends Model, E extends Enum<E>, RE extends Enum<RE>> AdaptedParentBuilder<R, M, E> parent(Class<M> modelClass,
			Function<R, List<M>> getParents, ViewFactory<M, AdaptedParent<M>> viewFactory, RE... changeParentEvents);

}