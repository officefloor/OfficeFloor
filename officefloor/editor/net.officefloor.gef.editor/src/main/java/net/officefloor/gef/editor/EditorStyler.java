package net.officefloor.gef.editor;

import org.eclipse.gef.mvc.fx.models.GridModel;

import javafx.beans.property.Property;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Enables styling the content.
 * 
 * @author Daniel Sagenschneider
 */
public interface EditorStyler {

	/**
	 * <p>
	 * Obtains the root {@link Parent} for the editor.
	 * <p>
	 * This allows for interrogating the structure of the editor.
	 * 
	 * @return Editor {@link Parent}.
	 */
	Parent getEditor();

	/**
	 * Obtains the {@link GridModel} to configure the content grid.
	 * 
	 * @return {@link GridModel} to configure the content grid.
	 */
	GridModel getGridModel();

	/**
	 * Obtains the {@link Property} to the style sheet rules for the {@link Scene}.
	 * 
	 * @return {@link Property} to specify the style sheet rules for the
	 *         {@link Scene}.
	 */
	Property<String> editorStyle();
}