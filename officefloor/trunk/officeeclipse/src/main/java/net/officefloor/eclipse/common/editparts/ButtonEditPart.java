/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.common.editparts;

import java.beans.PropertyChangeListener;
import java.util.List;

import net.officefloor.eclipse.common.ModelEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.Model;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.gef.EditPart;

/**
 * Abstract {@link org.eclipse.gef.EditPart} for a
 * {@link org.eclipse.draw2d.Button} that requires specialising for handling the
 * {@link org.eclipse.draw2d.Button} click.
 * 
 * @author Daniel
 */
public abstract class ButtonEditPart extends AbstractOfficeFloorEditPart<Model>
		implements Model, ModelEditPart, ActionListener {

	/**
	 * Label for the button.
	 */
	private final String label;

	/**
	 * X location.
	 */
	private int x;

	/**
	 * Y location.
	 */
	private int y;

	/**
	 * Initiate.
	 * 
	 * @param label
	 *            Label for the button.
	 */
	public ButtonEditPart(String label) {
		this.label = label;
	}

	/*
	 * ================================================
	 * AbstractOfficeFloorEditPart
	 * ================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		// Create the button
		Button button = new Button(this.label);

		// Handle the button clicks
		button.addActionListener(this);

		// Return the button
		return new OfficeFloorFigure(button);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		// No property change handling
	}

	/*
	 * ============================================================
	 * ModelEditPart
	 * ============================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.ModelEditPart#getEditPart()
	 */
	public EditPart getEditPart() {
		return this;
	}

	/*
	 * ===========================================================
	 * ActionListener
	 * ============================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.draw2d.ActionListener#actionPerformed(org.eclipse.draw2d.
	 * ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		this.handleButtonClick();
	}

	/**
	 * Override to provide handling on the {@link Button} click.
	 */
	protected abstract void handleButtonClick();

	/*
	 * ====================================================================
	 * Model
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.Model#getX()
	 */
	public int getX() {
		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.Model#setX(int)
	 */
	public void setX(int x) {
		this.x = x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.Model#getY()
	 */
	public int getY() {
		return y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.Model#setY(int)
	 */
	public void setY(int y) {
		this.y = y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.model.Model#addPropertyChangeListener(java.beans.
	 * PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// No property change listening
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.model.Model#removePropertyChangeListener(java.beans.
	 * PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		// No property change listening
	}

}
