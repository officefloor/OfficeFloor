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
	 * Aggregates the {@link Change} instances, basing resulting {@link Change} on
	 * first {@link Change}.
	 * 
	 * @param <T>     Target type.
	 * @param change  First {@link Change}.
	 * @param changes Further {@link Change} instances to aggregate.
	 * @return {@link Change} aggregating the input {@link Change} instances.
	 */
	public static <T> Change<T> aggregate(Change<T> change, Change<?>... changes) {
		Change<?>[] allChanges = new Change[changes.length + 1];
		allChanges[0] = change;
		System.arraycopy(changes, 0, allChanges, 1, changes.length);
		return new AggregateChange<>(change.getTarget(), change.getChangeDescription(), allChanges);
	}

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
	 * @param target            Target.
	 * @param changeDescription Change description.
	 * @param changes           {@link Change} instances.
	 */
	public AggregateChange(T target, String changeDescription, Change<?>... changes) {
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
