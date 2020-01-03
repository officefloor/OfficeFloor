package net.officefloor.gef.ide.preferences;

import javafx.scene.layout.Pane;

/**
 * Interface for generic preference styler.
 * 
 * @author Daniel Sagenschneider
 */
public interface PreferenceStyler {

	/**
	 * Creates a visual for the editing the style.
	 * 
	 * @param close Removes the view.
	 * @return Visual for editing the style.
	 */
	Pane createVisual(Runnable close);
}