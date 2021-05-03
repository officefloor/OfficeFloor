/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
