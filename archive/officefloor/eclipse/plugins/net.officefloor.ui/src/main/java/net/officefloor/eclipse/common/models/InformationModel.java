/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.common.models;

import java.beans.PropertyChangeListener;

import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.model.Model;

/**
 * {@link Model} to provide information on the
 * {@link AbstractOfficeFloorEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class InformationModel implements Model {

	/**
	 * Text information.
	 */
	private final String text;

	/**
	 * X.
	 */
	private int x = 10;

	/**
	 * Y.
	 */
	private int y = 10;

	/**
	 * Instantiate.
	 * 
	 * @param text
	 *            Text.
	 */
	public InformationModel(String text) {
		this.text = text;
	}

	/**
	 * Obtains the text information.
	 * 
	 * @return Text information.
	 */
	public String getText() {
		return this.text;
	}

	/*
	 * ===================== Model ======================
	 */

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener arg0) {
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

	}

}
