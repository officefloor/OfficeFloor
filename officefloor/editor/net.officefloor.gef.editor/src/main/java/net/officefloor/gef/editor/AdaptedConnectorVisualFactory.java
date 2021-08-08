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
