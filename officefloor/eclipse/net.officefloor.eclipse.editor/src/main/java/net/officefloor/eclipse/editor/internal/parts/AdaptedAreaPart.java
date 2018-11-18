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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IResizableContentPart;
import org.eclipse.gef.mvc.fx.parts.ITransformableContentPart;

import javafx.scene.Node;
import javafx.scene.transform.Affine;
import net.officefloor.eclipse.editor.AdaptedArea;
import net.officefloor.model.Model;

/**
 * {@link IContentPart} for the
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedAreaPart<M extends Model> extends AbstractAdaptedConnectablePart<M, AdaptedArea<M>>
		implements ITransformableContentPart<Node>, IResizableContentPart<Node> {

	/**
	 * {@link TransformContent}.
	 */
	private TransformContent<M, AdaptedArea<M>> transformableContent;

	/*
	 * ===================== IContentPart =================================
	 */

	@Override
	public void init() {
		super.init();
		this.transformableContent = new TransformContent<>(this);
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		List<Object> children = new ArrayList<>();
		children.addAll(this.getContent().getAdaptedConnectors());
		return children;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Node createVisualNode() {
		return this.getContent().createVisual(new AdaptedModelVisualFactoryContextImpl<>(
				(Class<M>) this.getContent().getModel().getClass(), false, this.getConnectorLoader(), (action) -> {

					// Undertake the action
					this.getContent().action(action);
				}));
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
		return this.getContent().getDimension();
	}

	@Override
	public void setContentSize(Dimension totalSize) {
		this.getContent().setDimension(totalSize);
	}

}