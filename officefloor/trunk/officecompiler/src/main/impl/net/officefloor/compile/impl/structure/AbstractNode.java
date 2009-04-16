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
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;

/**
 * Abstract functionality for nodes.
 * 
 * @author Daniel
 */
public abstract class AbstractNode {

	/**
	 * Ensures both inputs are a {@link LinkFlowNode} and if so links them.
	 * 
	 * @param linkSource
	 *            Source {@link LinkFlowNode}.
	 * @param linkTarget
	 *            Target {@link LinkFlowNode}.
	 * @return <code>true</code> if linked.
	 */
	protected boolean linkFlow(Object linkSource, Object linkTarget) {

		// Ensure the link source is link flow node
		if (!(linkSource instanceof LinkFlowNode)) {
			this.addIssue("Invalid link source: "
					+ linkSource
					+ " ["
					+ (linkSource == null ? null : linkSource.getClass()
							.getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link flow node
		if (!(linkTarget instanceof LinkFlowNode)) {
			this.addIssue("Invalid link target: "
					+ linkTarget
					+ " ["
					+ (linkTarget == null ? null : linkTarget.getClass()
							.getName()
							+ "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkFlowNode) linkSource)
				.linkFlowNode((LinkFlowNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkObjectNode} and if so links them.
	 * 
	 * @param linkSource
	 *            Source {@link LinkObjectNode}.
	 * @param linkTarget
	 *            Target {@link LinkObjectNode}.
	 * @return <code>true</code> if linked.
	 */
	protected boolean linkObject(Object linkSource, Object linkTarget) {

		// Ensure the link source is link object node
		if (!(linkSource instanceof LinkObjectNode)) {
			this.addIssue("Invalid link source: "
					+ linkSource
					+ " ["
					+ (linkSource == null ? null : linkSource.getClass()
							.getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link object node
		if (!(linkTarget instanceof LinkObjectNode)) {
			this.addIssue("Invalid link target: "
					+ linkTarget
					+ " ["
					+ (linkTarget == null ? null : linkTarget.getClass()
							.getName()
							+ "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkObjectNode) linkSource)
				.linkObjectNode((LinkObjectNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkTeamNode} and if so links them.
	 * 
	 * @param linkSource
	 *            Source {@link LinkTeamNode}.
	 * @param linkTarget
	 *            Target {@link LinkTeamNode}.
	 * @return <code>true</code> if linked.
	 */
	protected boolean linkTeam(Object linkSource, Object linkTarget) {

		// Ensure the link source is link team node
		if (!(linkSource instanceof LinkTeamNode)) {
			this.addIssue("Invalid link source: "
					+ linkSource
					+ " ["
					+ (linkSource == null ? null : linkSource.getClass()
							.getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link team node
		if (!(linkTarget instanceof LinkTeamNode)) {
			this.addIssue("Invalid link target: "
					+ linkTarget
					+ " ["
					+ (linkTarget == null ? null : linkTarget.getClass()
							.getName()
							+ "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkTeamNode) linkSource)
				.linkTeamNode((LinkTeamNode) linkTarget);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	protected abstract void addIssue(String issueDescription);

}