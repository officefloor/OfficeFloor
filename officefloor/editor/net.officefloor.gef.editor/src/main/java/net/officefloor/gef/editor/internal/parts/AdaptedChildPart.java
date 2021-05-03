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
