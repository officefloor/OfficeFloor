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

import org.eclipse.gef.geometry.planar.Dimension;

import net.officefloor.model.Model;

/**
 * Builds an {@link AdaptedArea}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedAreaBuilder<R extends Model, O, M extends Model, E extends Enum<E>>
		extends AdaptedConnectableBuilder<R, O, M, E> {

	/**
	 * Specifies the minimum {@link Dimension}.
	 * 
	 * @param width  Minimum width.
	 * @param height Minimum height.
	 */
	void setMinimumDimension(double width, double height);

	/**
	 * Configures an {@link ModelAction} for the area {@link Model}.
	 * 
	 * @param action        {@link ModelAction}.
	 * @param visualFactory {@link AdaptedActionVisualFactory}.
	 */
	void action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory);

}
