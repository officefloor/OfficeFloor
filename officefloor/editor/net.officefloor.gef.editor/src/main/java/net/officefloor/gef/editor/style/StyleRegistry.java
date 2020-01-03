package net.officefloor.gef.editor.style;

import java.net.URL;

import org.eclipse.gef.mvc.fx.parts.IVisualPart;

import javafx.beans.property.ReadOnlyProperty;
import net.officefloor.gef.editor.AdaptedChild;

/**
 * Registry of styles for {@link IVisualPart} instances of the
 * {@link AdaptedChild} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface StyleRegistry {

	/**
	 * Registers the style for the {@link AdaptedChild}.
	 * 
	 * @param configurationPath
	 *            Configuration path to the style.
	 * @param stylesheetContent
	 *            Style sheet content for the {@link IVisualPart} of the
	 *            configuration item.
	 * @return {@link ReadOnlyProperty} to the {@link URL} {@link String} of the
	 *         style.
	 */
	ReadOnlyProperty<URL> registerStyle(String configurationPath, ReadOnlyProperty<String> stylesheetContent);

}