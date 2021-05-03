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
