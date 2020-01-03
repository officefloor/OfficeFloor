/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.model.impl.change;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * Aggregates multiple {@link Change} instances into a single {@link Change}.
 * 
 * @author Daniel Sagenschneider
 */
public class AggregateChange<T> implements Change<T> {

	/**
	 * Target.
	 */
	private final T target;

	/**
	 * Change description.
	 */
	private final String changeDescription;

	/**
	 * {@link Change} instances.
	 */
	private final Change<?>[] changes;

	/**
	 * Initiate.
	 * 
	 * @param target
	 *            Target.
	 * @param changeDescription
	 *            Change description.
	 * @param changes
	 *            {@link Change} instances.
	 */
	public AggregateChange(T target, String changeDescription,
			Change<?>... changes) {
		this.target = target;
		this.changeDescription = changeDescription;
		this.changes = changes;
	}

	/*
	 * ========================= Change ===========================
	 */

	@Override
	public T getTarget() {
		return this.target;
	}

	@Override
	public String getChangeDescription() {
		return this.changeDescription;
	}

	@Override
	public boolean canApply() {
		boolean isAble = true;
		for (int i = 0; i < this.changes.length; i++) {
			if (!(this.changes[i].canApply())) {
				isAble = false;
			}
		}
		return isAble;
	}

	@Override
	public void apply() {
		for (int i = 0; i < this.changes.length; i++) {
			this.changes[i].apply();
		}
	}

	@Override
	public void revert() {
		// Revert in reverse order
		for (int i = this.changes.length - 1; i >= 0; i--) {
			this.changes[i].revert();
		}
	}

	@Override
	public Conflict[] getConflicts() {
		List<Conflict> conflicts = new LinkedList<Conflict>();
		for (Change<?> change : this.changes) {
			Conflict[] listing = change.getConflicts();
			if (listing != null) {
				for (Conflict conflict : listing) {
					conflicts.add(conflict);
				}
			}
		}
		return conflicts.toArray(new Conflict[conflicts.size()]);
	}

}
