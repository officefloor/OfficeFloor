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
 * {@link LinkStartupAfterNode} that can be linked to another
 * {@link LinkStartupAfterNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkStartupAfterNode extends Node {

	/**
	 * Links the input {@link LinkStartupAfterNode} to this
	 * {@link LinkStartupAfterNode}.
	 * 
	 * @param node {@link LinkStartupAfterNode} to link to this
	 *             {@link LinkStartupAfterNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkStartupAfterNode(LinkStartupAfterNode node);

	/**
	 * Obtains the {@link LinkStartupAfterNode} instances linked to this
	 * {@link LinkStartupAfterNode}.
	 * 
	 * @return {@link LinkStartupAfterNode} instances linked to this
	 *         {@link LinkStartupAfterNode}.
	 */
	LinkStartupAfterNode[] getLinkedStartupAfterNodes();

}
