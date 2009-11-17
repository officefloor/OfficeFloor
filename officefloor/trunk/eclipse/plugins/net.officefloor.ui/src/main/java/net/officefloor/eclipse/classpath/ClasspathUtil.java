/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.eclipse.classpath;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.classpathcontainer.OfficeFloorClasspathContainer;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.TypeClasspathProvision;
import net.officefloor.eclipse.extension.classpath.VariableClasspathProvision;
import net.officefloor.eclipse.repository.project.FileConfigurationItem;
import net.officefloor.eclipse.util.LogUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ide.IDE;

/**
 * Utility methods for working with class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathUtil {

	/**
	 * Loads the {@link Class} from the {@link IProject} class path.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param className
	 *            Class name.
	 * @return {@link Class}.
	 * @throws ClassNotFoundException
	 *             If {@link Class} not found.
	 */
	public static Class<?> loadProjectClass(IProject project, String className)
			throws ClassNotFoundException {

		// Obtain via project class loader
		ProjectClassLoader classLoader = ProjectClassLoader.create(project);
		Class<?> clazz = classLoader.loadClass(className);

		// Return the class
		return clazz;
	}

	/**
	 * <p>
	 * Attempts to update the class path of the {@link IJavaProject} for the
	 * edit part with the extension class names.
	 * <p>
	 * No exception is thrown if unable to update the class path and the class
	 * path is subsequently not updated.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @param monitor
	 *            {@link IProgressMonitor}.
	 * @param extensionClassNames
	 *            Listing of class names that may have extension associated.
	 */
	public static void attemptUpdateOfficeFloorClasspath(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart,
			IProgressMonitor monitor, String... extensionClassNames) {
		try {
			// Obtain the project for the edit part
			IProject project = FileConfigurationItem.getProject(editPart);

			// Attempt to update the class path
			updateOfficeFloorClasspath(project, monitor, extensionClassNames);

		} catch (Throwable ex) {
			// Indicate failure to update class path
			editPart.messageError(ex);
		}
	}

	/**
	 * Convenience method to ensure {@link OfficeFloorClasspathContainer} is
	 * available on the {@link IProject} and that the class paths for the input
	 * extensions are available.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param monitor
	 *            {@link IProgressMonitor}. May be <code>null</code>.
	 * @param extensionClassNames
	 *            Listing of class names that may have an extension associated.
	 * @throws Exception
	 *             If fails to ensure available.
	 */
	public static void updateOfficeFloorClasspath(IProject project,
			IProgressMonitor monitor, String... extensionClassNames)
			throws Exception {
		updateOfficeFloorClasspath(JavaCore.create(project), monitor,
				extensionClassNames);
	}

	/**
	 * Ensures the {@link OfficeFloorClasspathContainer} is available on the
	 * {@link IJavaProject} and that the class paths for the input extensions
	 * are available.
	 * 
	 * @param javaProject
	 *            {@link IJavaProject}.
	 * @param monitor
	 *            {@link IProgressMonitor}. May be <code>null</code>.
	 * @param extensionClassNames
	 *            Listing of class names that may have an extension associated.
	 * @throws Exception
	 *             If fails to ensure available.
	 */
	public static void updateOfficeFloorClasspath(IJavaProject javaProject,
			IProgressMonitor monitor, String... extensionClassNames)
			throws Exception {
		OfficeFloorClasspathContainer.addExtensionToProjectClasspath(
				javaProject, monitor, extensionClassNames);
	}

	/**
	 * Creates the {@link IClasspathEntry} from a {@link ClasspathProvision}.
	 * 
	 * @param provision
	 *            {@link ClasspathProvision}.
	 * @return {@link IClasspathEntry} or <code>null</code> if fails.
	 */
	public static IClasspathEntry createClasspathEntry(
			ClasspathProvision provision) {

		// Handle based on type of provision
		if (provision instanceof TypeClasspathProvision) {
			// Type provision
			TypeClasspathProvision typeProvision = (TypeClasspathProvision) provision;
			return createClasspathEntry(typeProvision.getType());
		} else if (provision instanceof VariableClasspathProvision) {
			// Variable provision
			VariableClasspathProvision variableProvision = (VariableClasspathProvision) provision;
			return createClasspathEntry(variableProvision.getVariable(),
					variableProvision.getPath());
		} else {
			// Unknown provision type
			String provisionTypeName = (provision == null ? null : provision
					.getClass().getName());
			MessageDialog.openWarning(null, "Unknown", "Unknown "
					+ ClasspathProvision.class.getSimpleName() + " type "
					+ provisionTypeName);
			return null;
		}
	}

	/**
	 * Obtains the {@link IClasspathEntry} of the input variable and path.
	 * 
	 * @param variable
	 *            Name of variable.
	 * @param path
	 *            Path from variable.
	 * @return {@link IClasspathEntry} for the variable or <code>null</code> if
	 *         fails to create.
	 */
	public static IClasspathEntry createClasspathEntry(String variable,
			String path) {

		// Create the path
		IPath variablePath = new Path(variable).append(path);

		// Return the variable class path entry
		return JavaCore.newVariableEntry(variablePath, null, null);
	}

	/**
	 * Obtains the {@link IClasspathEntry} of the class path containing the
	 * {@link Class}.
	 * 
	 * @param clazz
	 *            {@link Class}.
	 * @return {@link IClasspathEntry} of the class path containing the
	 *         {@link Class} or <code>null</code> if issue obtaining.
	 */
	public static IClasspathEntry createClasspathEntry(Class<?> clazz) {

		try {
			// Obtain the class resource name
			String classResourceName = clazz.getName().replace('.', '/')
					+ ".class";

			// Obtain the URL to the class resource
			URL classUrl = clazz.getClassLoader()
					.getResource(classResourceName);
			URL resolvedUrl = FileLocator.resolve(classUrl);
			String resolvedPath = resolvedUrl.getPath();

			// Determine the class path
			String classpath;
			if (resolvedPath.startsWith("file:")) {
				// Class contained in file, strip path to obtain file
				resolvedPath = resolvedPath.substring("file:".length());
				int endOfFilePath = resolvedPath.indexOf('!');
				classpath = resolvedPath.substring(0, endOfFilePath);

			} else {
				// Plain directory, obtain path to start of class directory
				String path = new File(resolvedUrl.toURI()).getAbsolutePath();
				classpath = path.substring(0,
						(path.length() - classResourceName.length()));
			}

			// Obtain the path for the class path
			IPath classpathPath = new Path(classpath);

			// Return the class path entry
			return JavaCore.newLibraryEntry(classpathPath, null, null);

		} catch (Throwable ex) {

			// Log failure to obtain class path for class
			LogUtil.logError("Failed to obtain class path for class "
					+ (clazz == null ? null : clazz.getName()), ex);

			// No class path as failed to obtain
			return null;
		}
	}

	/**
	 * Opens the class path resource.
	 * 
	 * @param resourcePath
	 *            Path to the resource on the class path.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} opening the resource.
	 */
	public static void openClasspathResource(String resourcePath,
			AbstractOfficeFloorEditor<?, ?> editor) {

		try {
			// Obtain the URL with full path
			ProjectClassLoader projectClassLoader = ProjectClassLoader
					.create(editor);
			URL url = projectClassLoader.getResource(resourcePath);
			if (url == null) {
				// Can not find item to open
				MessageDialog.openWarning(editor.getEditorSite().getShell(),
						"Open", "Can not find '" + resourcePath + "'");
				return;
			}

			// Obtain the file to open
			String urlFilePath = url.getFile();
			IPath path = new Path(urlFilePath);
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocation(path);
			if (files.length != 1) {
				// Can not find file
				MessageDialog.openWarning(editor.getEditorSite().getShell(),
						"Open", "Can not find '" + resourcePath + "' at ["
								+ urlFilePath + "]");
				return;
			}
			IFile file = files[0];

			// Open the file
			IDE.openEditor(editor.getEditorSite().getPage(), file);

		} catch (Throwable ex) {
			// Failed to open file
			MessageDialog.openInformation(editor.getEditorSite().getShell(),
					"Open", "Failed to open '" + resourcePath + "': "
							+ ex.getMessage());
		}
	}

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
		} else if (javaElement instanceof ICompilationUnit) {
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
		return getClassPathLocation(new Path(fullPath));
	}

	/**
	 * Obtains the location on the class path for the input {@link IPath}.
	 * 
	 * @param path
	 *            {@link IPath}.
	 * @return Location on the class path for the input {@link IPath}.
	 */
	public static String getClassPathLocation(IPath path) {

		// Obtain the resource for the path
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource pathResource = workspaceRoot.findMember(path);
		IResource resource = pathResource;

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
		String fullPath = pathResource.getFullPath().toString();
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
	// TODO remove as use StandardJavaElementContentProvider
	@Deprecated
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
	// TODO remove as use StandardJavaElementContentProvider
	public static Object[] getChildren(Object parent) {

		// No children for null
		if (parent == null) {
			return new Object[0];
		}

		// Return children based on type
		if (parent instanceof IResource) {
			IResource resource = (IResource) parent;
			return getChildren(resource);

		} else if (parent instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) parent;
			return getChildren(javaElement);

		} else if (parent instanceof IJarEntryResource) {
			IJarEntryResource jarEntryResource = (IJarEntryResource) parent;
			return getChildren(jarEntryResource);

		} else {
			// Unhandled type
			MessageDialog.openWarning(null, "Unknown", "Unhandled parent type "
					+ parent.getClass().getName());
			return new Object[0];
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
				MessageDialog.openWarning(null, "Unhandled resource type",
						"Unhandled resource type "
								+ resource.getClass().getName());
				return new Object[0];
			}

		} catch (CoreException ex) {
			MessageDialog.openError(null, "Error", ex.getMessage());
			return new Object[0];
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
				children.addAll(Arrays.asList(fragment.getClassFiles()));
				children.addAll(Arrays.asList(fragment.getCompilationUnits()));
				children.addAll(Arrays.asList(fragment.getNonJavaResources()));

			} else if (javaElement instanceof ITypeRoot) {

				// No children of class file

			} else {
				// Unhandled java type
				MessageDialog.openWarning(null, "Unhandled java element type",
						"Unhandled java element type "
								+ javaElement.getClass().getName());
			}

			// Return the children
			return children.toArray();

		} catch (CoreException ex) {
			MessageDialog.openError(null, "Error", ex.getMessage());
			return new Object[0];
		}
	}

	/**
	 * Obtains the children of the {@link IJarEntryResource}.
	 * 
	 * @param jarEntryResource
	 *            {@link IJarEntryResource}.
	 * @return Children.
	 */
	public static Object[] getChildren(IJarEntryResource jarEntryResource) {
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
			MessageDialog.openError(null, "Error", ex.getMessage());
			return project; // just return the project
		}
	}

	/**
	 * All access via static methods.
	 */
	private ClasspathUtil() {
	}

}