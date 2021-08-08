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
