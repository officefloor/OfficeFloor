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

package net.officefloor.compile.internal.structure;

/**
 * Node within the compilation tree.
 *
 * @author Daniel Sagenschneider
 */
public interface Node {

	/**
	 * Creates the qualified name, handling <code>null</code> names.
	 * 
	 * @param names Names. Entries may be <code>null</code>.
	 * @return Qualified name.
	 */
	static String qualify(String... names) {
		StringBuilder qualifiedName = new StringBuilder();
		if (names != null) {
			for (String name : names) {
				if ((name == null) || (name.trim().length() == 0)) {
					// Determine if already qualified
					if (qualifiedName.length() > 0) {
						// Append null / blank
						qualifiedName.append(".[" + name + "]");
					}
				} else {
					// Have name, so determine if qualifier
					if (qualifiedName.length() > 0) {
						qualifiedName.append(".");
					}
					qualifiedName.append(name);
				}
			}
		}
		return qualifiedName.toString();
	}

	/**
	 * Creates an escaped name.
	 * 
	 * @param name Name.
	 * @return Escaped name.
	 */
	static String escape(String name) {
		return name != null ? name.replace('.', '_') : null;
	}

	/**
	 * Obtains the name of the {@link Node}.
	 * 
	 * @return Name of the {@link Node}.
	 */
	String getNodeName();

	/**
	 * Obtains the {@link Node} type.
	 * 
	 * @return {@link Node} type.
	 */
	String getNodeType();

	/**
	 * Obtains the location of the {@link Node}.
	 * 
	 * @return Location of the {@link Node}. May be <code>null</code> if
	 *         {@link Node} does not support a location.
	 */
	String getLocation();

	/**
	 * Obtains the {@link Node} containing this {@link Node}.
	 * 
	 * @return {@link Node} containing this {@link Node}.
	 */
	Node getParentNode();

	/**
	 * Obtains the qualified name of the {@link Node}.
	 * 
	 * @return Qualified name of the {@link Node}.
	 */
	default String getQualifiedName() {
		String name = escape(this.getNodeName());
		Node parent = this.getParentNode();
		return parent != null ? parent.getQualifiedName(name) : name;
	}

	/**
	 * Obtains the qualified name for child {@link Node}.
	 * 
	 * @param name Name of child {@link Node}.
	 * @return Name qualified by this {@link Node}.
	 */
	default String getQualifiedName(String name) {
		return qualify(this.getQualifiedName(), name);
	}

	/**
	 * Indicates if the {@link Node} has been initialised. {@link Node} instances
	 * should only be initialised once. Initialising the {@link Node} twice is an
	 * issue.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	boolean isInitialised();

	/**
	 * Obtains the child {@link Node} instances.
	 * 
	 * @return Child {@link Node} instances.
	 */
	Node[] getChildNodes();

}
