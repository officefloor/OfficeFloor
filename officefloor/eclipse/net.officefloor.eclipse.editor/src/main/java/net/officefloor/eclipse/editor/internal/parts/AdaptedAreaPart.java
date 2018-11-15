/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor.internal.parts;

import java.util.Collections;
import java.util.List;

import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.geometry.planar.RoundedRectangle;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IResizableContentPart;
import org.eclipse.gef.mvc.fx.parts.ITransformableContentPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import net.officefloor.eclipse.editor.AdaptedArea;
import net.officefloor.model.Model;

/**
 * {@link IContentPart} for the
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedAreaPart<M extends Model>
		extends AbstractAdaptedPart<M, AdaptedArea<M>, GeometryNode<RoundedRectangle>>
		implements ITransformableContentPart<GeometryNode<RoundedRectangle>>,
		IResizableContentPart<GeometryNode<RoundedRectangle>> {

	/**
	 * {@link TransformContent}.
	 */
	private TransformContent<M, AdaptedArea<M>, GeometryNode<RoundedRectangle>> transformableContent;

	private Dimension dimension = new Dimension(100, 100);

	/*
	 * ===================== IContentPart =================================
	 */

	@Override
	public void init() {
		super.init();
		this.transformableContent = new TransformContent<>(this);
	}

	@Override
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		return Collections.emptyList();
	}

	@Override
	protected GeometryNode<RoundedRectangle> doCreateVisual() {

		RoundedRectangle rectangle = new RoundedRectangle(200, 100, 500, 400, 5, 5);
		GeometryNode<RoundedRectangle> node = new GeometryNode<RoundedRectangle>(rectangle);
		node.setMinWidth(100);
		node.setMinHeight(50);
		node.strokeProperty().set(Color.KHAKI);
		node.fillProperty().set(Color.KHAKI);

		return node;
	}

	@Override
	protected void doRefreshVisual(GeometryNode<RoundedRectangle> visual) {
		// nothing to update
	}

	/*
	 * ================= ITransformableContentPart ==========================
	 */

	@Override
	public Affine getContentTransform() {
		return this.transformableContent.getContentTransform();
	}

	@Override
	public void setContentTransform(Affine totalTransform) {
		this.transformableContent.setContentTransform(totalTransform);
	}

	/*
	 * ================= IResizableContentPart ==========================
	 */

	@Override
	public Dimension getContentSize() {
		return this.dimension;
	}

	@Override
	public void setContentSize(Dimension totalSize) {
		this.dimension = totalSize;
	}

}