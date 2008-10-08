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
import org.eclipse.draw2d.CheckBox;
import org.eclipse.gef.EditPart;

/**
 * Abstract {@link org.eclipse.gef.EditPart} for a
 * {@link org.eclipse.draw2d.CheckBox} that requires specialising for
 * checking/unchecking.
 * 
 * @author Daniel
 */
public abstract class CheckBoxEditPart extends
		AbstractOfficeFloorEditPart<Model> implements Model, ModelEditPart,
		ActionListener {

	/**
	 * {@link CheckBox}.
	 */
	private final CheckBox checkBox;

	/**
	 * X location.
	 */
	private int x = -1;

	/**
	 * Y location.
	 */
	private int y = -1;

	/**
	 * Initiate.
	 * 
	 * @param isChecked
	 *            Whether the {@link CheckBox} is initially checked.
	 */
	public CheckBoxEditPart(boolean isChecked) {
		this.checkBox = new CheckBox();
		this.checkBox.setSelected(isChecked);

		// Handle checking/unchecking
		this.checkBox.addActionListener(this);
	}

	/*
	 * ==========================================================================
	 * AbstractOfficeFloorEditPart
	 * ==============================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		// No property change handlers
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		// Return the check box
		return new OfficeFloorFigure(this.checkBox);
	}

	/*
	 * ==========================================================================
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
		// Notify the state change
		this.checkBoxStateChanged(this.checkBox.isSelected());
	}

	/**
	 * Notifies that the {@link CheckBox} state has chanaged.
	 * 
	 * @param isChecked
	 *            True if {@link CheckBox} is checked.
	 */
	protected abstract void checkBoxStateChanged(boolean isChecked);

	/*
	 * ==========================================================================
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
	 * ==========================================================================
	 * Model
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.Model#getX()
	 */
	public int getX() {
		return this.x;
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
		return this.y;
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
