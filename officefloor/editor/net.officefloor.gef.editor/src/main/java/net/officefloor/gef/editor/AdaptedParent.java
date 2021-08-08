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
