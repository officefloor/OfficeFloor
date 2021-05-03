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

import java.util.Collections;
import java.util.List;

import org.eclipse.gef.fx.nodes.GeometryNode;
import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.geometry.planar.RoundedRectangle;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IResizableContentPart;
import org.eclipse.gef.mvc.fx.parts.ITransformableContentPart;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import net.officefloor.gef.editor.AdaptedArea;
import net.officefloor.gef.editor.AdaptedModelVisualFactory;
import net.officefloor.gef.editor.ParentToAreaConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link IContentPart} for the
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedAreaPart<M extends Model> extends AbstractAdaptedConnectablePart<M, AdaptedArea<M>>
		implements AdaptedConnectablePart, ITransformableContentPart<Node>, IResizableContentPart<Node> {

	/**
	 * {@link TransformContent}.
	 */
	private TransformContent<M, AdaptedArea<M>> transformableContent;

	/*
	 * =============== AdaptedConnectablePart ============================
	 */

	@Override
	public void setActiveConnector(boolean isActive) {
		// no active connector
	}

	/*
	 * ===================== IContentPart =================================
	 */

	@Override
	public void init() {
		super.init();
		this.transformableContent = new TransformContent<>(this);
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
	protected List<Object> doGetContentChildren() {
		return Collections.emptyList();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Node createVisualNode() {

		// Create the view factory
		Dimension minimum = this.getContent().getMinimumDimension();
		AdaptedModelVisualFactory<M> viewFactory = (model, context) -> {
			GeometryNode<RoundedRectangle> node = new GeometryNode<>(new RoundedRectangle(200, 100, 200, 100, 5, 5));
			context.connector((visualContext) -> node, ParentToAreaConnectionModel.class).getNode();
			node.setMinWidth(minimum.width);
			node.setMinHeight(minimum.height);
			node.setFill(Color.KHAKI);
			node.setStroke(Color.KHAKI);
			return node;
		};

		// Create and return the view
		GeometryNode<RoundedRectangle> visual = (GeometryNode<RoundedRectangle>) viewFactory.createVisual(
				this.getContent().getModel(),
				new AdaptedModelVisualFactoryContextImpl<>((Class<M>) this.getContent().getModel().getClass(), false,
						this.getConnectorLoader(), (action) -> {

							// Undertake the action
							this.getContent().action(action);
						}));

		// Specify the initial location
		M model = this.getContent().getModel();
		visual.setLayoutX(model.getX());
		visual.setLayoutY(model.getY());

		// Specify the initial dimension
		Dimension dimension = this.getContent().getDimension();
		visual.setPrefWidth(dimension.width);
		visual.setPrefHeight(dimension.height);

		// Return the visual
		return visual;
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
