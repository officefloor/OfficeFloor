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

package net.officefloor.gef.editor.internal.models;

import org.eclipse.gef.geometry.planar.Point;

import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.model.Model;

/**
 * Adapted prototype for creating a new {@link AdaptedParent}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedPrototype<M extends Model> {

	/**
	 * Triggers creating a new {@link AdaptedParent} at the location.
	 * 
	 * @param location
	 *            Location for the new {@link AdaptedParent}.
	 */
	void newAdaptedParent(Point location);

}
