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

import org.eclipse.gef.geometry.convert.fx.FX2Geometry;
import org.eclipse.gef.geometry.convert.fx.Geometry2FX;
import org.eclipse.gef.geometry.planar.AffineTransform;
import org.eclipse.gef.mvc.fx.parts.ITransformableContentPart;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import javafx.geometry.Bounds;
import javafx.scene.transform.Affine;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.model.Model;

/**
 * {@link ITransformableContentPart} implementation methods.
 * 
 * @author Daniel Sagenschneider
 */
public class TransformContent<M extends Model, A extends AdaptedModel<M>> {

	/**
	 * {@link AbstractAdaptedPart}.
	 */
	private final AbstractAdaptedPart<M, A, ?> part;

	/**
	 * {@link AffineTransform} for location of the {@link AdaptedParent}.
	 */
	private AffineTransform contentTransform = null;

	/**
	 * Instantiate.
	 * 
	 * @param part {@link AbstractAdaptedPart}.
	 */
	public TransformContent(AbstractAdaptedPart<M, A, ?> part) {
		this.part = part;

		// Capture the initial location
		M model = this.part.getContent().getModel();
		this.contentTransform = new AffineTransform(1, 0, 0, 1, model.getX(), model.getY());
	}

	/*
	 * =================== ITransformableContentPart ===========================
	 */

	public Affine getContentTransform() {
		return Geometry2FX.toFXAffine(this.contentTransform);
	}

	public void setContentTransform(Affine totalTransform) {
		this.contentTransform = FX2Geometry.toAffineTransform(totalTransform);

		// Determine the location
		Bounds boundsInScene = this.part.getVisual().localToScene(this.part.getVisual().getLayoutBounds());
		Bounds boundsInParent = ((InfiniteCanvasViewer) this.part.getRoot().getViewer()).getCanvas()
				.getScrolledOverlayGroup().sceneToLocal(boundsInScene);

		// Obtain the location
		int x = (int) boundsInParent.getMinX();
		int y = (int) boundsInParent.getMinY();

		// Update location on model (as already within change location operation)
		this.part.getContent().getModel().setX(x);
		this.part.getContent().getModel().setY(y);
	}

}
