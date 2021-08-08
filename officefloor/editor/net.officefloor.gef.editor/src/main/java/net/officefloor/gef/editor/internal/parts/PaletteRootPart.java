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

import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.parts.LayeredRootPart;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A specific root part for the palette viewer.
 *
 * @author Alexander Nyßen
 */
public class PaletteRootPart extends LayeredRootPart {

	@Override
	protected Group createContentLayer() {
		Group contentLayer = super.createContentLayer();
		VBox vbox = new VBox();
		vbox.setPickOnBounds(true);
		// define padding and spacing
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(10d);
		// fixed at top/right position
		vbox.setAlignment(Pos.TOP_LEFT);
		contentLayer.getChildren().add(vbox);
		return contentLayer;
	}

	@Override
	protected void doAddChildVisual(IVisualPart<? extends Node> child, int index) {
		if (child instanceof IContentPart) {
			int contentLayerIndex = 0;
			for (int i = 0; i < index; i++) {
				if (i < getChildrenUnmodifiable().size() && getChildrenUnmodifiable().get(i) instanceof IContentPart) {
					contentLayerIndex++;
				}
			}
			((VBox) getContentLayer().getChildren().get(0)).getChildren().add(contentLayerIndex,
					new Group(child.getVisual()));
		} else {
			super.doAddChildVisual(child, index);
		}
	}

	@Override
	protected void doRemoveChildVisual(IVisualPart<? extends Node> child, int index) {
		if (child instanceof IContentPart) {
			((VBox) getContentLayer().getChildren().get(0)).getChildren().remove(index);
		} else {
			super.doRemoveChildVisual(child, index);
		}
	}
}
