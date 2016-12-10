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
package net.officefloor.model.impl.change;

import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * {@link Change} that does nothing.
 * 
 * @author Daniel Sagenschneider
 */
public class NoChange<T> extends AbstractChange<T> {

	/**
	 * Initiate.
	 * 
	 * @param target
	 *            Target of the {@link Change}.
	 * @param changeDescription
	 *            Description of the {@link Change}.
	 * @param conflictDescriptions
	 *            Descriptions for any {@link Conflict} instances added to this
	 *            {@link Change}.
	 */
	public NoChange(T target, String changeDescription,
			String... conflictDescriptions) {
		super(target, changeDescription);

		// Add the conflicts
		Conflict[] conflicts = new Conflict[conflictDescriptions.length];
		for (int i = 0; i < conflicts.length; i++) {
			conflicts[i] = new ConflictImpl(conflictDescriptions[i]);
		}
		this.setConflicts(conflicts);
	}

	/*
	 * ===================== Change ==================================
	 */

	@Override
	public boolean canApply() {
		// Can not apply an no change
		return false;
	}

	@Override
	public void apply() {
		// Do nothing
	}

	@Override
	public void revert() {
		// Nothing to revert
	}

}