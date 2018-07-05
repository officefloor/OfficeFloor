/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor.internal.parts;

import org.eclipse.gef.geometry.convert.fx.FX2Geometry;
import org.eclipse.gef.geometry.convert.fx.Geometry2FX;
import org.eclipse.gef.geometry.planar.AffineTransform;
import org.eclipse.gef.mvc.fx.parts.ITransformableContentPart;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.transform.Affine;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.model.Model;

public class AdaptedParentPart<M extends Model> extends AdaptedChildPart<M, AdaptedParent<M>>
		implements ITransformableContentPart<Node> {

	/**
	 * Loads the styling to the visual {@link Node}.
	 * 
	 * @param visualNode
	 *            Visual {@link Node}.
	 */
	public static void loadStyling(Node visualNode) {
		visualNode.getStyleClass().remove("child");
		visualNode.getStyleClass().add("parent");
	}

	/**
	 * {@link AffineTransform} for location of the {@link AdaptedParent}.
	 */
	private AffineTransform contentTransform = null;

	@Override
	public void init() {
		super.init();

		// Capture the initial location
		M model = this.getContent().getModel();
		this.contentTransform = new AffineTransform(1, 0, 0, 1, model.getX(), model.getY());
	}

	@Override
	public <T> T getAdapter(Class<T> classKey) {

		// Determine if can adapt
		T adapter = this.getContent().getAdapter(classKey);
		if (adapter != null) {
			return adapter;
		}

		// Inherit adapters
		return super.getAdapter(classKey);
	}

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	public Node doCreateVisual() {

		// Flag as palette prototype (if one)
		if (this.getContent().isPalettePrototype()) {
			this.isPalettePrototype = true;
		}

		// Obtain the visual
		Node container = super.doCreateVisual();

		// Provide parent styling
		loadStyling(container);

		// Specify the initial location
		M model = this.getContent().getModel();
		container.setLayoutX(model.getX());
		container.setLayoutY(model.getY());

		// Return the pane
		return container;
	}

	@Override
	public Affine getContentTransform() {
		return Geometry2FX.toFXAffine(this.contentTransform);
	}

	@Override
	public void setContentTransform(Affine totalTransform) {
		this.contentTransform = FX2Geometry.toAffineTransform(totalTransform);

		// Determine the location
		Bounds boundsInScene = this.getVisual().localToScene(this.getVisual().getLayoutBounds());
		Bounds boundsInParent = ((InfiniteCanvasViewer) getRoot().getViewer()).getCanvas().getScrolledOverlayGroup()
				.sceneToLocal(boundsInScene);

		// Obtain the location
		int x = (int) boundsInParent.getMinX();
		int y = (int) boundsInParent.getMinY();

		// Update location on model (as already within change location operation)
		this.getContent().getModel().setX(x);
		this.getContent().getModel().setY(y);
	}

}