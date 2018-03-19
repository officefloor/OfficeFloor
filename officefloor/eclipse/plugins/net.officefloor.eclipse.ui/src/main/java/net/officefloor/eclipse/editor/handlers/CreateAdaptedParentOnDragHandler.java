/*******************************************************************************
 * Copyright (c) 2016, 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package net.officefloor.eclipse.editor.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.gef.common.adapt.AdapterKey;
import org.eclipse.gef.geometry.planar.Dimension;
import org.eclipse.gef.geometry.planar.Point;
import org.eclipse.gef.mvc.fx.domain.IDomain;
import org.eclipse.gef.mvc.fx.gestures.ClickDragGesture;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnDragHandler;
import org.eclipse.gef.mvc.fx.models.SelectionModel;
import org.eclipse.gef.mvc.fx.operations.DeselectOperation;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.policies.CreationPolicy;
import org.eclipse.gef.mvc.fx.policies.DeletionPolicy;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import com.google.common.collect.HashMultimap;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.ViewFactoryContext;
import net.officefloor.eclipse.editor.models.AdaptedConnector;
import net.officefloor.eclipse.editor.models.AdaptedPrototype;
import net.officefloor.eclipse.editor.models.ChildrenGroupFactory.ChildrenGroup;
import net.officefloor.eclipse.editor.parts.AdaptedParentPart;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

public class CreateAdaptedParentOnDragHandler<M extends Model> extends AbstractHandler implements IOnDragHandler {

	/**
	 * {@link AdaptedPrototype}.
	 */
	private AdaptedPrototype<M> prototype;

	/**
	 * {@link AdaptedParentPart}.
	 */
	private AdaptedParentPart<M> prototypePart;

	/**
	 * {@link IOnDragHandler} instances.
	 */
	private Map<AdapterKey<? extends IOnDragHandler>, IOnDragHandler> dragPolicies;

	/**
	 * Obtains the content {@link IViewer}.
	 * 
	 * @return Content {@link IViewer}.
	 */
	protected IViewer getContentViewer() {
		return getHost().getRoot().getViewer().getDomain()
				.getAdapter(AdapterKey.get(IViewer.class, IDomain.CONTENT_VIEWER_ROLE));
	}

	/**
	 * Obtains the location of the {@link MouseEvent}.
	 * 
	 * @param e
	 *            {@link MouseEvent}.
	 * @return Location of the {@link MouseEvent}.
	 */
	protected Point getLocation(MouseEvent e) {
		Point2D location = ((InfiniteCanvasViewer) getHost().getRoot().getViewer()).getCanvas().getContentGroup()
				.sceneToLocal(e.getSceneX(), e.getSceneY());
		return new Point(location.getX(), location.getY());
	}

	/**
	 * Completes the drag.
	 * 
	 * @param isCreateAdaptedParent
	 *            Indicates whether to create the {@link AdaptedParentPart}.
	 * @param event
	 *            {@link MouseEvent} at completion of drag.
	 */
	protected void completeDrag(boolean isCreateAdaptedParent, MouseEvent event) {

		// Obtain location to create parent
		Point location = this.getLocation(event);

		// Delete the proxy
		this.restoreRefreshVisuals(this.prototypePart);
		IRootPart<? extends Node> contentRoot = this.getContentViewer().getRootPart();
		DeletionPolicy deletionPolicy = contentRoot.getAdapter(DeletionPolicy.class);
		init(deletionPolicy);
		deletionPolicy.delete(this.prototypePart);
		commit(deletionPolicy);

		// Determine if create adapted parent
		if (isCreateAdaptedParent) {

			// Create the parent at the location
			this.prototype.newAdaptedParent(location);
		}

		// Clear state
		this.prototype = null;
		this.prototypePart = null;
		this.dragPolicies = null;
	}

	/*
	 * ==================== IOnDragHandler ===========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public AdaptedParentPart<M> getHost() {
		return (AdaptedParentPart<M>) super.getHost();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void startDrag(MouseEvent event) {

		// Create proxy to create parent at particular location
		AdaptedParentPart<M> parentPart = this.getHost();
		AdaptedParent<M> parent = parentPart.getContent();
		this.prototype = parentPart.getAdapter(AdaptedPrototype.class);
		if (this.prototype == null) {
			throw new IllegalStateException(AdaptedParent.class.getSimpleName() + " does not adapt to "
					+ AdaptedPrototype.class.getSimpleName() + " for model " + parent.getModel().getClass().getName());
		}
		ProxyCreateAdaptedParent proxy = new ProxyCreateAdaptedParent(parent, this.prototype);

		// Create part for proxy to visual parent in drag
		IRootPart<? extends Node> contentRoot = this.getContentViewer().getRootPart();
		CreationPolicy creationPolicy = contentRoot.getAdapter(CreationPolicy.class);
		init(creationPolicy);
		this.prototypePart = (AdaptedParentPart<M>) creationPolicy.create(proxy, contentRoot, HashMultimap.create());
		commit(creationPolicy);

		// Disable refresh
		this.storeAndDisableRefreshVisuals(this.prototypePart);

		// Deselect all but the new part
		List<IContentPart<? extends Node>> deselected = new ArrayList<>(
				this.getContentViewer().getAdapter(SelectionModel.class).getSelectionUnmodifiable());
		deselected.remove(this.prototypePart);
		DeselectOperation deselectOperation = new DeselectOperation(this.getContentViewer(), deselected);
		try {
			this.getHost().getRoot().getViewer().getDomain().execute(deselectOperation, new NullProgressMonitor());
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

		// Trigger drag start policies
		this.dragPolicies = this.prototypePart.getAdapters(ClickDragGesture.ON_DRAG_POLICY_KEY);
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.startDrag(event);
			}
		}
	}

	@Override
	public void drag(MouseEvent event, Dimension delta) {
		if (this.prototypePart == null) {
			return;
		}

		// Forward drag event
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.drag(event, delta);
			}
		}
	}

	@Override
	public void abortDrag() {
		if (this.prototypePart == null) {
			return;
		}

		// Forward drag event
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.abortDrag();
			}
		}

		// Finished drag
		this.completeDrag(false, null);
	}

	@Override
	public void endDrag(MouseEvent event, Dimension delta) {
		if (this.prototypePart == null) {
			return;
		}

		// Forward drag event
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.endDrag(event, delta);
			}
		}

		// Create the adapted parent at end drag location
		this.completeDrag(true, event);
	}

	@Override
	public void hideIndicationCursor() {
	}

	@Override
	public boolean showIndicationCursor(KeyEvent event) {
		return false;
	}

	@Override
	public boolean showIndicationCursor(MouseEvent event) {
		return false;
	}

	/**
	 * Proxy create {@link AdaptedParent} via {@link AdaptedPrototype}.
	 */
	private class ProxyCreateAdaptedParent implements AdaptedPrototype<M>, AdaptedParent<M> {

		/**
		 * {@link AdaptedParent}.
		 */
		private final AdaptedParent<M> parent;

		/**
		 * {@link AdaptedPrototype}.
		 */
		private final AdaptedPrototype<M> prototype;

		/**
		 * Instantiate.
		 * 
		 * @param parent
		 *            {@link AdaptedParent}.
		 * @param prototype
		 *            {@link AdaptedPrototype}.
		 */
		public ProxyCreateAdaptedParent(AdaptedParent<M> parent, AdaptedPrototype<M> prototype) {
			this.parent = parent;
			this.prototype = prototype;
		}

		/*
		 * ===================== AdaptedParent ===================
		 */

		@Override
		public void newAdaptedParent(Point location) {
			this.prototype.newAdaptedParent(location);
		}

		/*
		 * ===================== AdaptedParent ===================
		 */

		@Override
		public ReadOnlyStringProperty getLabel() {
			return this.parent.getLabel();
		}

		@Override
		public StringProperty getEditLabel() {
			return null;
		}

		@Override
		public List<AdaptedConnection<?>> getConnections() {
			return Collections.emptyList();
		}

		@Override
		public M getModel() {
			return this.parent.getModel();
		}

		@Override
		public void changeLocation(int x, int y) {
		}

		@Override
		public <T> T getAdapter(Class<T> classKey) {
			return this.parent.getAdapter(classKey);
		}

		@Override
		public List<ChildrenGroup<M, ?>> getChildrenGroups() {
			return this.parent.getChildrenGroups();
		}

		@Override
		public List<AdaptedConnector<M>> getAdaptedConnectors() {
			return this.parent.getAdaptedConnectors();
		}

		@Override
		public AdaptedConnector<M> getAdaptedConnector(Class<? extends ConnectionModel> connectionClass) {
			return this.parent.getAdaptedConnector(connectionClass);
		}

		@Override
		public Pane createVisual(ViewFactoryContext context) {
			return this.parent.createVisual(context);
		}
	}

}