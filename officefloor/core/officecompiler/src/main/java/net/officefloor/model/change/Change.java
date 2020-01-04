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

package net.officefloor.model.change;

/**
 * <p>
 * A change to be applied/reverted.
 * <p>
 * All operations utilise this to enable do/undo functionality.
 * 
 * @author Daniel Sagenschneider
 */
public interface Change<T> {

	/**
	 * Obtains the target to which this {@link Change} applies.
	 * 
	 * @return Target to which this {@link Change} applies.
	 */
	T getTarget();

	/**
	 * Obtains a description of the {@link Change}.
	 * 
	 * @return Description of the {@link Change}.
	 */
	String getChangeDescription();

	/**
	 * <p>
	 * Indicates if can apply this {@link Change}.
	 * <p>
	 * Typically there will be {@link Conflict} instances providing detail on
	 * why the {@link Change} can not be applied.
	 * 
	 * @return <code>true</code> if can apply this {@link Change}.
	 * 
	 * @see #getConflicts()
	 */
	boolean canApply();

	/**
	 * Applies this {@link Change}.
	 */
	void apply();

	/**
	 * <p>
	 * Reverts this {@link Change} (after being applied).
	 * <p>
	 * This enables do/undo functionality.
	 */
	void revert();

	/**
	 * <p>
	 * Obtains the {@link Conflict} instances preventing this {@link Change}
	 * from being applied.
	 * <p>
	 * A {@link Change} can only be applied if this returns an empty array.
	 * 
	 * @return Any {@link Conflict} instances preventing applying this
	 *         {@link Change}.
	 */
	Conflict[] getConflicts();

}
