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

import net.officefloor.model.change.Conflict;

/**
 * {@link Conflict} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ConflictImpl implements Conflict {

	/**
	 * Description of the {@link Conflict}.
	 */
	private final String conflictDescription;

	/**
	 * Cuase of the {@link Conflict}. May be <code>null</code>.
	 */
	private final Throwable cause;

	/**
	 * Initiate.
	 * 
	 * @param conflictDescription
	 *            Description of the {@link Conflict}.
	 * @param cause
	 *            Cause of the {@link Conflict}. May be <code>null</code>.
	 */
	public ConflictImpl(String conflictDescription, Throwable cause) {
		this.conflictDescription = conflictDescription;
		this.cause = cause;
	}

	/*
	 * ====================== Conflict ==================================
	 */

	@Override
	public String getConflictDescription() {
		return this.conflictDescription;
	}

	@Override
	public Throwable getConflictCause() {
		return this.cause;
	}

}
