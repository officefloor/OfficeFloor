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
package net.officefloor.eclipse.common.dialog.input.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.dialog.input.Input;
import net.officefloor.eclipse.common.dialog.input.InputContext;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * {@link Input} that utilises a {@link SelectResourcesBlock}.
 * 
 * @author Daniel
 */
public class ClasspathResourceSelectionInput implements Input<Tree> {

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
	 * @param extensions
	 *            Valid extensions.
	 */
	public ClasspathResourceSelectionInput(IProject root,
			String... extensions) {
		this.root = root;
		this.extensions = extensions;
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param editor
	 *            {@link IEditorPart}.
	 * @param extensions
	 *            Valid extensions.
	 */
	public ClasspathResourceSelectionInput(IEditorPart editor,
			String... extensions) {
		// Obtain the project of the editor
		this.root = ProjectConfigurationContext.getProject(editor
				.getEditorInput());
		this.extensions = extensions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.dialog.PropertyInput#buildControl(net.officefloor.eclipse.common.dialog.PropertyInputContext)
	 */
	@Override
	public Tree buildControl(InputContext context) {

		// Create the tree
		Tree tree = new Tree(context.getParent(), SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 150;	// hint on height
		tree.setLayoutData(gridData);

		// Create the Tree selection
		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setAutoExpandLevel(2);
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
	public String getValue(Tree control, InputContext context) {

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

							// Obtain appropriate fragment roots
							List<Object> results = new LinkedList<Object>();
							for (IPackageFragmentRoot fragmentRoot : this
									.getPackageFragmentRoots(javaProject)) {
								if (this
										.containsFilteredResources(fragmentRoot)) {
									results.add(fragmentRoot);
								}
							}
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

						// Include Package Fragments containing relevant files
						List<Object> results = new LinkedList<Object>();
						for (IJavaElement javaElement : packageFragmentRoot
								.getChildren()) {
							if (javaElement instanceof IPackageFragment) {
								IPackageFragment packageFragment = (IPackageFragment) javaElement;

								// Only include if have files not filtered out
								if (this
										.containsFilteredResources(packageFragment)) {
									results.add(packageFragment);
								}
							}
						}
						return results.toArray();
					}

					// Package Fragment
					if (o instanceof IPackageFragment) {
						IPackageFragment packageFragment = (IPackageFragment) o;
						return this.getFilteredResources(packageFragment);
					}

					// Unknown type, therefore return empty
					return new Object[0];

				} catch (Exception ex) {
					// Propagate failure
					throw new OfficeFloorPluginFailure(ex);
				}
			}

			/**
			 * Obtains the {@link IPackageFragmentRoot} instances.
			 * 
			 * @param javaProject
			 *            {@link IJavaProject}.
			 * @return {@link IPackageFragmentRoot} instances.
			 * @throws Exception
			 *             If fails.
			 */
			private IPackageFragmentRoot[] getPackageFragmentRoots(
					IJavaProject javaProject) throws Exception {
				IClasspathEntry[] classPath = javaProject
						.getResolvedClasspath(true);
				List<IPackageFragmentRoot> fragmentRoots = new LinkedList<IPackageFragmentRoot>();
				for (IClasspathEntry entry : classPath) {
					// Add the Package Fragment Root
					IPath entryPath = entry.getPath();
					IPackageFragmentRoot fragmentRoot = javaProject
							.findPackageFragmentRoot(entryPath);
					fragmentRoots.add(fragmentRoot);
				}
				return fragmentRoots.toArray(new IPackageFragmentRoot[0]);
			}

			/**
			 * Indicates if the input {@link IJavaElement} contains filtered
			 * descendant non-java resources.
			 * 
			 * @param javaElement
			 *            {@link IJavaElement}.
			 * @return <code>true</code> if contains filtered descendant
			 *         non-java resources.
			 * @throws Exception
			 *             If fails.
			 */
			private boolean containsFilteredResources(IJavaElement javaElement)
					throws Exception {
				return (this.getFilteredResources(javaElement).length > 0);
			}

			/**
			 * Obtains the filtered descendant non-java resources.
			 * 
			 * @param javaElement
			 *            {@link IJavaElement}.
			 * @return Filtered non-java descendant resources.
			 * @throws Exception
			 *             If fails.
			 */
			private Object[] getFilteredResources(IJavaElement javaElement)
					throws Exception {
				return ClasspathResourceSelectionInput.this
						.filterResources(this.getNonJavaResources(javaElement));
			}

			/**
			 * Obtains the descendant non-java resources.
			 * 
			 * @param javaElement
			 *            {@link IJavaElement}.
			 * @return Non-java descendant resources.
			 * @throws Exception
			 *             If fails.
			 */
			private Object[] getNonJavaResources(IJavaElement javaElement)
					throws Exception {
				List<Object> list = new LinkedList<Object>();
				this.loadDescendantNonJavaResources(list, javaElement);
				return list.toArray();
			}

			/**
			 * Obtains the descendant non-java resources.
			 * 
			 * @param list
			 *            List to be populated.
			 * @param javaElement
			 *            {@link IJavaElement}.
			 * @throws Exception
			 *             If fails.
			 */
			private void loadDescendantNonJavaResources(List<Object> list,
					IJavaElement javaElement) throws Exception {
				if (javaElement instanceof IJavaProject) {
					// Load fragment roots of the java project
					IJavaProject javaProject = (IJavaProject) javaElement;
					IClasspathEntry[] classPath = javaProject
							.getResolvedClasspath(true);
					for (IClasspathEntry entry : classPath) {
						// Add the Package Fragment Root
						IPath entryPath = entry.getPath();
						IPackageFragmentRoot sourcePackageFragmentRoot = javaProject
								.findPackageFragmentRoot(entryPath);
						this.loadDescendantNonJavaResources(list,
								sourcePackageFragmentRoot);
					}

				} else if (javaElement instanceof IPackageFragmentRoot) {
					// Load fragments of fragment root
					IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) javaElement;
					for (IJavaElement child : packageFragmentRoot.getChildren()) {
						this.loadDescendantNonJavaResources(list, child);
					}
				} else if (javaElement instanceof IPackageFragment) {
					// Load non-java elements of fragment
					IPackageFragment packageFragment = (IPackageFragment) javaElement;
					list.addAll(Arrays.asList(packageFragment
							.getNonJavaResources()));
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
