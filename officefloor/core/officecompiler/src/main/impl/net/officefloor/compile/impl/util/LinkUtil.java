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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkSynchronousNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Utility class to work with links.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkUtil {

	/**
	 * {@link LinkFlowNode} {@link Traverser}.
	 */
	private static final Traverser<LinkFlowNode> FLOW_TRAVERSER = (link) -> link.getLinkedFlowNode();

	/**
	 * {@link LinkObjectNode} {@link Traverser}.
	 */
	private static final Traverser<LinkObjectNode> OBJECT_TRAVERSER = (object) -> object.getLinkedObjectNode();

	/**
	 * {@link LinkTeamNode} {@link Traverser}.
	 */
	private static final Traverser<LinkTeamNode> TEAM_TRAVERSER = (team) -> team.getLinkedTeamNode();

	/**
	 * {@link LinkOfficeNode} {@link Traverser}.
	 */
	private static final Traverser<LinkOfficeNode> OFFICE_TRAVERSER = (office) -> office.getLinkedOfficeNode();

	/**
	 * Ensures both inputs are a {@link LinkFlowNode} and if so links them.
	 *
	 * @param linkSource
	 *            Source {@link LinkFlowNode}.
	 * @param linkTarget
	 *            Target {@link LinkFlowNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param node
	 *            {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkFlow(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link flow node
		if (!(linkSource instanceof LinkFlowNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link flow node
		if (!(linkTarget instanceof LinkFlowNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkFlowNode) linkSource).linkFlowNode((LinkFlowNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkSynchronousNode} and if so links
	 * them.
	 * 
	 * @param linkSource
	 *            Source {@link LinkSynchronousNode}.
	 * @param linkTarget
	 *            Target {@link LinkSynchronousNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param node
	 *            {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	@Deprecated // no synchronous OfficeFloor interaction (as always via queues)
	public static boolean linkSynchronous(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link synchronous node
		if (!(linkSource instanceof LinkSynchronousNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link synchronous node
		if (!(linkTarget instanceof LinkSynchronousNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkSynchronousNode) linkSource).linkSynchronousNode((LinkSynchronousNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkObjectNode} and if so links them.
	 *
	 * @param linkSource
	 *            Source {@link LinkObjectNode}.
	 * @param linkTarget
	 *            Target {@link LinkObjectNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param node
	 *            {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkObject(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link object node
		if (!(linkSource instanceof LinkObjectNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link object node
		if (!(linkTarget instanceof LinkObjectNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkObjectNode) linkSource).linkObjectNode((LinkObjectNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkTeamNode} and if so links them.
	 *
	 * @param linkSource
	 *            Source {@link LinkTeamNode}.
	 * @param linkTarget
	 *            Target {@link LinkTeamNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param node
	 *            {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkTeam(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link team node
		if (!(linkSource instanceof LinkTeamNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link team node
		if (!(linkTarget instanceof LinkTeamNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkTeamNode) linkSource).linkTeamNode((LinkTeamNode) linkTarget);
	}

	/**
	 * Ensures both inputs are a {@link LinkOfficeNode} and if so links them.
	 *
	 * @param linkSource
	 *            Source {@link LinkOfficeNode}.
	 * @param linkTarget
	 *            Target {@link LinkOfficeNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param node
	 *            {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkOffice(Object linkSource, Object linkTarget, CompilerIssues issues, Node node) {

		// Obtain the node
		if (linkSource instanceof Node) {
			node = (Node) linkSource;
		}

		// Ensure the link source is link office node
		if (!(linkSource instanceof LinkOfficeNode)) {
			issues.addIssue(node, "Invalid link source: " + linkSource + " ["
					+ (linkSource == null ? null : linkSource.getClass().getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link office node
		if (!(linkTarget instanceof LinkOfficeNode)) {
			issues.addIssue(node, "Invalid link target: " + linkTarget + " ["
					+ (linkTarget == null ? null : linkTarget.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkOfficeNode) linkSource).linkOfficeNode((LinkOfficeNode) linkTarget);
	}

	/**
	 * Links the {@link ManagedObjectSourceNode} to the
	 * {@link InputManagedObjectNode}.
	 *
	 * @param inputManagedObject
	 *            {@link InputManagedObjectNode}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSourceNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param node
	 *            {@link Node} wishing to link the {@link Flow}.
	 * @return <code>true</code> if linked.
	 */
	public static boolean linkManagedObjectSourceInput(Object managedObjectSource, Object inputManagedObject,
			CompilerIssues issues, Node node) {

		// Obtain the node
		if (managedObjectSource instanceof Node) {
			node = (Node) managedObjectSource;
		}

		// Ensure is a managed object source
		if (!(managedObjectSource instanceof ManagedObjectSourceNode)) {
			issues.addIssue(node, "Invalid managed object source node: " + managedObjectSource + " ["
					+ (managedObjectSource == null ? null : managedObjectSource.getClass().getName() + "]"));
			return false; // can not link
		}

		// Ensure is an input managed object node
		if (!(inputManagedObject instanceof InputManagedObjectNode)) {
			issues.addIssue(node, "Invalid input managed object node: " + inputManagedObject + " ["
					+ (inputManagedObject == null ? null : inputManagedObject.getClass().getName() + "]"));
			return false; // can not link
		}

		// Link the managed object source to the input managed object
		return ((ManagedObjectSourceNode) managedObjectSource)
				.linkInputManagedObjectNode((InputManagedObjectNode) inputManagedObject);
	}

	/**
	 * Finds the furtherest target link by the specified type.
	 * 
	 * @param <T>
	 *            Target type.
	 * @param link
	 *            Starting {@link LinkFlowNode}.
	 * @param targetType
	 *            Target {@link LinkFlowNode} type to find.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return Furtherest target {@link LinkFlowNode} or <code>null</code> if no
	 *         targets found.
	 */
	public static <T extends LinkFlowNode> T findFurtherestTarget(LinkFlowNode link, Class<T> targetType,
			CompilerIssues issues) {
		return retrieveFurtherestTarget(link, FLOW_TRAVERSER, targetType, false, issues);
	}

	/**
	 * Finds the furtherest target link by the specified type.
	 * 
	 * @param <T>
	 *            Target type.
	 * @param link
	 *            Starting {@link LinkObjectNode}.
	 * @param targetType
	 *            Target {@link LinkObjectNode} type to find.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return Furthurest target {@link LinkObjectNode} or <code>null</code> if
	 *         no targets found.
	 */
	public static <T extends LinkObjectNode> T retrieveFurtherestTarget(LinkObjectNode link, Class<T> targetType,
			CompilerIssues issues) {
		return retrieveFurtherestTarget(link, OBJECT_TRAVERSER, targetType, true, issues);
	}

	/**
	 * Finds the target link by the specified type.
	 * 
	 * @param <T>
	 *            Target type.
	 * @param link
	 *            Starting {@link LinkFlowNode}.
	 * @param targetType
	 *            Target {@link LinkFlowNode} type to find.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return Target {@link LinkFlowNode} or <code>null</code> if target not
	 *         found.
	 */
	public static <T extends LinkFlowNode> T findTarget(LinkFlowNode link, Class<T> targetType, CompilerIssues issues) {
		return retrieveTarget(link, FLOW_TRAVERSER, targetType, false, issues, null).target;
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param <T>
	 *            Target type.
	 * @param link
	 *            Starting {@link LinkFlowNode}.
	 * @param targetType
	 *            Target {@link LinkFlowNode} type to retrieve.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return Target {@link LinkFlowNode} or <code>null</code> if issue
	 *         obtaining which is reported to the {@link CompilerIssues}.
	 */
	public static <T extends LinkFlowNode> T retrieveTarget(LinkFlowNode link, Class<T> targetType,
			CompilerIssues issues) {
		return retrieveTarget(link, FLOW_TRAVERSER, targetType, true, issues, null).target;
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param <T>
	 *            Target type.
	 * @param link
	 *            Starting {@link LinkObjectNode}.
	 * @param targetType
	 *            Target {@link LinkObjectNode} type to retrieve.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return Target {@link LinkObjectNode} or <code>null</code> if issue
	 *         obtaining which is reported to the {@link CompilerIssues}.
	 */
	public static <T extends LinkObjectNode> T retrieveTarget(LinkObjectNode link, Class<T> targetType,
			CompilerIssues issues) {
		return retrieveTarget(link, OBJECT_TRAVERSER, targetType, true, issues, null).target;
	}

	/**
	 * Finds the target link by the specified type.
	 * 
	 * @param <T>
	 *            Target type.
	 * @param link
	 *            Starting {@link LinkTeamNode}.
	 * @param targetType
	 *            Target {@link LinkTeamNode} type to retrieve.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return Target {@link LinkTeamNode} or <code>null</code> if not found.
	 */
	public static <T extends Node> T findTarget(LinkTeamNode link, Class<T> targetType, CompilerIssues issues) {
		return retrieveTarget(link, TEAM_TRAVERSER, targetType, false, issues, null).target;
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param <T>
	 *            Target type.
	 * @param link
	 *            Starting {@link LinkOfficeNode}.
	 * @param targetType
	 *            Target {@link LinkOfficeNode} type to retrieve.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return Target {@link LinkOfficeNode} or <code>null</code> if issue
	 *         obtaining which is reported to the {@link CompilerIssues}.
	 */
	public static <T extends Node> T retrieveTarget(LinkOfficeNode link, Class<T> targetType, CompilerIssues issues) {
		return retrieveTarget(link, OFFICE_TRAVERSER, targetType, true, issues, null).target;
	}

	/**
	 * Links the {@link LinkFlowNode}.
	 * 
	 * @param node
	 *            {@link LinkFlowNode} to have the link loaded.
	 * @param linkNode
	 *            Link {@link LinkFlowNode} to load.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param loader
	 *            {@link Consumer} to load the link onto the
	 *            {@link LinkFlowNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkFlowNode(LinkFlowNode node, LinkFlowNode linkNode, CompilerIssues issues,
			Consumer<LinkFlowNode> loader) {
		return linkNode(node, linkNode, FLOW_TRAVERSER, issues, loader);
	}

	/**
	 * Links the {@link LinkObjectNode}.
	 * 
	 * @param node
	 *            {@link LinkObjectNode} to have the link loaded.
	 * @param linkNode
	 *            Link {@link LinkObjectNode} to load.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param loader
	 *            {@link Consumer} to load the link onto the
	 *            {@link LinkObjectNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkObjectNode(LinkObjectNode node, LinkObjectNode linkNode, CompilerIssues issues,
			Consumer<LinkObjectNode> loader) {
		return linkNode(node, linkNode, OBJECT_TRAVERSER, issues, loader);
	}

	/**
	 * Links the {@link LinkObjectNode}.
	 * 
	 * @param node
	 *            {@link LinkTeamNode} to have the link loaded.
	 * @param linkNode
	 *            Link {@link LinkTeamNode} to load.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param loader
	 *            {@link Consumer} to load the link onto the
	 *            {@link LinkTeamNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkTeamNode(LinkTeamNode node, LinkTeamNode linkNode, CompilerIssues issues,
			Consumer<LinkTeamNode> loader) {
		return linkNode(node, linkNode, TEAM_TRAVERSER, issues, loader);
	}

	/**
	 * Links the {@link LinkOfficeNode}.
	 * 
	 * @param node
	 *            {@link LinkOfficeNode} to have the link loaded.
	 * @param linkNode
	 *            Link {@link LinkOfficeNode} to load.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param loader
	 *            {@link Consumer} to load the link on the
	 *            {@link LinkOfficeNode}.
	 * @return <code>true</code> if successful, or <code>false</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	public static boolean linkOfficeNode(LinkOfficeNode node, LinkOfficeNode linkNode, CompilerIssues issues,
			Consumer<LinkOfficeNode> loader) {
		return linkNode(node, linkNode, OFFICE_TRAVERSER, issues, loader);
	}

	/**
	 * Loads the link to the {@link Node}.
	 * 
	 * @param node
	 *            {@link Node} to have the link loaded.
	 * @param linkNode
	 *            Link {@link Node} to load.
	 * @param traverser
	 *            {@link Traverser}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param loader
	 *            {@link Consumer} to load the link onto the {@link Node}.
	 * @return <code>true</code> if successfully loaded the link {@link Node}.
	 *         Otherwise, <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	private static <L extends Node> boolean linkNode(L node, L linkNode, Traverser<L> traverser, CompilerIssues issues,
			Consumer<L> loader) {

		// Ensure not already linked
		L existingLink = traverser.getNextLinkNode(node);
		if (existingLink != null) {
			issues.addIssue(node, node.getNodeType() + " " + node.getNodeName() + " linked more than once");
			return false; // already linked
		}

		// Load the link
		loader.accept(linkNode);
		return true;
	}

	/**
	 * Retrieves the furtherest target link by the specified type.
	 * 
	 * @param <L>
	 *            Link type.
	 * @param <T>
	 *            Target type.
	 * @param link
	 *            Starting {@link LinkFlowNode}.
	 * @param traverser
	 *            {@link Traverser} to traverse the links.
	 * @param targetType
	 *            Target {@link LinkFlowNode} type to find.
	 * @param isIssueOnNoTarget
	 *            Indicates if issue should be made if target not found.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return Furtherest target {@link LinkFlowNode} or <code>null</code> if no
	 *         targets found.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Node, L extends Node> T retrieveFurtherestTarget(L link, Traverser<L> traverser,
			Class<T> targetType, boolean isIssueOnNoTarget, CompilerIssues issues) {

		// Keep track of all traversed links
		Set<Object> traversedLinks = new HashSet<Object>();

		// Find the first target
		T target = retrieveTarget(link, traverser, targetType, isIssueOnNoTarget, issues, traversedLinks).target;

		// Loop to find the furtherest target
		T furtherestTarget = null;
		while (target != null) {
			// Keep reference to found target (will become the furtherest)
			furtherestTarget = target;

			// Attempt to obtain the next target
			link = (L) target;
			Target<T> result = retrieveTarget(link, traverser, targetType, false, issues, traversedLinks);
			if (result.isError) {
				return null;
			}
			target = result.target;
		}

		// Return the furtherest target
		return furtherestTarget;
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param <L>
	 *            Link type.
	 * @param <T>
	 *            Target type.
	 * @param link
	 *            Starting link.
	 * @param traverser
	 *            {@link Traverser} to traverse the links.
	 * @param targetType
	 *            Target type to retrieve.
	 * @param isIssueOnNoTarget
	 *            Indicates if issue should be made if target not found.
	 * @param traversedLinks
	 *            Optional traversed links. May be <code>null</code>.
	 * @return Target link or <code>null</code> if issue obtaining which is
	 *         reported to the {@link CompilerIssues}.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Node, L extends Node> Target<T> retrieveTarget(L link, Traverser<L> traverser,
			Class<T> targetType, boolean isIssueOnNoTarget, CompilerIssues issues, Set<Object> traversedLinks) {

		// Ensure have starting link
		if (link == null) {
			throw new IllegalArgumentException("No starting link to find " + targetType.getSimpleName());
		}

		// Ensure have traversed links
		if (traversedLinks == null) {
			traversedLinks = new HashSet<Object>();
		}

		// Must traverse away from first link
		L previousLink = link;
		link = traverser.getNextLinkNode(link);
		traversedLinks.add(previousLink);

		// Traverse the links until find target type
		while (link != null) {

			// Determine if a cycle
			if (traversedLinks.contains(link)) {
				// In a cycle
				issues.addIssue(previousLink, previousLink.getNodeName() + " results in a cycle on linking to a "
						+ targetType.getSimpleName());
				return new Target<T>(null, true);
			}

			// Keep track for cycles
			traversedLinks.add(link);

			// Determine if link of correct target type
			if (targetType.isInstance(link)) {
				// Found the target
				return new Target<T>((T) link, false);
			}

			// Traverse to next link
			previousLink = link;
			link = traverser.getNextLinkNode(link);
		}

		// Run out of links, so could not find target
		if (isIssueOnNoTarget) {
			issues.addIssue(previousLink, previousLink.getNodeType() + " " + previousLink.getNodeName()
					+ " is not linked to a " + targetType.getSimpleName());
		}
		return new Target<T>(null, false); // target not found
	}

	/**
	 * Indicates result of retrieve.
	 */
	private static class Target<T extends Node> {

		/**
		 * Target. May be <code>null</code>.
		 */
		public T target;

		/**
		 * Flag indicating if error in attempting to obtain target.
		 */
		public boolean isError;

		/**
		 * Instantiate.
		 * 
		 * @param target
		 *            Target. May be <code>null</code>.
		 * @param isError
		 *            Flag indicating if error in attempting to obtain target.
		 */
		public Target(T target, boolean isError) {
			this.target = target;
			this.isError = isError;
		}
	}

	/**
	 * Traverser over the links.
	 */
	private static interface Traverser<L> {

		/**
		 * Traverses to the next link.
		 * 
		 * @param link
		 *            Current link node.
		 * @return Next link node.
		 */
		L getNextLinkNode(L link);
	}

	/**
	 * All access via static methods.
	 */
	private LinkUtil() {
	}

}