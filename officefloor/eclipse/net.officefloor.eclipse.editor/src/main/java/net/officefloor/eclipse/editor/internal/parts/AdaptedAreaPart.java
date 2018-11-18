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

import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IResizableContentPart;
import org.eclipse.gef.mvc.fx.parts.ITransformableContentPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javafx.scene.Node;
import javafx.scene.transform.Affine;
import net.officefloor.eclipse.editor.AdaptedArea;
import net.officefloor.model.Model;

/**
 * {@link IContentPart} for the
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedAreaPart<M extends Model> extends AbstractAdaptedPart<M, AdaptedArea<M>, Node>
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
	protected SetMultimap<? extends Object, String> doGetContentAnchorages() {
		return HashMultimap.create();
	}

	@Override
	protected List<? extends Object> doGetContentChildren() {
		return Collections.emptyList();
	}

	@Override
	protected Node doCreateVisual() {
		return this.getContent().createVisual(
				new AdaptedModelVisualFactoryContextImpl<>((Class<M>) this.getContent().getModel().getClass(), false,
						(connectionClasses, role, assocations, node) -> {

//							// Load the connectors for the connection classes
//							for (Class<?> connectionClass : connectionClasses) {
//
//								// Obtain the adapted connector
//								AdaptedConnector<M> connector = AdaptedChildPart.this.getContent()
//										.getAdaptedConnector((Class<? extends ConnectionModel>) connectionClass, role);
//								if (connector == null) {
//									throw new IllegalStateException("Connection " + connectionClass.getName()
//											+ " not configured to connect to model "
//											+ AdaptedChildPart.this.getContent().getModel().getClass().getName());
//								}
//
//								// Obtain the visual
//								AdaptedConnectorVisual visual = AdaptedChildPart.this.adaptedConnectorVisuals
//										.get(connector);
//								if (visual.node != null) {
//									throw new IllegalStateException("Connection " + connectionClass.getName()
//											+ " configured more than once for model "
//											+ AdaptedChildPart.this.getContent().getModel().getClass().getName());
//								}
//
//								// Load the connector visual
//								visual.node = node;
//
//								// Associate the connectors
//								assocations.add(connector);
//								connector.setAssociation(assocations, role);
//							}

						}, (action) -> {

//							// Undertake the action
//							this.getContent().action(action);
						}));
	}

	@Override
	protected void doRefreshVisual(Node visual) {
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
		return this.getContent().getDimension();
	}

	@Override
	public void setContentSize(Dimension totalSize) {
		this.getContent().setDimension(totalSize);
	}

}