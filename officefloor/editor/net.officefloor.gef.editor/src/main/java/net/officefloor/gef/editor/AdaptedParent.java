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

import net.officefloor.model.Model;

/**
 * Builder for the {@link AdaptedParent}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedParent<M extends Model> extends AdaptedChild<M> {

	/**
	 * Indicates if the palette prototype.
	 * 
	 * @return <code>true</code> if the palette prototype.
	 */
	boolean isPalettePrototype();

	/**
	 * Obtains the {@link AdaptedArea} instances.
	 * 
	 * @return {@link AdaptedArea} instances.
	 */
	List<AdaptedArea<?>> getAdaptedAreas();

	/**
	 * Indicates if {@link AdaptedArea} change event.
	 * 
	 * @param eventName Name of the event.
	 * @return <code>true</code> if {@link AdaptedArea} change event.
	 */
	boolean isAreaChangeEvent(String eventName);

	/**
	 * Obtains the adapter.
	 * 
	 * @param          <T> Adapted type.
	 * @param classKey {@link Class} key.
	 * @return Adapter or <code>null</code> if no adapter available.
	 */
	<T> T getAdapter(Class<T> classKey);

	/**
	 * Changes the location of the {@link Model}.
	 * 
	 * @param x X.
	 * @param y Y.
	 */
	void changeLocation(int x, int y);

}
