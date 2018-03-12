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
package net.officefloor.eclipse.woof;

import org.eclipse.draw2d.geometry.Rectangle;

import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;

/**
 * Resizes the {@link WoofGovernanceAreaModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ResizeWoofGovernanceAreaChange extends AbstractChange<WoofGovernanceAreaModel> {

	/**
	 * New bounds.
	 */
	private final Rectangle newBounds;

	/**
	 * Current (old) bounds.
	 */
	private final Rectangle oldBounds;

	/**
	 * Initiate.
	 * 
	 * @param target
	 *            {@link WoofGovernanceAreaModel} to be resized.
	 * @param bounds
	 *            {@link Rectangle} for resize.
	 */
	public ResizeWoofGovernanceAreaChange(WoofGovernanceAreaModel target, Rectangle bounds) {
		super(target, "Resize Governance Area");

		// Store state
		this.newBounds = bounds;
		this.oldBounds = new Rectangle(target.getX(), target.getY(), target.getWidth(), target.getHeight());
	}

	/**
	 * Resizes the {@link WoofGovernanceAreaModel}.
	 * 
	 * @param bounds
	 *            Bounds for the {@link WoofGovernanceAreaModel}.
	 */
	protected void resize(Rectangle bounds) {
		WoofGovernanceAreaModel area = this.getTarget();

		// Determine if move
		boolean isMove = ((area.getWidth() == bounds.width) && (area.getHeight() == bounds.height));

		// Resize
		area.setX(bounds.x);
		area.setY(bounds.y);
		area.setWidth(bounds.width);
		area.setHeight(bounds.height);

		// Trigger firing event on move
		if (isMove) {
			// Swap value back and forth to fire event
			area.setWidth(bounds.width + 100);
			area.setWidth(bounds.width);
		}
	}

	/*
	 * ==================== Change ====================================
	 */

	@Override
	public void apply() {
		this.resize(this.newBounds);
	}

	@Override
	public void revert() {
		this.resize(this.oldBounds);
	}

}