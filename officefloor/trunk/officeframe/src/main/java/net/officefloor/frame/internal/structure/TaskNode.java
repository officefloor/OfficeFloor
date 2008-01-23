/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.internal.structure;

/**
 * Node within the tree of {@link TaskNode} instances to execute.
 * 
 * @author Daniel
 */
public interface TaskNode {

	/**
	 * <p>
	 * Specifies the parallel owner of this {@link TaskNode}.
	 * <p>
	 * The input {@link TaskNode} is executed once the current {@link Flow} that
	 * this {@link TaskNode} is involved with is complete.
	 * 
	 * @param taskNode
	 *            Parallel owner of this {@link TaskNode}.
	 */
	void setParallelOwner(TaskNode taskNode);

	/**
	 * <p>
	 * Obtains the parallel owner of this {@link TaskNode}.
	 * 
	 * @return Parallel owner of this {@link TaskNode}.
	 */
	TaskNode getParallelOwner();

	/**
	 * Specifies the parallel {@link TaskNode} to the current {@link TaskNode}.
	 * The current {@link TaskNode} will not complete until the input parallel
	 * {@link TaskNode} is complete.
	 * 
	 * @param taskNode
	 *            Parallel {@link TaskNode}.
	 */
	void setParallelNode(TaskNode taskNode);

	/**
	 * Obtains the parallel {@link TaskNode} to the current {@link TaskNode}.
	 * 
	 * @return Parallel {@link TaskNode} to the current {@link TaskNode}.
	 */
	TaskNode getParallelNode();

	/**
	 * Specifies the next {@link TaskNode} in the {@link Flow} to execute after
	 * the current {@link TaskNode} is completed.
	 * 
	 * @param taskNode
	 *            Next {@link TaskNode}.
	 */
	void setNextNode(TaskNode taskNode);

	/**
	 * Obtains the next {@link TaskNode} in the {@link Flow} to execute after
	 * the current {@link TaskNode} has completed.
	 * 
	 * @return Next {@link TaskNode}.
	 */
	TaskNode getNextNode();

	/**
	 * Clears the {@link TaskNode} instances linked to this {@link TaskNode}.
	 */
	void clearNodes();
	
}
