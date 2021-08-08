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

package net.officefloor.compile.impl.util;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;

/**
 * Propagates a failure loading a type.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadTypeError extends Error {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Type being attempted to be loaded.
	 * 
	 * @see #getType()
	 */
	private final Class<?> type;

	/**
	 * Name of the source class being used to load the type. May also be an alias.
	 */
	private final String sourceClassName;

	/**
	 * {@link CompilerIssue} instances indicating cause of not loading type.
	 */
	private final CompilerIssue[] causes;

	/**
	 * Initiate.
	 * 
	 * @param type            Type being attempted to be loaded.
	 * @param sourceClassName Name of the source class being used to load the type.
	 *                        May also be an alias.
	 * @param causes          {@link CompilerIssue} instances indicating cause of
	 *                        not loading type.
	 */
	public LoadTypeError(Class<?> type, String sourceClassName, CompilerIssue[] causes) {
		super("Failure loading " + type.getSimpleName() + " from source " + sourceClassName);
		this.type = type;
		this.sourceClassName = sourceClassName;
		this.causes = causes;
	}

	/**
	 * Convenience method to add this as an issue to the {@link CompilerIssues}.
	 * 
	 * @param node   {@link Node} handling this.
	 * @param issues {@link CompilerIssues}.
	 */
	public void addLoadTypeIssue(Node node, CompilerIssues issues) {
		issues.addIssue(node, this.getMessage(), this.causes);
	}

	/**
	 * Obtains the type being attempted to be loaded.
	 * 
	 * @return Type being attempted to be loaded.
	 */
	public Class<?> getType() {
		return this.type;
	}

	/**
	 * Obtains the name of the source class being used to load the type. May also be
	 * an alias.
	 * 
	 * @return Name of the source class being used to load the type. May also be an
	 *         alias.
	 */
	public String getSourceClassName() {
		return this.sourceClassName;
	}

	/**
	 * Obtains the {@link CompilerIssue} instances indicating cause of not loading
	 * type.
	 * 
	 * @return {@link CompilerIssue} instances indicating cause of not loading type.
	 */
	public CompilerIssue[] getCauses() {
		return this.causes;
	}

}
