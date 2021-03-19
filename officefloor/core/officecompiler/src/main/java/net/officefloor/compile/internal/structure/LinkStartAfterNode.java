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

package net.officefloor.compile.internal.structure;

/**
 * {@link LinkStartAfterNode} that can be linked to another
 * {@link LinkStartAfterNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkStartAfterNode extends Node {

	/**
	 * Links this {@link LinkStartAfterNode} to start after the input
	 * {@link LinkStartAfterNode}.
	 * 
	 * @param node {@link LinkStartAfterNode} to have this
	 *             {@link LinkStartAfterNode} start before.
	 * @return <code>true</code> if linked.
	 */
	boolean linkStartAfterNode(LinkStartAfterNode node);

	/**
	 * Obtains the {@link LinkStartAfterNode} instances.
	 * 
	 * @return {@link LinkStartAfterNode} instances.
	 */
	LinkStartAfterNode[] getLinkedStartAfterNodes();

}
