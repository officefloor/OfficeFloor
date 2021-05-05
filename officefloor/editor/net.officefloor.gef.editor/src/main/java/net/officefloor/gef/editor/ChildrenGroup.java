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

import javafx.collections.ObservableList;
import net.officefloor.gef.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.model.Model;

/**
 * Child group.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChildrenGroup<M extends Model, E extends Enum<E>> {

	/**
	 * Obtains the {@link ChildrenGroupImpl} name.
	 * 
	 * @return {@link ChildrenGroupImpl} name.
	 */
	String getChildrenGroupName();

	/**
	 * Obtains the parent {@link AdaptedChild}.
	 * 
	 * @return Parent {@link AdaptedChild}.
	 */
	AdaptedChild<M> getParent();

	/**
	 * Obtains the {@link AdaptedChild} instances.
	 * 
	 * @return {@link AdaptedChild} instances.
	 */
	ObservableList<AdaptedChild<?>> getChildren();

	/**
	 * Obtains the events.
	 * 
	 * @return Events.
	 */
	E[] getEvents();

}
