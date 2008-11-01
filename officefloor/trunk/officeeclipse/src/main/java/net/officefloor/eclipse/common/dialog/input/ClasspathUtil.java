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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarEntryResource;

/**
 * Utility methods for working with classpath.
 * 
 * @author Daniel
 */
public class ClasspathUtil {

	/**
	 * Obtains the class name of the input {@link IJavaElement}.
	 * 
	 * @param classFile
	 *            {@link IClassFile}.
	 * @return Class name or <code>null</code> if {@link IJavaElement} not a
	 *         class or contained within a class.
	 */
	public static String getClassName(IJavaElement javaElement) {

		// Find the type
		IType type;
		if (javaElement instanceof IType) {
			type = (IType) javaElement;
		} else if (javaElement instanceof IClassFile) {
			type = ((IClassFile) javaElement).getType();
		} else if (javaElement instanceof ICompilationUnit){
			ICompilationUnit unit = (ICompilationUnit) javaElement;
			
			// Strip extension from name
			String name = javaElement.getElementName().split("\\.")[0];
			
			// Obtain the type
			type = unit.getType(name);
		} else {
			// Look upwards for type
			type = (IType) javaElement.getAncestor(IJavaElement.TYPE);
		}

		// Ensure have type
		if (type == null) {
			return null;
		}

		// Determine the fully qualified name
		return type.getFullyQualifiedName();
	}

	/**
	 * Obtains the location on the class path for the input full path.
	 * 
	 * @param fullPath
	 *            Full path.
	 * @return Location on the class path for the input full path.
	 */
	public static String getClassPathLocation(String fullPath) {

		// Obtain the java element
		IPath path = new Path(fullPath);

		// Obtain the resource for the path
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = workspaceRoot.findMember(path);

		// Obtain the java element
		IJavaElement javaElement = null;
		do {

			// Ensure have the resource
			if (resource == null) {
				// Did not find java element for resource
				return null;
			}

			// Obtain the java element from the resource
			javaElement = JavaCore.create(resource);

			// Obtain the parent resource
			resource = resource.getParent();

		} while (javaElement == null);

		// Obtain the package fragment root for the java element
		IPackageFragmentRoot fragmentRoot = null;
		do {

			// Determine if package fragment root
			if (javaElement instanceof IPackageFragmentRoot) {
				fragmentRoot = (IPackageFragmentRoot) javaElement;
			}

			// Obtain the parent java element
			javaElement = javaElement.getParent();

		} while (fragmentRoot == null);

		// Ensure have a package fragment root
		if (fragmentRoot == null) {
			// Did not find the fragment root
			return null;
		}

		// Obtain the fragment root full path
		String fragmentPath = fragmentRoot.getResource().getFullPath()
				.toString()
				+ "/";

		// Obtain the class path location (by removing fragment root path)
		String location = fullPath.substring(fragmentPath.length());

		// Return the location
		return location;
	}

	/**
	 * Obtains the descendants of the input item.
	 * 
	 * @param item
	 *            Item.
	 * @return Descendants of the item.
	 */
	public static Object[] getDescendants(Object item) {
		List<Object> descendants = new LinkedList<Object>();
		loadDescendants(item, descendants);
		return descendants.toArray();
	}

	/**
	 * Loads the descendants of the input item.
	 * 
	 * @param item
	 *            Item.
	 * @param descendants
	 *            Listing to add descendants.
	 */
	public static void loadDescendants(Object item, List<Object> descendants) {
		// Load the children
		for (Object child : getChildren(item)) {

			// Add the child
			descendants.add(child);

			// Load the grand children
			loadDescendants(child, descendants);
		}
	}

	/**
	 * Obtains the children of the input parent.
	 * 
	 * @param parent
	 *            Parent.
	 * @return Children.
	 */
	public static Object[] getChildren(Object parent) {

		// Return children based on type
		if (parent instanceof IResource) {
			IResource resource = (IResource) parent;
			return getChildren(resource);

		} else if (parent instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) parent;
			return getChildren(javaElement);

		} else if (parent instanceof JarEntryResource) {
			JarEntryResource jarEntryResource = (JarEntryResource) parent;
			return getChildren(jarEntryResource);

		} else {
			// Unhandled type
			throw new OfficeFloorPluginFailure("Unhandled parent type "
					+ parent.getClass().getName());
		}
	}

	/**
	 * Obtains the children of the {@link IResource}.
	 * 
	 * @param resource
	 *            {@link IResource}.
	 * @return Children.
	 */
	public static Object[] getChildren(IResource resource) {
		try {
			// Handle based on type of resource
			if (resource instanceof IWorkspaceRoot) {
				IWorkspaceRoot workspaceRoot = (IWorkspaceRoot) resource;

				// Return the open projects of the workspace
				List<Object> projects = new LinkedList<Object>();
				for (IProject project : workspaceRoot.getProjects()) {
					if (project.isOpen()) {
						// Obtain the specific project
						Object specificProject = getSpecificProject(project);
						projects.add(specificProject);
					}
				}
				return projects.toArray();

			} else if (resource instanceof IProject) {
				IProject project = (IProject) resource;

				// Obtain the specific project
				Object specificProject = getSpecificProject(project);

				// Return children if only a java project
				if (!(specificProject instanceof IJavaProject)) {
					// Not java project so no class path children
					return new Object[0];
				}
				IJavaProject javaProject = (IJavaProject) specificProject;

				// Return the children of the java project
				return getChildren(javaProject);

			} else if (resource instanceof IFolder) {
				IFolder folder = (IFolder) resource;

				// Return the sub folders and files of folder
				return folder.members();

			} else if (resource instanceof IFile) {

				// No children of file
				return new Object[0];

			} else {
				// Unhandled resource
				throw new OfficeFloorPluginFailure("Unhandled resource type "
						+ resource.getClass().getName());
			}

		} catch (CoreException ex) {
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/**
	 * Obtains the children of the {@link IJavaElement}.
	 * 
	 * @param javaElement
	 *            {@link IJavaElement}.
	 * @return Children.
	 */
	public static Object[] getChildren(IJavaElement javaElement) {
		try {
			// Children to return
			List<Object> children = new LinkedList<Object>();

			// Handle based on type of java element
			if (javaElement instanceof IJavaProject) {
				IJavaProject javaProject = (IJavaProject) javaElement;

				// Add the package fragment roots on the class path
				IClasspathEntry[] classPath = javaProject
						.getResolvedClasspath(true);
				for (IClasspathEntry entry : classPath) {
					// Obtain the Package Fragment Root of the class path entry
					IPath entryPath = entry.getPath();
					IPackageFragmentRoot fragmentRoot = javaProject
							.findPackageFragmentRoot(entryPath);

					// Add the package fragment root
					children.add(fragmentRoot);
				}

			} else if (javaElement instanceof IPackageFragmentRoot) {
				IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) javaElement;

				// Add the package fragment root children
				children.addAll(Arrays.asList(fragmentRoot.getChildren()));
				children.addAll(Arrays.asList(fragmentRoot
						.getNonJavaResources()));

			} else if (javaElement instanceof IPackageFragment) {
				IPackageFragment fragment = (IPackageFragment) javaElement;

				// Add the fragment children
				children.addAll(Arrays.asList(fragment.getChildren()));
				children.addAll(Arrays.asList(fragment.getNonJavaResources()));

			} else if (javaElement instanceof ITypeRoot) {

				// No children of class file

			} else {
				// Unhandled java type
				throw new OfficeFloorPluginFailure(
						"Unhandled java element type "
								+ javaElement.getClass().getName());
			}

			// Return the children
			return children.toArray();

		} catch (CoreException ex) {
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/**
	 * Obtains the children of the {@link JarEntryResource}.
	 * 
	 * @param jarEntryResource
	 *            {@link JarEntryResource}.
	 * @return Children.
	 */
	public static Object[] getChildren(JarEntryResource jarEntryResource) {
		return jarEntryResource.getChildren();
	}

	/**
	 * Obtains the specific project, being either {@link IJavaProject} of the
	 * input {@link IProject} or the input {@link IProject}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @return Specific project.
	 */
	public static Object getSpecificProject(IProject project) {
		try {
			// Determine if is a java project
			if (project.hasNature(JavaCore.NATURE_ID)) {
				// Obtain the java project
				IJavaProject javaProject = JavaCore.create(project);

				// Return the java project
				return javaProject;

			} else {
				// Return project as is
				return project;
			}

		} catch (CoreException ex) {
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/**
	 * All access via static methods.
	 */
	private ClasspathUtil() {
	}

}
