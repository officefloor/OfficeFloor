/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.gef.editor;

import java.util.List;
import java.util.function.Function;

import javafx.beans.property.Property;
import net.officefloor.model.Model;

/**
 * Builds the root {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedRootBuilder<R extends Model, O> extends EditorStyler {

	/**
	 * Adds an {@link AdaptedParent}.
	 * 
	 * @param <M>
	 *            {@link Model} type.
	 * @param <E>
	 *            {@link Model} event type.
	 * @param <RE>
	 *            Root {@link Model} event type.
	 * @param modelPrototype
	 *            {@link Model} prototype used in view validation, creation
	 *            prototype and to obtain the {@link Class} of the {@link Model}.
	 * @param getParents
	 *            {@link Function} to obtain the parent {@link Model} instances.
	 * @param viewFactory
	 *            {@link AdaptedChildVisualFactory} to create the view for the
	 *            {@link AdaptedParent}.
	 * @param changeParentEvents
	 *            {@link Enum} events on root {@link Model} about change in parent
	 *            {@link Model} listing.
	 * @return {@link AdaptedParentBuilder} to build the {@link AdaptedParent} over
	 *         the {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	<M extends Model, E extends Enum<E>, RE extends Enum<RE>> AdaptedParentBuilder<R, O, M, E> parent(M modelPrototype,
			Function<R, List<M>> getParents, AdaptedChildVisualFactory<M> viewFactory, RE... changeParentEvents);

	/**
	 * <p>
	 * Allows adding an overlay.
	 * <p>
	 * Co-ordinates used rather than Point to avoid importing libraries.
	 * 
	 * @param x
	 *            X co-ordinate.
	 * @param y
	 *            Y co-ordintate.
	 * @param overlayVisualFactory
	 *            {@link OverlayVisualFactory}.
	 */
	void overlay(double x, double y, OverlayVisualFactory overlayVisualFactory);

	/**
	 * Obtains the {@link Property} to the style sheet rules for the palette.
	 * 
	 * @return {@link Property} to specify the style sheet rules for the palette.
	 */
	Property<String> paletteStyle();

	/**
	 * Obtains the {@link Property} to the style sheet rules for the palette
	 * indicator.
	 * 
	 * @return {@link Property} to specify the style sheet rules for the palette
	 *         indicator.
	 */
	Property<String> paletteIndicatorStyle();

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
