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

package net.officefloor.gef.editor.internal.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.fx.nodes.InfiniteCanvas;
import org.eclipse.gef.mvc.fx.viewer.IViewer;
import org.eclipse.gef.mvc.fx.viewer.InfiniteCanvasViewer;

import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.officefloor.gef.editor.AdaptedEditorModule;
import net.officefloor.gef.editor.AdaptedErrorHandler;
import net.officefloor.gef.editor.AdaptedParent;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.editor.internal.behaviors.PaletteFocusBehavior;

/**
 * Composite of the viewers.
 *
 * @author Daniel Sagenschneider
 */
public class ViewersComposite implements AdaptedErrorHandler {

	/**
	 * Palette indicator width.
	 */
	private static final double PALETTE_INDICATOR_WIDTH = 10.0;

	/**
	 * {@link IViewer} for the content.
	 */
	private final IViewer contentViewer;

	/**
	 * Palette indicator.
	 */
	private final Pane paletteIndicator = new Pane();

	/**
	 * {@link IViewer} for the palette.
	 */
	private final IViewer paletteViewer;

	/**
	 * {@link SelectOnly}. May be <code>null</code>.
	 */
	private final SelectOnly selectOnly;

	/**
	 * Composite for the view.
	 */
	private final VBox composite = new VBox();

	/**
	 * Header for the error.
	 */
	private final GridPane errorHeader = new GridPane();

	/**
	 * Label for the error.
	 */
	private final Label errorLabel = new Label();

	/**
	 * Dismisses the error.
	 */
	private final Hyperlink dismissError = new Hyperlink("dismiss");

	/**
	 * Editor with stack trace.
	 */
	private final SplitPane editorWithStackTrace = new SplitPane();

	/**
	 * Toggle for showing the stack trace.
	 */
	private final Hyperlink stackTraceToggle = new Hyperlink();

	/**
	 * Indicate if showing stack trace.
	 */
	private boolean isShowingStackTrace = false;

	/**
	 * {@link TextArea} to display the stack trace.
	 */
	private final TextArea stackTrace = new TextArea();

	/**
	 * Indicates loading the editor, so can not yet report errors.
	 */
	private boolean isLoading = true;

	/**
	 * Instantiate.
	 * 
	 * @param contentViewer {@link IViewer} for the editor.
	 * @param paletteViewer {@link IViewer} for the palette.
	 * @param selectOnly    {@link SelectOnly}. May be <code>null</code>.
	 */
	public ViewersComposite(IViewer contentViewer, IViewer paletteViewer, SelectOnly selectOnly) {
		this.contentViewer = contentViewer;
		this.paletteViewer = paletteViewer;
		this.selectOnly = selectOnly;
	}

	/**
	 * Initialises.
	 * 
	 * @param isCreateParents Flag indicate whether able to create
	 *                        {@link AdaptedParent} instances.
	 */
	public void init(boolean isCreateParents) {

		// Obtain the content root (and it's Pane)
		InfiniteCanvas contentRootNode = ((InfiniteCanvasViewer) this.contentViewer).getCanvas();
		Pane contentPane = null;
		for (Node child : contentRootNode.getChildrenUnmodifiable()) {
			if (child instanceof Pane) {
				contentPane = (Pane) child;
			}
		}
		contentPane.getStyleClass().add("content");

		// Obtain the palette root (and it's Pane)
		InfiniteCanvas paletteRootNode = ((InfiniteCanvasViewer) this.paletteViewer).getCanvas();
		Pane palettePane = null;
		for (Node child : paletteRootNode.getChildrenUnmodifiable()) {
			if (child instanceof Pane) {
				palettePane = (Pane) child;
			}
		}
		palettePane.getStyleClass().add("palette");

		// Keep palette pane same size as canvas (ease styling)
		palettePane.prefHeightProperty().bind(paletteRootNode.heightProperty());

		// Arrange viewers above each other
		AnchorPane viewersPane = new AnchorPane();
		viewersPane.getChildren().addAll(contentRootNode, paletteRootNode);

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

		// Create palette indicator
		List<Pane> panes = new ArrayList<>(2);
		if (isCreateParents) {

			// Able to create parents, so provide palette
			this.paletteIndicator.getStyleClass().add("palette-indicator");
			this.paletteIndicator.setMaxSize(PALETTE_INDICATOR_WIDTH, Double.MAX_VALUE);
			this.paletteIndicator.setMinSize(PALETTE_INDICATOR_WIDTH, 0.0);
			panes.add(this.paletteIndicator);

			// Register listeners to show/hide palette (if not select only mode)
			if (this.selectOnly == null) {
				this.paletteIndicator.setOnMouseEntered((event) -> paletteRootNode.setVisible(true));
				paletteRootNode.setOnMouseExited((event) -> paletteRootNode.setVisible(false));
			}

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
			if (this.selectOnly == null) {
				// No select only, so click hides
				paletteRootNode.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
					if (event.getTarget() != paletteRootNode) {
						paletteRootNode.setVisible(false);
					}
				});
			}
		}
		panes.add(viewersPane);

		// Provide composite
		HBox editor = new HBox();
		editor.getStyleClass().add("editor");
		editor.getChildren().addAll(panes);

		// Ensure composite fills the whole space
		editor.setMinSize(0, 0);
		editor.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		editor.setFillHeight(true);
		editor.setSpacing(0.0); // no spacing between palette and content

		// Load the editor into the split pane (allow viewing stack trace)
		this.editorWithStackTrace.setOrientation(Orientation.HORIZONTAL);
		this.editorWithStackTrace.getItems().add(editor);

		// Configure the error details
		HBox errorDetails = new HBox(10.0);
		errorDetails.getStyleClass().setAll("header-panel");
		final String errorImagePath = AdaptedEditorModule.class.getPackage().getName().replace('.', '/') + "/error.png";
		final ImageView errorImage = new ImageView(new Image(errorImagePath, 15, 15, true, true));
		this.errorLabel.alignmentProperty().setValue(Pos.CENTER_LEFT);
		this.dismissError.getStyleClass().add("dismiss-error");
		this.dismissError.setOnAction((event) -> this.showError((String) null));
		errorDetails.getChildren().setAll(errorImage, this.errorLabel, this.dismissError);

		// Provide stack trace toggle
		this.stackTraceToggle.setText("Show Stack Trace");
		this.stackTraceToggle.getStyleClass().setAll("details-button", "more");
		this.stackTraceToggle.setVisible(this.isShowingStackTrace);
		this.stackTraceToggle.setOnAction((event) -> {
			if (this.isShowingStackTrace) {
				// Hide stack trace
				this.editorWithStackTrace.getItems().remove(this.stackTrace);
				this.stackTraceToggle.setText("Show Stack Trace");
				this.stackTraceToggle.getStyleClass().setAll("details-button", "more");
				this.isShowingStackTrace = false;
			} else {
				// Show stack trace
				if (!this.editorWithStackTrace.getItems().contains(this.stackTrace)) {
					this.editorWithStackTrace.getItems().add(this.stackTrace);
				}
				this.stackTraceToggle.setText("Hide Stack Trace");
				this.stackTraceToggle.getStyleClass().setAll("details-button", "less");
				this.isShowingStackTrace = true;
			}
		});
		HBox stackTraceToggleContainer = new HBox();
		stackTraceToggleContainer.getChildren().add(this.stackTraceToggle);
		stackTraceToggleContainer.getStyleClass().setAll("container");
		stackTraceToggleContainer.alignmentProperty().setValue(Pos.CENTER_RIGHT);
		HBox stackTraceToggleButtonBar = new HBox();
		stackTraceToggleButtonBar.getStyleClass().setAll("header-panel", "button-bar");
		stackTraceToggleButtonBar.getChildren().setAll(stackTraceToggleContainer);

		// Configurer the error header
		this.errorHeader.getStyleClass().setAll("dialog-pane", "error-header");
		this.errorHeader.pseudoClassStateChanged(PseudoClass.getPseudoClass("header"), true);
		this.errorHeader.add(errorDetails, 0, 0);
		this.errorHeader.add(stackTraceToggleButtonBar, 1, 0);
		GridPane.setHgrow(errorDetails, Priority.ALWAYS);
		GridPane.setHgrow(stackTraceToggleButtonBar, Priority.SOMETIMES);

		// Configure the stack trace
		this.stackTrace.setEditable(false);
		this.stackTrace.prefHeightProperty().bind(this.editorWithStackTrace.heightProperty());
		this.stackTrace.prefWidthProperty().bind(Bindings.divide(this.composite.widthProperty(), 2));

		// Configure the composite (initially only error)
		this.composite.getChildren().add(this.editorWithStackTrace);
		VBox.setVgrow(this.editorWithStackTrace, Priority.ALWAYS);
	}

	/**
	 * Obtains the palette indicator {@link Pane}.
	 * 
	 * @return Palette indicator {@link Pane}.
	 */
	public Pane getPaletteIndicator() {
		return this.paletteIndicator;
	}

	/**
	 * Obtains the {@link Pane} containing the view.
	 * 
	 * @return {@link Pane} containing the view.
	 */
	public Pane getComposite() {
		return this.composite;
	}

	/**
	 * Indicates loading complete so may show errors.
	 */
	public void loadComplete() {
		this.isLoading = false;
	}

	/*
	 * ================= AdaptedErrorHandler ====================
	 */

	@Override
	public void showError(String message) {
		this.showError((message == null) || (message.trim().length() == 0) ? null : new MessageOnlyException(message));
	}

	@Override
	public void showError(Throwable error) {

		// Determine if error
		String errorText = null;
		String stackTraceText = null;
		if (error != null) {

			// Provide error text
			errorText = error.getMessage();
			if ((errorText == null) || (errorText.trim().length() == 0)) {
				errorText = error.getClass().getSimpleName() + " thrown";
			}

			// Determine if stack trace
			if (!(error instanceof MessageOnlyException)) {
				StringWriter buffer = new StringWriter();
				error.printStackTrace(new PrintWriter(buffer));
				stackTraceText = buffer.toString();
			}
		}

		// Handle error message
		if (errorText == null) {
			// No error message
			this.composite.getChildren().remove(this.errorHeader);

		} else {
			// Show error message
			this.errorLabel.setText(errorText);
			this.dismissError.setVisited(false);
			if (!this.composite.getChildren().contains(this.errorHeader)) {
				this.composite.getChildren().add(0, this.errorHeader);
			}
		}

		// Handle stack trace
		if (stackTraceText == null) {
			// No stack trace
			this.stackTraceToggle.setVisible(false);
			this.editorWithStackTrace.getItems().remove(this.stackTrace);

		} else {
			// Show stack trace
			this.stackTrace.setText(stackTraceText);
			this.stackTraceToggle.setVisible(true);
		}
	}

	@Override
	public boolean isError(UncertainOperation operation) {
		try {

			// Undertake operation
			operation.run();

			// No error
			return false;

		} catch (Throwable ex) {

			// Determine if still loading editor (as can not show error)
			if (this.isLoading) {
				if (ex instanceof RuntimeException) {
					throw (RuntimeException) ex;
				} else {
					throw new RuntimeException(ex);
				}
			}

			// Show the error
			this.showError(ex);

			// An error
			return true;
		}
	}

}
