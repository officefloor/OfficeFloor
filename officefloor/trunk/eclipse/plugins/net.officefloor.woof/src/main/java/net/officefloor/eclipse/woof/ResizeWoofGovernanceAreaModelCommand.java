/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.eclipse.woof;

import net.officefloor.eclipse.woof.editparts.WoofGovernanceAreaEditPart;
import net.officefloor.model.woof.WoofGovernanceAreaModel;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

/**
 * Resizes the {@link WoofGovernanceAreaModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ResizeWoofGovernanceAreaModelCommand extends Command {

	/**
	 * {@link WoofGovernanceAreaEditPart} to be resized.
	 */
	protected final WoofGovernanceAreaEditPart editPart;

	/**
	 * New bounds.
	 */
	protected final Rectangle newBounds;

	/**
	 * Current (old) bounds.
	 */
	protected final Rectangle oldBounds;

	/**
	 * Initiate.
	 * 
	 * @param editPart
	 *            {@link WoofGovernanceAreaEditPart} to be resized.
	 * @param bounds
	 *            {@link Rectangle} for resize.
	 */
	public ResizeWoofGovernanceAreaModelCommand(
			WoofGovernanceAreaEditPart editPart, Rectangle bounds) {
		super("Resize");

		// Store state
		this.editPart = editPart;
		this.newBounds = bounds;
		this.oldBounds = new Rectangle(editPart.getFigure().getBounds());
	}

	/*
	 * ==================== Command ====================================
	 */

	@Override
	public void execute() {
		// Resize to new bounds
		this.resize(this.newBounds);
	}

	@Override
	public void undo() {
		// Resize back to old bounds
		this.resize(this.oldBounds);
	}

	/**
	 * Resizes the {@link WoofGovernanceAreaEditPart}.
	 * 
	 * @param bounds
	 *            Bounds for the {@link WoofGovernanceAreaEditPart}.
	 */
	protected void resize(Rectangle bounds) {
		this.editPart.resize(bounds);
	}

}