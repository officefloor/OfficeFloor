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
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.gef.geometry.planar.Dimension;

import net.officefloor.model.Model;

/**
 * Builder of an {@link AdaptedParent}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedParentBuilder<R extends Model, O, M extends Model, E extends Enum<E>>
		extends AdaptedChildBuilder<R, O, M, E> {

	/**
	 * Configures creating the {@link Model}.
	 * 
	 * @param provideParentAction {@link ModelAction} to provide parent.
	 */
	void create(ModelAction<R, O, M> provideParentAction);

	/**
	 * Configures an {@link ModelAction} for the parent {@link Model}.
	 * 
	 * @param action        {@link ModelAction}.
	 * @param visualFactory {@link AdaptedActionVisualFactory}.
	 */
	void action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory);

	/**
	 * Adds an {@link AdaptedArea}.
	 * 
	 * @param                  <AM> {@link Model} type.
	 * @param                  <AE> {@link Model} event type.
	 * @param                  <RE> Root {@link Model} event type.
	 * @param areaPrototype    Prototype of area {@link Model} to obtain the
	 *                         {@link Model} class.
	 * @param getAreas         {@link Function} to obtain the area {@link Model}
	 *                         instances.
	 * @param getDimension     Obtains the {@link Dimension} of the area from the
	 *                         {@link Model}.
	 * @param setDimension     Loads the {@link Dimension} of the area onto the
	 *                         {@link Model}.
	 * @param changeAreaEvents {@link Enum} events on parent {@link Model} about
	 *                         change in area {@link Model} listing.
	 * @return {@link AdaptedAreaBuilder} to build the {@link AdaptedArea} over the
	 *         {@link Model}.
	 */
	@SuppressWarnings("unchecked")
	<AM extends Model, AE extends Enum<AE>, RE extends Enum<RE>> AdaptedAreaBuilder<R, O, AM, AE> area(AM areaPrototype,
			Function<M, List<AM>> getAreas, Function<AM, Dimension> getDimension,
			BiConsumer<AM, Dimension> setDimension, E... changeAreaEvents);

}
