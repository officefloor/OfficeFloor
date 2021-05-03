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
 * Adapted area.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedArea<M extends Model> extends AdaptedConnectable<M>, AdaptedConnector<M> {

	/**
	 * Obtains the minimum {@link Dimension} for the {@link AdaptedArea}.
	 * 
	 * @return Minimum {@link Dimension} for the {@link AdaptedArea}.
	 */
	Dimension getMinimumDimension();

	/**
	 * Obtains the {@link Dimension}.
	 * 
	 * @return {@link Dimension}.
	 */
	Dimension getDimension();

	/**
	 * Specifies the {@link Dimension}.
	 * 
	 * @param dimension {@link Dimension}.
	 */
	void setDimension(Dimension dimension);

	/**
	 * Obtains the {@link ParentToAreaConnectionModel}.
	 * 
	 * @return {@link ParentToAreaConnectionModel}.
	 */
	ParentToAreaConnectionModel getParentConnection();

	/**
	 * Obtains the adapter.
	 * 
	 * @param          <T> Adapted type.
	 * @param classKey {@link Class} key.
	 * @return Adapter or <code>null</code> if no adapter available.
	 */
	<T> T getAdapter(Class<T> classKey);

}
