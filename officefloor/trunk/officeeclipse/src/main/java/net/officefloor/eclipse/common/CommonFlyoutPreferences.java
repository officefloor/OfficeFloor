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
package net.officefloor.eclipse.common;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;

/**
 * Implementation of
 * {@link org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences}
 * for common use.
 * 
 * @author Daniel
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
