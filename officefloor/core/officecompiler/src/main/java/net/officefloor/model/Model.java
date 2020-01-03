package net.officefloor.model;

import java.beans.PropertyChangeListener;

/**
 * Contract for top level functionality for all model elements.
 * 
 * @author Daniel Sagenschneider
 */
public interface Model {

	/**
	 * Obtains the X co-ordinate for the model.
	 * 
	 * @return X co-ordinate for the model.
	 */
	int getX();

	/**
	 * Specifies the X co-ordinate for the model.
	 * 
	 * @param x
	 *            X co-ordinate for the model.
	 */
	void setX(int x);

	/**
	 * Obtains the Y co-ordinate for the model.
	 * 
	 * @return Y co-ordinate for the model.
	 */
	int getY();

	/**
	 * Specifies the Y co-ordinate for the model.
	 * 
	 * @param y
	 *            Y co-ordinate for the model.
	 */
	void setY(int y);

	/**
	 * Adds a {@link PropertyChangeListener} to this model element.
	 * 
	 * @param listener
	 *            {@link PropertyChangeListener} to this model element.
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Removes a {@link PropertyChangeListener} from this model element.
	 * 
	 * @param listener
	 *            {@link PropertyChangeListener}.
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);

}
