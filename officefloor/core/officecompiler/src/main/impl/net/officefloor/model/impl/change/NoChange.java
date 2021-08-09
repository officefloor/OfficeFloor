/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * @param conflicts
	 *            {@link Conflict} instances.
	 */
	public NoChange(T target, String changeDescription, Conflict... conflicts) {
		super(target, changeDescription);

		// Add the conflicts
		this.setConflicts(conflicts);
	}

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
	public NoChange(T target, String changeDescription, String... conflictDescriptions) {
		super(target, changeDescription);

		// Add the conflicts
		Conflict[] conflicts = new Conflict[conflictDescriptions.length];
		for (int i = 0; i < conflicts.length; i++) {
			conflicts[i] = new ConflictImpl(conflictDescriptions[i], null);
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
