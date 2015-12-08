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
package net.officefloor.plugin.web.http.resource.classpath;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.officefloor.plugin.web.http.resource.HttpResource;

/**
 * Node on the class path that may be retrieved as a {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathHttpResourceNode implements Serializable {

	/**
	 * Creates the {@link ClassPathHttpResourceNode} tree for available
	 * {@link HttpResource} instances.
	 * 
	 * @param classpathPrefix
	 *            The prefix on the class path to locate resources for the tree.
	 *            <code>null</code> for the entire class path.
	 * @return {@link ClassPathHttpResourceNode}.
	 */
	public static ClassPathHttpResourceNode createClassPathResourceTree(
			String classpathPrefix) {

		// Obtain the class path entries
		String classPath = System.getProperty("java.class.path");
		String[] classPathEntries = classPath.split(File.pathSeparator);

		// Create the root class path node for loading (always directory)
		ClassPathHttpResourceNode root = new ClassPathHttpResourceNode(null,
				"/", (classpathPrefix == null ? "" : classpathPrefix), true);

		// Iterate over class path entries creating the nodes
		for (String classPathEntry : classPathEntries) {
			File classPathFile = new File(classPathEntry);

			// Handle loading the class path file
			if (classPathFile.isDirectory()) {

				// Obtain the class path prefix directory
				if (classpathPrefix != null) {
					classPathFile = new File(classPathFile, classpathPrefix);
				}

				// Ensure the directory exists
				if (!classPathFile.exists()) {
					// Directory not exists, so ignore
					continue;
				}

				// Load the directory
				loadDirectoryEntries(root, classPathFile);
				continue; // loaded as directory
			}

			if (classPathFile.isFile()) {
				// Attempt to load as jar
				JarFile jarFile;
				try {
					jarFile = new JarFile(classPathFile);
				} catch (IOException ex) {
					// Not jar file, so ignore
					continue;
				}

				// Load as jar file
				loadJarEntries(root, jarFile, classpathPrefix);
			}

			// As here, then unknown class path entry so ignore
		}

		// Return the root of the tree
		return root;
	}

	/**
	 * Loads the {@link ClassPathHttpResourceNode} directory instances.
	 * 
	 * @param parent
	 *            Parent {@link ClassPathHttpResourceNode}.
	 * @param directory
	 *            Parent directory.
	 */
	private static void loadDirectoryEntries(ClassPathHttpResourceNode parent,
			File directory) {

		// Iterate over children of directory
		for (File file : directory.listFiles()) {

			// Determine if a directory
			boolean isDirectory = file.isDirectory();

			// Always add the node
			ClassPathHttpResourceNode child = addChild(parent, file.getName(),
					isDirectory);

			// Add children of directory
			if (isDirectory) {
				loadDirectoryEntries(child, file);
			}
		}
	}

	/**
	 * Loads the {@link ClassPathHttpResourceNode} instances for the JAR file.
	 * 
	 * @param tree
	 *            Tree root {@link ClassPathHttpResourceNode}.
	 * @param jarFile
	 *            {@link JarFile}.
	 * @param classpathPrefix
	 *            Class path prefix.
	 */
	private static void loadJarEntries(ClassPathHttpResourceNode tree,
			JarFile jarFile, String classpathPrefix) {

		// Ensure have class path prefix
		classpathPrefix = (classpathPrefix == null ? "" : classpathPrefix);

		// Iterate over the jar entries
		for (Enumeration<JarEntry> enumeration = jarFile.entries(); enumeration
				.hasMoreElements();) {
			JarEntry jarEntry = enumeration.nextElement();

			// Obtain the entry name
			String jarEntryName = jarEntry.getName();
			if (!jarEntryName.startsWith(classpathPrefix)) {
				continue; // not to be made available as resource
			}

			// Strip off class path prefix to find resource path
			String resourcePath = jarEntryName.substring(classpathPrefix
					.length());

			// Ignore root directory
			if ("/".equals(resourcePath)) {
				continue; // tree root directory, so not include
			}

			// Split resource path into child node paths for loading
			String[] nodePaths = resourcePath.split("/");

			// Add the parent directories (-1 to ignore leaf node)
			ClassPathHttpResourceNode parent = tree;
			for (int i = 0; i < (nodePaths.length - 1); i++) {
				String nodePath = nodePaths[i];

				// Add the child directory
				ClassPathHttpResourceNode child = addChild(parent, nodePath,
						true);

				// Child becomes parent for next iteration
				parent = child;
			}

			// Create the leaf node being added
			addChild(parent, nodePaths[nodePaths.length - 1],
					jarEntry.isDirectory());
		}
	}

	/**
	 * Adds a child to parent.
	 * 
	 * @param parent
	 *            Parent.
	 * @param nodePath
	 *            Node path for child.
	 * @param isDirectory
	 *            Indicates if directory.
	 * @return Added child {@link ClassPathHttpResourceNode}.
	 */
	private static ClassPathHttpResourceNode addChild(
			ClassPathHttpResourceNode parent, String nodePath,
			boolean isDirectory) {

		// Ensure have path for child
		if ((nodePath == null) || (nodePath.length() == 0)) {
			return parent; // no node path so return parent
		}

		// Determine if node already added
		ClassPathHttpResourceNode child = parent.getChild(nodePath);
		if (child != null) {
			return child; // child already added, so return it
		}

		// No child so create the child
		String resourcePath = parent.resourcePath + nodePath
				+ (isDirectory ? "/" : "");
		String classPath = parent.classPath
				+ ("".equals(parent.classPath) ? "" : "/") + nodePath;
		child = new ClassPathHttpResourceNode(nodePath, resourcePath,
				classPath, isDirectory);

		// Add child to the parent
		ClassPathHttpResourceNode[] children = new ClassPathHttpResourceNode[parent.children.length + 1];
		System.arraycopy(parent.children, 0, children, 0,
				parent.children.length);
		children[children.length - 1] = child;
		parent.children = children;

		// Sort the children
		Arrays.sort(parent.children,
				new Comparator<ClassPathHttpResourceNode>() {
					@Override
					public int compare(ClassPathHttpResourceNode a,
							ClassPathHttpResourceNode b) {
						return String.CASE_INSENSITIVE_ORDER.compare(
								a.nodePath, b.nodePath);
					}
				});

		// Return the child
		return child;
	}

	/**
	 * Path of this node relative to its parent.
	 */
	private final String nodePath;

	/**
	 * {@link HttpResource} path to this node.
	 */
	private final String resourcePath;

	/**
	 * Class path to this node.
	 */
	private final String classPath;

	/**
	 * Indicates if this node is a directory (otherwise a file).
	 */
	private final boolean isDirectory;

	/**
	 * Children of this {@link ClassPathHttpResourceNode}.
	 */
	private ClassPathHttpResourceNode[] children = new ClassPathHttpResourceNode[0];

	/**
	 * Initiate.
	 * 
	 * @param nodePath
	 *            Path of this node relative to its parent.
	 * @param resourcePath
	 *            {@link HttpResource} path to this node.
	 * @param classPath
	 *            Class path to this node.
	 * @param isDirectory
	 *            <code>true</code> if this node is a directory (otherwise a
	 *            file).
	 */
	public ClassPathHttpResourceNode(String nodePath, String resourcePath,
			String classPath, boolean isDirectory) {
		this.nodePath = nodePath;
		this.resourcePath = resourcePath;
		this.classPath = classPath;
		this.isDirectory = isDirectory;
	}

	/**
	 * Obtains the path of this {@link ClassPathHttpResourceNode} from its
	 * parent.
	 * 
	 * @return Path of this {@link ClassPathHttpResourceNode} from its parent.
	 */
	public String getNodePath() {
		return this.nodePath;
	}

	/**
	 * Obtains the {@link HttpResource} path.
	 * 
	 * @return {@link HttpResource} path.
	 */
	public String getResourcePath() {
		return this.resourcePath;
	}

	/**
	 * Obtains the class path for this {@link HttpResource}.
	 * 
	 * @return Class path.
	 */
	public String getClassPath() {
		return this.classPath;
	}

	/**
	 * Indicates if this is a directory.
	 * 
	 * @return <code>true</code> if this is a directory.
	 */
	public boolean isDirectory() {
		return this.isDirectory;
	}

	/**
	 * Obtains the child {@link ClassPathHttpResourceNode} instances.
	 * 
	 * @return Child {@link ClassPathHttpResourceNode} instances.
	 */
	public ClassPathHttpResourceNode[] getChildren() {
		return this.children;
	}

	/**
	 * Obtains the child {@link ClassPathHttpResourceNode} by the name.
	 * 
	 * @param childNodePath
	 *            Child node path.
	 * @return Child {@link ClassPathHttpResourceNode} or <code>null</code> if
	 *         no child by node path.
	 */
	public ClassPathHttpResourceNode getChild(String childNodePath) {

		// Find the corresponding child
		for (ClassPathHttpResourceNode child : this.children) {
			if (child.nodePath.equals(childNodePath)) {
				return child; // found corresponding child
			}
		}

		// As here, no child by node path
		return null;
	}

}