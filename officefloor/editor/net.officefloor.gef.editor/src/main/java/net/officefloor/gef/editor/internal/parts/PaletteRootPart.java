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
