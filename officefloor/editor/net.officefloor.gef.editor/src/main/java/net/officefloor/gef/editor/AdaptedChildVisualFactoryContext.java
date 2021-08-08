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

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import net.officefloor.model.Model;

/**
 * Context for the {@link AdaptedChildVisualFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedChildVisualFactoryContext<M extends Model>
		extends AdaptedModelVisualFactoryContext<M>, AdaptedConnectorVisualFactoryContext {

	/**
	 * Convenience method to add the {@link AdaptedModel} {@link Label} to the
	 * {@link Pane}.
	 * 
	 * @param parent {@link Pane}.
	 * @return Added {@link Label}.
	 */
	Label label(Pane parent);

	/**
	 * Specifies the {@link Pane} for the child group.
	 *
	 * @param                <P> Parent {@link Pane} type.
	 * @param childGroupName Name of the child group.
	 * @param parent         {@link Pane} to add the child group visuals.
	 * @return Input {@link Pane}.
	 */
	<P extends Pane> P childGroup(String childGroupName, P parent);

	/**
	 * <p>
	 * Indicates if palette prototype.
	 * <p>
	 * This allows for visual to not show actions or connectors that would be
	 * confusing (and error) if used from the palette.
	 * 
	 * @return <code>true</code> if the palette prototype.
	 */
	boolean isPalettePrototype();

}
