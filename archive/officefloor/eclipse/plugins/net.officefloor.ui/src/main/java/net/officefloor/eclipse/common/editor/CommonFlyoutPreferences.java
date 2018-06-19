/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.editor;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;

/**
 * Implementation of
 * {@link org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences}
 * for common use.
 * 
 * @author Daniel Sagenschneider
 */
public class CommonFlyoutPreferences implements FlyoutPreferences {

	/**
	 * Docking location of palette.
	 */
	protected int dockLocation = PositionConstants.EAST;

	/**
	 * State of the palette.
	 */
	protected int paletteState = FlyoutPaletteComposite.STATE_PINNED_OPEN;

	/**
	 * Palette width initially set by default.
	 */
	protected int paletteWidth = -1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences#getDockLocation()
	 */
	public int getDockLocation() {
		return this.dockLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences#getPaletteState()
	 */
	public int getPaletteState() {
		return this.paletteState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences#getPaletteWidth()
	 */
	public int getPaletteWidth() {
		return this.paletteWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences#setDockLocation(int)
	 */
	public void setDockLocation(int location) {
		this.dockLocation = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences#setPaletteState(int)
	 */
	public void setPaletteState(int state) {
		this.paletteState = state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences#setPaletteWidth(int)
	 */
	public void setPaletteWidth(int width) {
		this.paletteWidth = width;
	}

}
