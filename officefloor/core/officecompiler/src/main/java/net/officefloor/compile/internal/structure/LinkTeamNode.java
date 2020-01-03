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
 * {@link LinkTeamNode} that can be linked to another {@link LinkTeamNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkTeamNode extends Node {

	/**
	 * Links the input {@link LinkTeamNode} to this {@link LinkTeamNode}.
	 * 
	 * @param node
	 *            {@link LinkTeamNode} to link to this {@link LinkTeamNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkTeamNode(LinkTeamNode node);

	/**
	 * Obtains the {@link LinkTeamNode} linked to this {@link LinkTeamNode}.
	 * 
	 * @return {@link LinkTeamNode} linked to this {@link LinkTeamNode}.
	 */
	LinkTeamNode getLinkedTeamNode();

}
