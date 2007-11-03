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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.dialog.PropertyInput;
import net.officefloor.eclipse.common.dialog.PropertyInputContext;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * {@link PropertyInput} that utilises a {@link SelectResourcesBlock}.
 * 
 * @author Daniel
 */
public class ClasspathResourceSelectionPropertyInput implements
		PropertyInput<Tree> {

	/**
	 * Root of tree.
	 */
	private final IProject root;

	/**
	 * List of extensions for files to include.
	 */
	private final String[] extensions;

	/**
	 * Initiate.
	 * 
	 * @param root
	 *            Root of the tree.
	 */
	public ClasspathResourceSelectionPropertyInput(IProject root,
			String... extensions) {
		this.root = root;
		this.extensions = extensions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.PropertyInput#buildControl(net.officefloor.eclipse.common.dialog.PropertyInputContext)
	 */
	@Override
	public Tree buildControl(PropertyInputContext context) {

		// Create the tree
		Tree tree = new Tree(context.getParent(), SWT.NONE);

		// Create the Tree selection
		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(this.getResourceProvider(IResource.ROOT
				| IResource.PROJECT | IResource.FOLDER | IResource.FILE));
		treeViewer.setLabelProvider(WorkbenchLabelProvider
				.getDecoratingWorkbenchLabelProvider());
		treeViewer.setInput(this.root);

		// Return the tree
		return tree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.PropertyInput#getValue(org.eclipse.swt.widgets.Control,
	 *      net.officefloor.eclipse.common.dialog.PropertyInputContext)
	 */
	@Override
	public String getValue(Tree control, PropertyInputContext context) {

		// Obtain the selection from the tree
		TreeItem[] treeItems = control.getSelection();

		// Ensure only one tree item selected
		if (treeItems.length != 1) {
			return null;
		}

		// Obtain the data of selection
		TreeItem treeItem = treeItems[0];
		Object data = treeItem.getData();

		// Ensure resource is a file
		if (!(data instanceof IFile)) {
			return null;
		}
		IFile file = (IFile) data;

		// Obtains the parent path
		String parentPath = "";
		Object parentData = treeItem.getParentItem().getData();
		if (parentData instanceof IPackageFragment) {
			IPackageFragment packageFragment = (IPackageFragment) parentData;
			if (!packageFragment.isDefaultPackage()) {
				parentPath = packageFragment.getElementName().replace(".", "/")
						+ "/";
			}
		}

		// Prepend parent path to file
		String classPathFileLocation = parentPath + file.getName();

		// Return the path
		return classPathFileLocation;
	}

	/**
	 * Obtains the {@link ITreeContentProvider}.
	 * 
	 * @param resourceType
	 *            Resource type.
	 */
	private ITreeContentProvider getResourceProvider(final int resourceType) {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				try {
					if (o instanceof IResource) {

						// Handle the resource
						IResource resource = (IResource) o;
						switch (resource.getType()) {

						case IResource.FILE:
							// No children for File
							return new Object[0];

						case IResource.PROJECT:
							// Obtain the project
							IProject project = resource.getProject();

							// Obtain the Class Path for Java Project
							IJavaProject javaProject = JavaCore.create(project);
							IClasspathEntry[] classPath = javaProject
									.getResolvedClasspath(true);

							// Create the list for tree root
							List<Object> results = new LinkedList<Object>();
							for (IClasspathEntry entry : classPath) {
								// Add the Package Fragment Root for class path
								// entry
								IPath entryPath = entry.getPath();
								IPackageFragmentRoot sourcePackageFragmentRoot = javaProject
										.findPackageFragmentRoot(entryPath);
								if (sourcePackageFragmentRoot != null) {
									results.add(sourcePackageFragmentRoot);
								}
							}

							// Return the class path roots
							return results.toArray();
						}

						// Determine if a container
						if (o instanceof IContainer) {

							// Obtain the members of the container
							IResource[] members = null;
							try {
								members = ((IContainer) o).members();
							} catch (CoreException e) {
								// Return nothing as failed
								return new Object[0];
							}

							// Filter in only the desired resources
							List<IResource> results = new LinkedList<IResource>();
							for (IResource member : members) {
								// Include only if in types of resource
								if ((member.getType() & resourceType) > 0) {
									results.add(member);
								}
							}

							// Return the results
							return results.toArray();
						}

						// Unknown resource, therefore return empty
						return new Object[0];
					}

					// Package Fragment Root
					if (o instanceof IPackageFragmentRoot) {
						IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) o;
						List<Object> results = new LinkedList<Object>();

						// Include only Package Fragments containing relevant
						// files
						for (IJavaElement javaElement : packageFragmentRoot
								.getChildren()) {
							if (javaElement instanceof IPackageFragment) {
								IPackageFragment packageFragment = (IPackageFragment) javaElement;
								// Only include if have files not filtered out
								if (ClasspathResourceSelectionPropertyInput.this
										.filterResources(packageFragment
												.getChildren()).length > 0) {
									results.add(packageFragment);
								}
							}
						}

						// Add files matching that directly under root
						results
								.addAll(Arrays
										.asList(ClasspathResourceSelectionPropertyInput.this
												.filterResources(packageFragmentRoot
														.getNonJavaResources())));

						// Return the Package Fragment Root list
						return results.toArray();
					}

					// Package Fragment
					if (o instanceof IPackageFragment) {
						IPackageFragment packageFragment = (IPackageFragment) o;
						return ClasspathResourceSelectionPropertyInput.this
								.filterResources(packageFragment
										.getNonJavaResources());
					}

					// Unknown type, therefore return empty
					return new Object[0];

				} catch (Exception ex) {
					// Propagate failure
					throw new OfficeFloorPluginFailure(ex);
				}
			}
		};
	}

	/**
	 * Filters the input resources.
	 * 
	 * @param resources
	 *            Resources to be filtered.
	 * @return Resources filtered in.
	 */
	private Object[] filterResources(Object[] resources) {
		List<Object> results = new LinkedList<Object>();
		if (resources != null) {
			for (Object resource : resources) {
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;

					// Determine if include file
					boolean isInclude = false;
					if (this.extensions.length == 0) {
						// No filtering
						isInclude = true;
					} else {
						// Filter based on extension
						String fileExtension = file.getFileExtension();
						for (String extension : this.extensions) {
							if ((extension != null)
									&& (extension.equals(fileExtension))) {
								isInclude = true;
							}
						}
					}

					// Add if include
					if (isInclude) {
						results.add(resource);
					}
				}
			}
		}
		return results.toArray();
	}

}
