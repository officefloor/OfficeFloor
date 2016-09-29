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
	 * Type being attempted to be loaded.
	 * 
	 * @see #getType()
	 */
	private final Class<?> type;

	/**
	 * Name of the source class being used to load the type. May also be an
	 * alias.
	 */
	private final String sourceClassName;

	/**
	 * {@link CompilerIssue} instances indicating cause of not loading type.
	 */
	private final CompilerIssue[] causes;

	/**
	 * Initiate.
	 * 
	 * @param type
	 *            Type being attempted to be loaded.
	 * @param sourceClassName
	 *            Name of the source class being used to load the type. May also
	 *            be an alias.
	 * @param causes
	 *            {@link CompilerIssue} instances indicating cause of not
	 *            loading type.
	 */
	public LoadTypeError(Class<?> type, String sourceClassName,
			CompilerIssue[] causes) {
		this.type = type;
		this.sourceClassName = sourceClassName;
		this.causes = causes;
	}

	/**
	 * Convenience method to add this as an issue to the {@link CompilerIssues}.
	 * 
	 * @param node
	 *            {@link Node} handling this.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public void addLoadTypeIssue(Node node, CompilerIssues issues) {
		issues.addIssue(node, "Failure loading " + this.type.getSimpleName()
				+ " from source " + this.sourceClassName, this.causes);
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
	 * Obtains the name of the source class being used to load the type. May
	 * also be an alias.
	 * 
	 * @return Name of the source class being used to load the type. May also be
	 *         an alias.
	 */
	public String getSourceClassName() {
		return this.sourceClassName;
	}

	/**
	 * Obtains the {@link CompilerIssue} instances indicating cause of not
	 * loading type.
	 * 
	 * @return {@link CompilerIssue} instances indicating cause of not loading
	 *         type.
	 */
	public CompilerIssue[] getCauses() {
		return this.causes;
	}

}