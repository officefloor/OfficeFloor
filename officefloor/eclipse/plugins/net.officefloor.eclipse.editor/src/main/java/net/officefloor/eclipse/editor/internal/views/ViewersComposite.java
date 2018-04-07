/*******************************************************************************
 * Copyright (c) 2016 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package net.officefloor.eclipse.editor.internal.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.fx.nodes.InfiniteCanvas;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import javafx.scene.Parent;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.internal.behaviors.PaletteFocusBehavior;

/**
 * Composite of the viewers.
 *
 * @author Daniel Sagenschneider
 */
public class ViewersComposite {

	/**
	 * Palette indicator width.
	 */
	private static final double PALETTE_INDICATOR_WIDTH = 10.0;

	/**
	 * Composite containing the palette and editor.
	 */
	private final HBox composite;

	/**
	 * Instantiate.
	 * 
	 * @param contentViewer
	 *            {@link IViewer} for the editor.
	 * @param paletteViewer
	 *            {@link IViewer} for the palette.
	 * @param isCreateParents
	 *            Flag indicate whether able to create {@link AdaptedParent}
	 *            instances.
	 */
	public ViewersComposite(IViewer contentViewer, IViewer paletteViewer, boolean isCreateParents) {

		// Obtain the content root
		Parent contentRootNode = contentViewer.getCanvas();
		InfiniteCanvas paletteRootNode = ((InfiniteCanvasViewer) paletteViewer).getCanvas();

		// Arrange viewers above each other
		AnchorPane viewersPane = new AnchorPane();
		viewersPane.getChildren().addAll(contentRootNode, paletteRootNode);

		// Create palette indicator
		List<Pane> panes = new ArrayList<>(2);
		if (isCreateParents) {

			// Able to create parents, so provide palette
			Pane paletteIndicator = new Pane();
			paletteIndicator.setStyle("-fx-background-color: rgba(128,128,128,1);");
			paletteIndicator.setMaxSize(PALETTE_INDICATOR_WIDTH, Double.MAX_VALUE);
			paletteIndicator.setMinSize(PALETTE_INDICATOR_WIDTH, 0.0);
			panes.add(paletteIndicator);

			// Register listeners to show/hide palette
			paletteIndicator.setOnMouseEntered((event) -> paletteRootNode.setVisible(true));
			paletteRootNode.setOnMouseExited((event) -> paletteRootNode.setVisible(false));

			// Register listeners to update the palette width
			paletteRootNode.getContentGroup().layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
				double scrollBarWidth = paletteRootNode.getVerticalScrollBar().isVisible()
						? paletteRootNode.getVerticalScrollBar().getLayoutBounds().getWidth()
						: 0;
				paletteRootNode.setPrefWidth(newValue.getWidth() + scrollBarWidth);
			});
			paletteRootNode.getVerticalScrollBar().visibleProperty().addListener((observable, oldValue, newValue) -> {
				double contentWidth = paletteRootNode.getContentGroup().getLayoutBounds().getWidth();
				double scrollBarWidth = newValue ? paletteRootNode.getVerticalScrollBar().getLayoutBounds().getWidth()
						: 0;
				paletteRootNode.setPrefWidth(contentWidth + scrollBarWidth);
			});

			// Hide palette when a palette element is pressed
			paletteRootNode.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
				if (event.getTarget() != paletteRootNode) {
					paletteRootNode.setVisible(false);
				}
			});
		}
		panes.add(viewersPane);

		// Provide composite
		this.composite = new HBox();
		this.composite.setStyle("-fx-background-color: transparent;");
		this.composite.getChildren().addAll(panes);

		// Ensure composite fills the whole space
		this.composite.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.composite.setMinSize(0, 0);
		this.composite.setFillHeight(true);

		// No spacing between viewers and palette indicator
		this.composite.setSpacing(0.0);

		// Ensure viewers fill the space
		HBox.setHgrow(viewersPane, Priority.ALWAYS);
		viewersPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		AnchorPane.setBottomAnchor(contentRootNode, 0.0);
		AnchorPane.setLeftAnchor(contentRootNode, 0.0);
		AnchorPane.setRightAnchor(contentRootNode, 0.0);
		AnchorPane.setTopAnchor(contentRootNode, 0.0);
		AnchorPane.setBottomAnchor(paletteRootNode, 0.0);
		AnchorPane.setLeftAnchor(paletteRootNode, 0.0);
		AnchorPane.setTopAnchor(paletteRootNode, 0.0);

		// Configure palette
		paletteRootNode.setZoomGrid(false);
		paletteRootNode.setShowGrid(false);
		paletteRootNode.setHorizontalScrollBarPolicy(ScrollBarPolicy.NEVER);
		paletteRootNode.setStyle(PaletteFocusBehavior.DEFAULT_STYLE);
		if (!isCreateParents) {
			paletteRootNode.setVisible(false);
		}
	}

	/**
	 * Obtains the {@link Pane} containing the view.
	 * 
	 * @return {@link Pane} containing the view.
	 */
	public Pane getComposite() {
		return composite;
	}

}