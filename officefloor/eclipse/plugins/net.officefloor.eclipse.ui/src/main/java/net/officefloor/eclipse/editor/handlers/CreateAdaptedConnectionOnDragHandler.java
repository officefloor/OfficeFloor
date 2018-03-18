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
import org.eclipse.gef.mvc.fx.gestures.ClickDragGesture;
import org.eclipse.gef.mvc.fx.handlers.AbstractHandler;
import org.eclipse.gef.mvc.fx.handlers.IOnDragHandler;
import org.eclipse.gef.mvc.fx.models.SelectionModel;
import org.eclipse.gef.mvc.fx.operations.DeselectOperation;
import org.eclipse.gef.mvc.fx.parts.CircleSegmentHandlePart;
import org.eclipse.gef.mvc.fx.parts.IContentPart;
import org.eclipse.gef.mvc.fx.parts.IVisualPart;
import org.eclipse.gef.mvc.fx.policies.CreationPolicy;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multiset;

import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.models.ProxyAdaptedConnection;
import net.officefloor.eclipse.editor.parts.AdaptedConnectionPart;
import net.officefloor.eclipse.editor.parts.AdaptedConnectorPart;

public class CreateAdaptedConnectionOnDragHandler extends AbstractHandler implements IOnDragHandler {

	public static final double GEF_STROKE_WIDTH = 3.5;
	public static final Color GEF_COLOR_GREEN = Color.rgb(99, 123, 71);
	public static final Double[] GEF_DASH_PATTERN = new Double[] { 13d, 8d };

	private CircleSegmentHandlePart bendTargetPart;
	private Map<AdapterKey<? extends IOnDragHandler>, IOnDragHandler> dragPolicies;
	private AdaptedConnectionPart<?> connectionPart;

	@Override
	public void abortDrag() {
		if (bendTargetPart == null) {
			return;
		}

		// forward event to bend target part
		if (dragPolicies != null) {
			for (IOnDragHandler dragPolicy : dragPolicies.values()) {
				dragPolicy.abortDrag();
			}
		}

		restoreRefreshVisuals(connectionPart);
		connectionPart = null;
		bendTargetPart = null;
		dragPolicies = null;
	}

	@Override
	public void drag(MouseEvent event, Dimension delta) {
		if (bendTargetPart == null) {
			return;
		}

		// forward drag events to bend target part
		if (dragPolicies != null) {
			for (IOnDragHandler dragPolicy : dragPolicies.values()) {
				dragPolicy.drag(event, delta);
			}
		}
	}

	@Override
	public void endDrag(MouseEvent e, Dimension delta) {
		if (bendTargetPart == null) {
			return;
		}

		// forward event to bend target part
		if (dragPolicies != null) {
			for (IOnDragHandler dragPolicy : dragPolicies.values()) {
				dragPolicy.endDrag(e, delta);
			}
		}

		restoreRefreshVisuals(connectionPart);
		connectionPart = null;
		bendTargetPart = null;
		dragPolicies = null;
	}

	protected CircleSegmentHandlePart findBendTargetPart(AdaptedConnectionPart<?> connectionPart,
			EventTarget eventTarget) {
		// find last segment handle part
		Multiset<IVisualPart<? extends Node>> anchoreds = connectionPart.getAnchoredsUnmodifiable();
		for (IVisualPart<? extends Node> anchored : anchoreds) {
			if (anchored instanceof CircleSegmentHandlePart) {
				CircleSegmentHandlePart circleSegmentHandlePart = (CircleSegmentHandlePart) anchored;
				if (circleSegmentHandlePart.getSegmentParameter() == 1.0) {
					return circleSegmentHandlePart;
				}
			}
		}
		throw new IllegalStateException("Cannot find bend target part.");
	}

	protected Point getLocation(MouseEvent e) {
		// XXX: Viewer may be null if the host is removed in the same pass in
		// which the event is forwarded.
		if (getHost().getViewer() == null) {
			return new Point(e.getSceneX(), e.getSceneY());
		}
		// FIXME: Prevent invocation of interaction policies when their host
		// does not have a link to the viewer.
		Point2D location = ((InfiniteCanvasViewer) getHost().getRoot().getViewer()).getCanvas().getContentGroup()
				.sceneToLocal(e.getSceneX(), e.getSceneY());
		return new Point(location.getX(), location.getY());
	}

	protected AdaptedConnectorPart getAdaptedConnectorPart() {
		return (AdaptedConnectorPart) getHost().getAnchoragesUnmodifiable().keySet().iterator().next();
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

	@Override
	public void startDrag(MouseEvent event) {
		
		System.out.println("TODO REMOVE startDrag " + this.getClass().getName());
		
		// create new connection
		AdaptedChild<?> child = this.getAdaptedConnectorPart().getContent().getParentAdaptedChild();
		AdaptedConnection<?> connection = new ProxyAdaptedConnection(child);

		// create using CreationPolicy from root part
		CreationPolicy creationPolicy = getHost().getRoot().getAdapter(CreationPolicy.class);
		init(creationPolicy);
		connectionPart = (AdaptedConnectionPart) creationPolicy.create(connection, getHost().getRoot(),
				HashMultimap.<IContentPart<? extends Node>, String>create());
		commit(creationPolicy);

		// disable refresh visuals for the curvePart
		storeAndDisableRefreshVisuals(connectionPart);

		// move curve to pointer location
		connectionPart.getVisual().setEndPoint(getLocation(event));

		// build operation to deselect all but the new curve part
		List<IContentPart<? extends Node>> toBeDeselected = new ArrayList<>(
				getHost().getRoot().getViewer().getAdapter(SelectionModel.class).getSelectionUnmodifiable());
		toBeDeselected.remove(connectionPart);
		DeselectOperation deselectOperation = new DeselectOperation(getHost().getRoot().getViewer(), toBeDeselected);
		// execute on stack
		try {
			getHost().getRoot().getViewer().getDomain().execute(deselectOperation, new NullProgressMonitor());
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

		// find bend target part
		bendTargetPart = findBendTargetPart(connectionPart, event.getTarget());
		if (bendTargetPart != null) {
			dragPolicies = bendTargetPart.getAdapters(ClickDragGesture.ON_DRAG_POLICY_KEY);
		}
		if (dragPolicies != null) {
			MouseEvent dragEvent = new MouseEvent(event.getSource(), event.getTarget(), MouseEvent.MOUSE_DRAGGED,
					event.getX(), event.getY(), event.getScreenX(), event.getScreenY(), event.getButton(),
					event.getClickCount(), event.isShiftDown(), event.isControlDown(), event.isAltDown(),
					event.isMetaDown(), event.isPrimaryButtonDown(), event.isMiddleButtonDown(),
					event.isSecondaryButtonDown(), event.isSynthesized(), event.isPopupTrigger(),
					event.isStillSincePress(), event.getPickResult());
			for (IOnDragHandler dragPolicy : dragPolicies.values()) {
				dragPolicy.startDrag(event);
				// XXX: send initial drag event so that the end position is set
				dragPolicy.drag(dragEvent, new Dimension());
			}
		}
	}
}
