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
package net.officefloor.compile.impl.change;

import net.officefloor.compile.change.Change;
import net.officefloor.compile.change.Conflict;
import net.officefloor.compile.impl.util.CompileUtil;

/**
 * Abstract {@link Change}.
 * 
 * @author Daniel
 */
public abstract class AbstractChange<T> implements Change<T> {

	/**
	 * Target of the {@link Change}.
	 */
	private final T target;

	/**
	 * Description of the {@link Change}.
	 */
	private final String changeDescription;

	/**
	 * {@link Conflict} instances preventing the {@link Change} from being
	 * applied.
	 */
	private Conflict[] conflicts;

	/**
	 * Initiate.
	 * 
	 * @param target
	 *            Target of the {@link Change}.
	 * @param changeDescription
	 *            Description of the {@link Change}.
	 */
	public AbstractChange(T target, String changeDescription) {
		this(target, changeDescription, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param target
	 *            Target of the {@link Change}.
	 * @param changeDescription
	 *            Description of the {@link Change}.
	 * @param conflicts
	 *            {@link Conflict} instances preventing the {@link Change} from
	 *            being applied.
	 */
	public AbstractChange(T target, String changeDescription,
			Conflict[] conflicts) {
		this.target = target;
		this.changeDescription = (CompileUtil.isBlank(changeDescription) ? "Change"
				: changeDescription);
		this.setConflicts(conflicts);
	}

	/**
	 * Specifies the {@link Conflict} instances.
	 * 
	 * @param conflicts
	 *            {@link Conflict} instances.
	 */
	protected void setConflicts(Conflict[] conflicts) {
		this.conflicts = (conflicts != null ? conflicts : new Conflict[0]);
	}

	/*
	 * ===================== Change ======================================
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
		return (this.conflicts.length == 0);
	}

	@Override
	public Conflict[] getConflicts() {
		return this.conflicts;
	}

	/**
	 * {@link #apply()} and {@link #revert()} to be implemented.
	 */
}