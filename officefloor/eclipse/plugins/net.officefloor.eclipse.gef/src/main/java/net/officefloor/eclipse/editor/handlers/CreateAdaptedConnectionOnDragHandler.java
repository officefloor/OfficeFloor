/*******************************************************************************
 * Copyright (c) 2015, 2017 itemis AG and others.
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
import org.eclipse.gef.mvc.fx.parts.CircleSegmentHandlePart;
import org.eclipse.gef.mvc.fx.parts.IBendableContentPart.BendPoint;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IRootPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.policies.CreationPolicy;
import org.eclipse.gef.mvc.fx.policies.DeletionPolicy;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multiset;

import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import net.officefloor.eclipse.editor.models.AdaptedConnector;
import net.officefloor.eclipse.editor.models.ProxyAdaptedConnection;
import net.officefloor.eclipse.editor.parts.AdaptedConnectionPart;
import net.officefloor.eclipse.editor.parts.AdaptedConnectorPart;

public class CreateAdaptedConnectionOnDragHandler extends AbstractHandler implements IOnDragHandler {

	/**
	 * Source {@link AdaptedConnectorPart}.
	 */
	private AdaptedConnectorPart sourceConnector;

	/**
	 * {@link ProxyAdaptedConnection}.
	 */
	private ProxyAdaptedConnection connection;

	/**
	 * {@link AdaptedConnectionPart} for the new {@link ProxyAdaptedConnection}.
	 */
	private AdaptedConnectionPart<?> connectionPart;

	/**
	 * Target {@link BendPoint} for dragging the {@link ProxyAdaptedConnection}.
	 */
	private CircleSegmentHandlePart bendTargetPart;

	/**
	 * Additional {@link IOnDragHandler} instances.
	 */
	private Map<AdapterKey<? extends IOnDragHandler>, IOnDragHandler> dragPolicies;

	/**
	 * Obtains the {@link AdaptedConnectorPart} for the start of the drag.
	 * 
	 * @return {@link AdaptedConnectorPart}.
	 */
	protected AdaptedConnectorPart getAdaptedConnectorPart() {
		return (AdaptedConnectorPart) getHost();
	}

	/**
	 * Obtains the location of the {@link MouseEvent}.
	 * 
	 * @param e
	 *            {@link MouseEvent}.
	 * @return {@link Point} location of the {@link MouseEvent}.
	 */
	protected Point getLocation(MouseEvent e) {
		// Viewer potentially null
		if (getHost().getViewer() == null) {
			return new Point(e.getSceneX(), e.getSceneY());
		}
		Point2D location = ((InfiniteCanvasViewer) getHost().getRoot().getViewer()).getCanvas().getContentGroup()
				.sceneToLocal(e.getSceneX(), e.getSceneY());
		return new Point(location.getX(), location.getY());
	}

	/**
	 * Finds the {@link CircleSegmentHandlePart} for the target {@link BendPoint}.
	 * 
	 * @param connectionPart
	 *            {@link AdaptedConnectionPart}.
	 * @param eventTarget
	 *            {@link EventTarget}.
	 * @return Target {@link CircleSegmentHandlePart}.
	 */
	protected CircleSegmentHandlePart findBendTargetPart(AdaptedConnectionPart<?> connectionPart,
			EventTarget eventTarget) {
		// Find last segment handle part
		Multiset<IVisualPart<? extends Node>> anchoreds = connectionPart.getAnchoredsUnmodifiable();
		for (IVisualPart<? extends Node> anchored : anchoreds) {
			if (anchored instanceof CircleSegmentHandlePart) {
				CircleSegmentHandlePart circleSegmentHandlePart = (CircleSegmentHandlePart) anchored;
				if (circleSegmentHandlePart.getSegmentParameter() == 1.0) {
					return circleSegmentHandlePart;
				}
			}
		}
		throw new IllegalStateException("Unable to obtain bend target part on creating new connection");
	}

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
	 * Cleans up the drag.
	 */
	protected void cleanupDrag() {

		// Remove the connection
		restoreRefreshVisuals(this.connectionPart);
		IRootPart<? extends Node> contentRoot = this.getContentViewer().getRootPart();
		DeletionPolicy deletionPolicy = contentRoot.getAdapter(DeletionPolicy.class);
		init(deletionPolicy);
		deletionPolicy.delete(this.connectionPart);
		commit(deletionPolicy);

		// Connector no longer active
		this.sourceConnector.setActiveConnector(false);

		// Clear drag details
		this.sourceConnector = null;
		this.connectionPart = null;
		this.bendTargetPart = null;
		this.dragPolicies = null;
	}

	/*
	 * ==================== IOnDragHandler =========================
	 */

	@Override
	public void startDrag(MouseEvent event) {

		// Create the proxy connection
		this.sourceConnector = this.getAdaptedConnectorPart();
		this.connection = new ProxyAdaptedConnection(sourceConnector.getContent());

		// Register the connector as active
		this.sourceConnector.setActiveConnector(true);

		// Create using CreationPolicy from root part
		CreationPolicy creationPolicy = getHost().getRoot().getAdapter(CreationPolicy.class);
		init(creationPolicy);
		this.connectionPart = (AdaptedConnectionPart<?>) creationPolicy.create(this.connection, getHost().getRoot(),
				HashMultimap.<IContentPart<? extends Node>, String>create());
		commit(creationPolicy);

		// Disable refresh visuals for the connection
		storeAndDisableRefreshVisuals(this.connectionPart);

		// Move connection to pointer location
		this.connectionPart.getVisual().setEndPoint(this.getLocation(event));

		// Build operation to deselect all but the new connection part
		List<IContentPart<? extends Node>> deselected = new ArrayList<>(
				getHost().getRoot().getViewer().getAdapter(SelectionModel.class).getSelectionUnmodifiable());
		deselected.remove(this.connectionPart);
		DeselectOperation deselectOperation = new DeselectOperation(getHost().getRoot().getViewer(), deselected);
		try {
			getHost().getRoot().getViewer().getDomain().execute(deselectOperation, new NullProgressMonitor());
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

		// Find bend target part
		this.bendTargetPart = this.findBendTargetPart(this.connectionPart, event.getTarget());
		if (this.bendTargetPart != null) {
			this.dragPolicies = this.bendTargetPart.getAdapters(ClickDragGesture.ON_DRAG_POLICY_KEY);
		}
		if (this.dragPolicies != null) {
			MouseEvent dragEvent = new MouseEvent(event.getSource(), event.getTarget(), MouseEvent.MOUSE_DRAGGED,
					event.getX(), event.getY(), event.getScreenX(), event.getScreenY(), event.getButton(),
					event.getClickCount(), event.isShiftDown(), event.isControlDown(), event.isAltDown(),
					event.isMetaDown(), event.isPrimaryButtonDown(), event.isMiddleButtonDown(),
					event.isSecondaryButtonDown(), event.isSynthesized(), event.isPopupTrigger(),
					event.isStillSincePress(), event.getPickResult());
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.startDrag(event);
				dragPolicy.drag(dragEvent, new Dimension());
			}
		}
	}

	@Override
	public void drag(MouseEvent event, Dimension delta) {
		if (this.bendTargetPart == null) {
			return;
		}

		// Forward events
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.drag(event, delta);
			}
		}
	}

	@Override
	public void abortDrag() {
		if (this.bendTargetPart == null) {
			return;
		}

		// Forward events
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.abortDrag();
			}
		}

		// Clean up the drag
		this.cleanupDrag();
	}

	@Override
	public void endDrag(MouseEvent e, Dimension delta) {
		if (this.bendTargetPart == null) {
			return;
		}

		// Forward events
		if (this.dragPolicies != null) {
			for (IOnDragHandler dragPolicy : this.dragPolicies.values()) {
				dragPolicy.endDrag(e, delta);
			}
		}

		// Obtain the target
		BendPoint targetBendPoint = this.connection.getTargetBendPoint();
		AdaptedConnector<?> target = (AdaptedConnector<?>) targetBendPoint.getContentAnchorage();
		if (target != null) {

			// Have target so create the connection
			AdaptedConnector<?> source = this.connection.getSourceAdaptedConnector();
			source.getParentAdaptedChild().createConnection(target.getParentAdaptedChild());
		}

		// Clearn up the drag
		this.cleanupDrag();
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

}