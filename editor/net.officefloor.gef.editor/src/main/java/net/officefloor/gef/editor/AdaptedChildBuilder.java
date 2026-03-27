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

import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

/**
 * Builds an {@link AdaptedChild}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedChildBuilder<R extends Model, O, M extends Model, E extends Enum<E>>
		extends AdaptedConnectableBuilder<R, O, M, E> {

	/**
	 * Registers a read-only label for the {@link Model}.
	 * 
	 * @param getLabel          {@link Function} to get the label from the
	 *                          {@link Model}.
	 * @param labelChangeEvents {@link Enum} events fired by the {@link Model} for
	 *                          label changes.
	 */
	@SuppressWarnings("unchecked")
	void label(Function<M, String> getLabel, E... labelChangeEvents);

	/**
	 * Registers a mutable label for the {@link Model}.
	 * 
	 * @param getLabel          {@link Function} to get the label from the
	 *                          {@link Model}.
	 * @param setLabel          {@link LabelChange}.
	 * @param labelChangeEvents {@link Enum} events fired by the {@link Model} for
	 *                          label changes.
	 */
	@SuppressWarnings("unchecked")
	void label(Function<M, String> getLabel, LabelChange<M> setLabel, E... labelChangeEvents);

	/**
	 * Registers children for the {@link Model}.
	 * 
	 * @param childGroupName Name of child group.
	 * @param getChildren    {@link Function} to get the children from the
	 *                       {@link Model}.
	 * @param childrenEvents {@link Enum} events fired by the {@link Model} for
	 *                       children changes.
	 * @return {@link ChildrenGroupBuilder}.
	 */
	@SuppressWarnings("unchecked")
	ChildrenGroupBuilder<R, O> children(String childGroupName, Function<M, List<? extends Model>> getChildren,
			E... childrenEvents);

	/**
	 * Creates a {@link Change} for the label of the {@link Model}.
	 */
	public static interface LabelChange<M extends Model> {

		/**
		 * Creates {@link Change} for the label of the {@link Model}.
		 * 
		 * @param model    {@link Model}.
		 * @param newLabel New label.
		 * @return {@link Change}.
		 */
		Change<M> changeLabel(Model model, String newLabel);
	}

}
