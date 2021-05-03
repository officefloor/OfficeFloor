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

import org.eclipse.gef.fx.nodes.GeometryNode;

import javafx.scene.layout.Region;

/**
 * Factory for the creation of the {@link GeometryNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnectorVisualFactory<N extends Region> {

	/**
	 * Creates the {@link GeometryNode}.
	 * 
	 * @param context
	 *            {@link AdaptedConnectorVisualFactoryContext}.
	 * @return New {@link GeometryNode}.
	 */
	N createGeometryNode(AdaptedConnectorVisualFactoryContext context);

}
