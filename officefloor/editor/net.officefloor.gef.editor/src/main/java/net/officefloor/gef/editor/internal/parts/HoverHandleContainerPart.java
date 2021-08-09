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
