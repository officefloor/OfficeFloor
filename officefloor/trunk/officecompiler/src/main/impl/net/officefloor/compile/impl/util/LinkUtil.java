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
package net.officefloor.compile.impl.util;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.Asset;

/**
 * Utility class to work with links.
 * 
 * @author Daniel
 */
public class LinkUtil {

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param link
	 *            Starting {@link LinkFlowNode}.
	 * @param targetType
	 *            Target {@link LinkFlowNode} type to retrieve.
	 * @param startingLinkName
	 *            Name of the starting {@link LinkFlowNode}.
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the {@link Asset}.
	 * @return Target {@link LinkFlowNode} or <code>null</code> if issue
	 *         obtaining which is reported to the {@link CompilerIssues}.
	 */
	public static <T> T retrieveTarget(LinkFlowNode link, Class<T> targetType,
			String startingLinkName, LocationType locationType,
			String location, AssetType assetType, String assetName,
			CompilerIssues issues) {

		// Create the link flow node traverser
		Traverser<LinkFlowNode> traverser = new Traverser<LinkFlowNode>() {
			@Override
			public LinkFlowNode getNextLinkNode(LinkFlowNode link) {
				return link.getLinkedFlowNode();
			}
		};

		// Return the retrieved target
		return retrieveTarget(link, traverser, targetType, startingLinkName,
				locationType, location, assetType, assetName, issues);
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param link
	 *            Starting {@link LinkObjectNode}.
	 * @param targetType
	 *            Target {@link LinkObjectNode} type to retrieve.
	 * @param startingLinkName
	 *            Name of the starting {@link LinkObjectNode}.
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the {@link Asset}.
	 * @return Target {@link LinkObjectNode} or <code>null</code> if issue
	 *         obtaining which is reported to the {@link CompilerIssues}.
	 */
	public static <T> T retrieveTarget(LinkObjectNode link,
			Class<T> targetType, String startingLinkName,
			LocationType locationType, String location, AssetType assetType,
			String assetName, CompilerIssues issues) {

		// Create the link object traverser
		Traverser<LinkObjectNode> traverser = new Traverser<LinkObjectNode>() {
			@Override
			public LinkObjectNode getNextLinkNode(LinkObjectNode link) {
				return link.getLinkedObjectNode();
			}
		};

		// Return the retrieved target
		return retrieveTarget(link, traverser, targetType, startingLinkName,
				locationType, location, assetType, assetName, issues);
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param link
	 *            Starting {@link LinkTeamNode}.
	 * @param targetType
	 *            Target {@link LinkTeamNode} type to retrieve.
	 * @param startingLinkName
	 *            Name of the starting {@link LinkTeamNode}.
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the {@link Asset}.
	 * @return Target {@link LinkTeamNode} or <code>null</code> if issue
	 *         obtaining which is reported to the {@link CompilerIssues}.
	 */
	public static <T> T retrieveTarget(LinkTeamNode link, Class<T> targetType,
			String startingLinkName, LocationType locationType,
			String location, AssetType assetType, String assetName,
			CompilerIssues issues) {

		// Create the link team traverser
		Traverser<LinkTeamNode> traverser = new Traverser<LinkTeamNode>() {
			@Override
			public LinkTeamNode getNextLinkNode(LinkTeamNode link) {
				return link.getLinkedTeamNode();
			}
		};

		// REturn the retrieved target
		return retrieveTarget(link, traverser, targetType, startingLinkName,
				locationType, location, assetType, assetName, issues);
	}

	/**
	 * Retrieves the target link by the specified type.
	 * 
	 * @param link
	 *            Starting link.
	 * @param traverser
	 *            {@link Traverser} to traverse the links.
	 * @param targetType
	 *            Target type to retrieve.
	 * @param startingLinkName
	 *            Name of the starting link.
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the {@link Asset}.
	 * @return Target link or <code>null</code> if issue obtaining which is
	 *         reported to the {@link CompilerIssues}.
	 */
	@SuppressWarnings("unchecked")
	private static <T, L> T retrieveTarget(L link, Traverser<L> traverser,
			Class<T> targetType, String startingLinkName,
			LocationType locationType, String location, AssetType assetType,
			String assetName, CompilerIssues issues) {

		// Ensure have starting link
		if (link != null) {

			// Must traverse away from first link
			link = traverser.getNextLinkNode(link);

			// Traverse the links until find target type
			Set<Object> traversedLinks = new HashSet<Object>();
			while (link != null) {

				// Determine if a cycle
				if (traversedLinks.contains(link)) {
					// In a cycle
					issues.addIssue(locationType, location, assetType,
							assetName, startingLinkName
									+ " results in a cycle on linking to a "
									+ targetType.getSimpleName());
					return null;
				}

				// Determine if link of correct target type
				if (targetType.isInstance(link)) {
					// Found the target
					return (T) link;
				}

				// Traverse to the next link to check
				traversedLinks.add(link);
				link = traverser.getNextLinkNode(link);
			}
		}

		// Run out of links (or no starting link), so could not find target
		issues.addIssue(locationType, location, assetType, assetName,
				startingLinkName + " is not linked to a "
						+ targetType.getSimpleName());
		return null; // target not found
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