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

import org.eclipse.gef.geometry.planar.Point;

import net.officefloor.model.Model;

/**
 * Builds the root {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedRootBuilder<R extends Model, O> {

	/**
	 * Adds an {@link AdaptedParent}.
	 * 
	 * @param modelPrototype
	 *            {@link Model} prototype used in view validation, creation
	 *            prototype and to obtain the {@link Class} of the {@link Model}.
	 * @param getParents
	 *            {@link Function} to obtain the parent {@link Model} instances.
	 * @param viewFactory
	 *            {@link AdaptedModelVisualFactory} to create the view for the
	 *            {@link AdaptedParent}.
	 * @param changeParentEvents
	 *            {@link Enum} events on root {@link Model} about change in parent
	 *            {@link Model} listing.
	 * @return {@link AdaptedParentBuilder} to build the {@link AdaptedParent} over
	 *         the {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	<M extends Model, E extends Enum<E>, RE extends Enum<RE>> AdaptedParentBuilder<R, O, M, E> parent(M modelPrototype,
			Function<R, List<M>> getParents, AdaptedModelVisualFactory<M, AdaptedParent<M>> viewFactory,
			RE... changeParentEvents);

	/**
	 * Allows adding an overlay.
	 * 
	 * @param location
	 *            Location of the overlay.
	 * @param overlayVisualFactory
	 *            {@link OverlayVisualFactory}.
	 */
	void overlay(Point location, OverlayVisualFactory overlayVisualFactory);

	/**
	 * Obtains the {@link AdaptedErrorHandler}.
	 * 
	 * @return {@link AdaptedErrorHandler}.
	 */
	AdaptedErrorHandler getErrorHandler();

	/**
	 * Obtains the {@link ChangeExecutor}.
	 * 
	 * @return {@link ChangeExecutor}.
	 */
	ChangeExecutor getChangeExecutor();

}