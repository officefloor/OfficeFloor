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
package net.officefloor.eclipse.common.dialog.input;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.classpath.ClasspathUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;

/**
 * Provides filtering based on the various class path types.
 * 
 * @author Daniel
 */
public class ClasspathFilter {

	/**
	 * Filters the items to be included.
	 */
	private final List<ItemFilter<?>> itemFilters = new LinkedList<ItemFilter<?>>();

	/**
	 * Default constructor.
	 */
	public ClasspathFilter() {
	}

	/**
	 * Initiate with an {@link InputFilter}.
	 * 
	 * @param type
	 *            Item type that this filter applies.
	 * @param filter
	 *            {@link InputFilter} on the items of the type.
	 */
	public <I> ClasspathFilter(Class<? extends I> type, InputFilter<I> filter) {
		this.addFilter(type, filter);
	}

	/**
	 * <p>
	 * Filters out items.
	 * <p>
	 * This will include items that an {@link InputFilter} is not provided for.
	 * 
	 * @param items
	 *            Items to be filtered.
	 * @return Filtered list of items.
	 */
	public Object[] filter(Object... items) {

		// Filter the items
		List<Object> includedItems = new LinkedList<Object>();
		for (Object item : items) {

			// Determine if item is to be filtered
			boolean isFilter = false;
			for (ItemFilter<?> itemFilter : this.itemFilters) {
				if (itemFilter.isFilter(item)) {
					// Item to be filtered out
					isFilter = true;

					// Item filtered, no need to check further
					break;
				}
			}

			// Determine if include the item
			if (!isFilter) {
				includedItems.add(item);
			}
		}

		// Return the included items
		return includedItems.toArray();
	}

	/**
	 * <p>
	 * Includes items.
	 * <p>
	 * This will NOT include items that does not have an {@link InputFilter}
	 * specifically provided.
	 * 
	 * @param items
	 *            Items to be included.
	 * @return Included list of items.
	 */
	public Object[] include(Object... items) {

		// Include the items
		List<Object> includedItems = new LinkedList<Object>();
		for (Object item : items) {

			// Determine if item is to be included
			boolean isInclude = false;
			for (ItemFilter<?> itemFilter : this.itemFilters) {
				if (itemFilter.isSpecificallyInclude(item)) {
					// Item to be included
					isInclude = true;

					// Item included, no need to check further
					break;
				}
			}

			// Determine if include the item
			if (isInclude) {
				includedItems.add(item);
			}
		}

		// Return the included items
		return includedItems.toArray();
	}

	/**
	 * Filters the items to ensure it has a descendant item not filtered out.
	 * 
	 * @param items
	 *            Items to be filtered.
	 * @return Filtered list of items.
	 */
	public Object[] descendantFilter(Object... items) {

		// Iterate over items including only if have include descendant
		List<Object> includedItems = new LinkedList<Object>();
		for (Object item : items) {

			// Determine is specifically include item
			if (this.include(item).length == 1) {
				// Include item
				includedItems.add(item);

			} else {
				// Obtain the descendants
				Object[] descendants = ClasspathUtil.getDescendants(item);

				// Obtain the descendant includes
				Object[] includes = this.include(descendants);

				// Add only if have at least one include
				if (includes.length > 0) {
					includedItems.add(item);
				}
			}
		}

		// Return the included items
		return includedItems.toArray();
	}

	/**
	 * Facade method to filter to just classes.
	 */
	public void addJavaClassFilter() {
		this.addJavaElementFilter(new InputFilter<IJavaElement>() {
			@Override
			public boolean isFilter(IJavaElement item) {
				return !(item instanceof ITypeRoot);
			}
		});
	}

	/**
	 * Adds a filter.
	 * 
	 * @param type
	 *            Item type that this filter applies.
	 * @param filter
	 *            {@link InputFilter} on the items of the type.
	 */
	public <I> void addFilter(Class<? extends I> type, InputFilter<I> filter) {
		this.itemFilters.add(new ItemFilter<I>(type, filter));
	}

	/**
	 * Adds a {@link IResource} filter.
	 * 
	 * @param filter
	 *            Filter.
	 */
	public void addResourceFilter(InputFilter<IResource> filter) {
		this.addFilter(IResource.class, filter);
	}

	/**
	 * Adds a {@link IProject} filter.
	 * 
	 * @param filter
	 *            Filter.
	 */
	public void addProjectFilter(InputFilter<? super IProject> filter) {
		this.addFilter(IProject.class, filter);
	}

	/**
	 * Adds a {@link IFolder} filter.
	 * 
	 * @param filter
	 *            Filter.
	 */
	public void addFolderFilter(InputFilter<? super IFolder> filter) {
		this.addFilter(IFolder.class, filter);
	}

	/**
	 * Adds a {@link IFile} filter.
	 * 
	 * @param filter
	 */
	public void addFileFilter(InputFilter<? super IFile> filter) {
		this.addFilter(IFile.class, filter);
	}

	/**
	 * Adds a {@link IJavaElement} filter.
	 * 
	 * @param filter
	 *            Filter.
	 */
	public void addJavaElementFilter(InputFilter<? super IJavaElement> filter) {
		this.addFilter(IJavaElement.class, filter);
	}

	/**
	 * Adds a {@link IJavaProject} filter.
	 * 
	 * @param filter
	 *            Filter.
	 */
	public void addJavaProjectFilter(InputFilter<? super IJavaProject> filter) {
		this.addFilter(IJavaProject.class, filter);
	}

	/**
	 * Adds a {@link IPackageFragmentRoot} filter.
	 * 
	 * @param filter
	 *            Filter.
	 */
	public void addPackageFragmentRootFilter(
			InputFilter<? super IPackageFragmentRoot> filter) {
		this.addFilter(IPackageFragmentRoot.class, filter);
	}

	/**
	 * Adds a {@link IPackageFragment} filter.
	 * 
	 * @param filter
	 *            Filter.
	 */
	public void addPackageFragmentFilter(
			InputFilter<? super IPackageFragment> filter) {
		this.addFilter(IPackageFragment.class, filter);
	}

	/**
	 * Adds a {@link IClassFile} filter.
	 * 
	 * @param filter
	 *            Filter.
	 */
	public void addClassFileFilter(InputFilter<? super IClassFile> filter) {
		this.addFilter(IClassFile.class, filter);
	}

	/**
	 * Item filter.
	 */
	private class ItemFilter<I> {

		/**
		 * Type of item to filter.
		 */
		private final Class<? extends I> itemType;

		/**
		 * {@link InputFilter} for the item type.
		 */
		private final InputFilter<I> itemFilter;

		/**
		 * Initiate.
		 * 
		 * @param itemType
		 *            Type of item to filter.
		 * @param itemFilter
		 *            {@link InputFilter} for the item type.
		 */
		public ItemFilter(Class<? extends I> itemType, InputFilter<I> itemFilter) {
			this.itemType = itemType;
			this.itemFilter = itemFilter;
		}

		/**
		 * Determines if filter out the item.
		 * 
		 * @param item
		 *            Item to be filtered.
		 * @return <code>true</code> to filter out the item.
		 */
		@SuppressWarnings("unchecked")
		public boolean isFilter(Object item) {

			// Determine if may filter on the item
			if (!this.itemType.isInstance(item)) {

				// Determine if java element
				if (item instanceof IJavaElement) {
					// Determine if filter on resource
					IJavaElement javaElement = (IJavaElement) item;
					return this.isFilter(javaElement.getResource());

				} else {
					// Can not handle type so do not filter
					return false;
				}
			}

			// Downcast to specific item
			I specificItem = (I) item;

			// Determine if filter
			boolean isFilter = this.itemFilter.isFilter(specificItem);

			// Return whether filter
			return isFilter;
		}

		/**
		 * Determines if the {@link InputFilter} specifically includes the item.
		 * 
		 * @param item
		 *            Item to be specifically included.
		 * @return <code>true</code> to specifically include.
		 */
		@SuppressWarnings("unchecked")
		public boolean isSpecificallyInclude(Object item) {

			// Determine if may include the item
			if (!this.itemType.isInstance(item)) {

				// Determine if java element
				if (item instanceof IJavaElement) {
					// Determine if specifically include on resource
					IJavaElement javaElement = (IJavaElement) item;
					return this
							.isSpecificallyInclude(javaElement.getResource());

				} else {
					// Can not handle type so do not specifically include
					return false;
				}
			}

			// Downcast to specific item
			I specificItem = (I) item;

			// Determine if include
			boolean isInclude = !this.itemFilter.isFilter(specificItem);

			// Return whether specially included
			return isInclude;
		}
	}

}