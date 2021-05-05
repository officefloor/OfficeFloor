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

import java.util.Map.Entry;

import org.eclipse.gef.mvc.fx.handlers.HoverOnHoverHandler;
import org.eclipse.gef.mvc.fx.parts.AbstractHandlePart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.viewer.IViewer;

import com.google.common.collect.SetMultimap;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class HoverHandleContainerPart extends AbstractHandlePart<VBox> {

	public HoverHandleContainerPart() {
		setAdapter(new HoverOnHoverHandler() {
			@Override
			public void hover(MouseEvent e) {
				// Deactivate hover for this part
			}
		});
	}

	@Override
	protected void doAddChildVisual(IVisualPart<? extends Node> child, int index) {
		getVisual().getChildren().add(index, child.getVisual());
		for (Entry<IVisualPart<? extends Node>, String> anchorage : getAnchoragesUnmodifiable().entries()) {
			child.attachToAnchorage(anchorage.getKey(), anchorage.getValue());
		}
	}

	@Override
	protected void doAttachToAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {
		super.doAttachToAnchorageVisual(anchorage, role);
		for (IVisualPart<? extends Node> child : getChildrenUnmodifiable()) {
			child.attachToAnchorage(anchorage, role);
		}
	}

	@Override
	protected VBox doCreateVisual() {
		VBox vBox = new VBox();
		vBox.setPickOnBounds(true);
		return vBox;
	}

	@Override
	protected void doDetachFromAnchorageVisual(IVisualPart<? extends Node> anchorage, String role) {
		super.doDetachFromAnchorageVisual(anchorage, role);
		for (IVisualPart<? extends Node> child : getChildrenUnmodifiable()) {
			child.detachFromAnchorage(anchorage, role);
		}
	}

	@Override
	protected void doRefreshVisual(VBox visual) {
		// check if we have a host
		SetMultimap<IVisualPart<? extends Node>, String> anchorages = getAnchoragesUnmodifiable();
		if (anchorages.isEmpty()) {
			return;
		}

		// determine center location of host visual
		IVisualPart<? extends Node> anchorage = anchorages.keys().iterator().next();
		refreshHandleLocation(anchorage.getVisual());
	}

	protected void refreshHandleLocation(Node hostVisual) {
		Bounds hostBounds = hostVisual.getBoundsInParent();
		Parent parent = hostVisual.getParent();
		if (parent != null) {
			hostBounds = parent.localToScene(hostBounds);
		}
		Point2D location = getVisual().getParent().sceneToLocal(hostBounds.getMaxX(), hostBounds.getMinY());
		getVisual().setLayoutX(location.getX());
		getVisual().setLayoutY(location.getY());
	}

	@Override
	protected void registerAtVisualPartMap(IViewer viewer, VBox visual) {
	}

	@Override
	protected void doRemoveChildVisual(IVisualPart<? extends Node> child, int index) {
		getVisual().getChildren().remove(index);
	}

	@Override
	protected void unregisterFromVisualPartMap(IViewer viewer, VBox visual) {
	}

}
