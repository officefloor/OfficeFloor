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

package net.officefloor.gef.editor.internal.parts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.mvc.fx.parts.IContentPart;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import net.officefloor.gef.editor.AdaptedChild;
import net.officefloor.gef.editor.AdaptedModelStyler;
import net.officefloor.gef.editor.ChildrenGroup;
import net.officefloor.gef.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.model.Model;

/**
 * {@link IContentPart} for the {@link AdaptedChild}.
 *
 * @author Daniel Sagenschneider
 */
public class AdaptedChildPart<M extends Model, A extends AdaptedChild<M>> extends AbstractAdaptedConnectablePart<M, A>
		implements AdaptedModelStyler {

	/**
	 * {@link ChildrenGroupVisual} instances for the {@link ChildrenGroupImpl}
	 * instances.
	 */
	private Map<ChildrenGroup<M, ?>, ChildrenGroupVisual> childrenGroupVisuals;

	/**
	 * Obtains the {@link Pane} for the {@link ChildrenGroupImpl}.
	 * 
	 * @param childrenGroup {@link ChildrenGroupImpl}.
	 * @return {@link Pane}.
	 */
	public Pane getChildrenGroupPane(ChildrenGroup<?, ?> childrenGroup) {
		return this.childrenGroupVisuals.get(childrenGroup).pane;
	}

	/*
	 * ================== IContentPart =========================
	 */

	@Override
	protected List<Object> doGetContentChildren() {
		List<Object> children = super.doGetContentChildren();
		children.addAll(this.getContent().getChildrenGroups());
		return children;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Node createVisualNode() {

		// Load the children group visuals
		this.childrenGroupVisuals = new HashMap<>();
		for (ChildrenGroup<M, ?> childrenGroup : this.getContent().getChildrenGroups()) {
			this.childrenGroupVisuals.put(childrenGroup, new ChildrenGroupVisual());
		}

		// Create the visual node
		Node visualNode = this.getContent().createVisual(new AdaptedChildVisualFactoryContextImpl<M>(
				(Class<M>) this.getContent().getModel().getClass(), this.isPalettePrototype, () -> {

					// Return the label
					return this.getContent().getLabel();

				}, (childGroupName, parent) -> {

					// Load the child group pane
					for (ChildrenGroup<M, ?> childrenGroup : this.getContent().getChildrenGroups()) {
						if (childGroupName.equals(childrenGroup.getChildrenGroupName())) {

							// Found the child group, so load the pane
							ChildrenGroupVisual visual = this.childrenGroupVisuals.get(childrenGroup);
							visual.pane = parent;

							// Child group registered
							return true;
						}
					}

					// Child group not registered
					return false;

				}, this.getConnectorLoader(), (action) -> {

					// Undertake the action
					this.getContent().action(action);
				}));

		// Ensure all children groups are configured
		for (ChildrenGroup<M, ?> childrenGroup : this.getContent().getChildrenGroups()) {
			ChildrenGroupVisual visual = this.childrenGroupVisuals.get(childrenGroup);
			if (visual.pane == null) {
				throw new IllegalStateException("Children group Pane '" + childrenGroup.getChildrenGroupName()
						+ "' not configured in view of model " + this.getContent().getModel().getClass().getName());
			}
		}

		// Return the visual node
		return visualNode;
	}

	/**
	 * {@link ChildrenGroupImpl} visual.
	 */
	private static class ChildrenGroupVisual {

		/**
		 * {@link Pane} for the {@link ChildrenGroupImpl}.
		 */
		private Pane pane = null;
	}

}
