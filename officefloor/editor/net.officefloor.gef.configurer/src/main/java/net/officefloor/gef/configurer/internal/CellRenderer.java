package net.officefloor.gef.configurer.internal;

import javafx.beans.property.Property;
import javafx.scene.control.TableCell;

/**
 * Renderer for the cell.
 * 
 * @author Daniel Sagenschneider
 */
public interface CellRenderer<I, V> {

	/**
	 * Obtains the value for the {@link TableCell}.
	 * 
	 * @return Value for the the {@link TableCell}.
	 */
	Property<V> getValue();

}