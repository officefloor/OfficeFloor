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

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * Abstract {@link Change}.
 * 
 * @author Daniel Sagenschneider
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
