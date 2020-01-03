package net.officefloor.gef.editor;

import javafx.scene.layout.Pane;

/**
 * Context for an overlay.
 * 
 * @author Daniel Sagenschneider
 */
public interface OverlayVisualContext {

	/**
	 * <p>
	 * Obtains the parent {@link Pane} that loads as the overlay.
	 * <p>
	 * Content for the overlay to be added to this {@link Pane}.
	 * 
	 * @return Parent {@link Pane} to load in the overlay.
	 */
	Pane getOverlayParent();

	/**
	 * <p>
	 * Indicates whether the overlay is fixed width.
	 * <p>
	 * By default the overlay resizes.
	 * 
	 * @param isFixedWith
	 *            <code>true</code> for fixed width.
	 */
	void setFixedWidth(boolean isFixedWith);

	/**
	 * <p>
	 * Indicates whether the overlay is fixed height.
	 * <p>
	 * By default the overlay resizes.
	 * 
	 * @param isFixedHeight
	 *            <code>true</code> for fixed height.
	 */
	void setFixedHeight(boolean isFixedHeight);

	/**
	 * Closes the overlay.
	 */
	void close();

}