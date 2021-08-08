/*-
 * #%L
 * Web resources
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.resource.classpath;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.officefloor.web.resource.HttpResource;

/**
 * Node on the class path that may be retrieved as a {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathNode {

	/**
	 * Creates the {@link ClassPathNode} tree for available {@link HttpResource}
	 * instances.
	 * 
	 * @param classpathPrefix The prefix on the class path to locate resources for
	 *                        the tree. <code>null</code> for the entire class path.
	 * @return {@link ClassPathNode}.
	 */
	public static ClassPathNode createClassPathResourceTree(String classpathPrefix) {

		// Obtain the class path entries
		String classPath = System.getProperty("java.class.path");
		String[] classPathEntries = classPath.split(File.pathSeparator);

		// Create the root class path node for loading (always directory)
		ClassPathNode root = new ClassPathNode(null, "/", (classpathPrefix == null ? "" : classpathPrefix), true);

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
	 * Loads the {@link ClassPathNode} directory instances.
	 * 
	 * @param parent    Parent {@link ClassPathNode}.
	 * @param directory Parent directory.
	 */
	private static void loadDirectoryEntries(ClassPathNode parent, File directory) {

		// Iterate over children of directory
		for (File file : directory.listFiles()) {

			// Determine if a directory
			boolean isDirectory = file.isDirectory();

			// Always add the node
			ClassPathNode child = addChild(parent, file.getName(), isDirectory);

			// Add children of directory
			if (isDirectory) {
				loadDirectoryEntries(child, file);
			}
		}
	}

	/**
	 * Loads the {@link ClassPathNode} instances for the JAR file.
	 * 
	 * @param tree            Tree root {@link ClassPathNode}.
	 * @param jarFile         {@link JarFile}.
	 * @param classpathPrefix Class path prefix.
	 */
	private static void loadJarEntries(ClassPathNode tree, JarFile jarFile, String classpathPrefix) {

		// Ensure have class path prefix
		classpathPrefix = (classpathPrefix == null ? "" : classpathPrefix);

		// Iterate over the jar entries
		for (Enumeration<JarEntry> enumeration = jarFile.entries(); enumeration.hasMoreElements();) {
			JarEntry jarEntry = enumeration.nextElement();

			// Obtain the entry name
			String jarEntryName = jarEntry.getName();
			if (!jarEntryName.startsWith(classpathPrefix)) {
				continue; // not to be made available as resource
			}

			// Strip off class path prefix to find resource path
			String resourcePath = jarEntryName.substring(classpathPrefix.length());

			// Ignore root directory
			if ("/".equals(resourcePath)) {
				continue; // tree root directory, so not include
			}

			// Split resource path into child node paths for loading
			String[] nodePaths = resourcePath.split("/");

			// Add the parent directories (-1 to ignore leaf node)
			ClassPathNode parent = tree;
			for (int i = 0; i < (nodePaths.length - 1); i++) {
				String nodePath = nodePaths[i];

				// Add the child directory
				ClassPathNode child = addChild(parent, nodePath, true);

				// Child becomes parent for next iteration
				parent = child;
			}

			// Create the leaf node being added
			addChild(parent, nodePaths[nodePaths.length - 1], jarEntry.isDirectory());
		}
	}

	/**
	 * Adds a child to parent.
	 * 
	 * @param parent      Parent.
	 * @param nodePath    Node path for child.
	 * @param isDirectory Indicates if directory.
	 * @return Added child {@link ClassPathNode}.
	 */
	private static ClassPathNode addChild(ClassPathNode parent, String nodePath, boolean isDirectory) {

		// Ensure have path for child
		if ((nodePath == null) || (nodePath.length() == 0)) {
			return parent; // no node path so return parent
		}

		// Determine if node already added
		ClassPathNode child = parent.getChild(nodePath);
		if (child != null) {
			return child; // child already added, so return it
		}

		// No child so create the child
		String resourcePath = parent.resourcePath + nodePath + (isDirectory ? "/" : "");
		String classPath = parent.classPath + ("".equals(parent.classPath) ? "" : "/") + nodePath;
		child = new ClassPathNode(nodePath, resourcePath, classPath, isDirectory);

		// Add child to the parent
		ClassPathNode[] children = new ClassPathNode[parent.children.length + 1];
		System.arraycopy(parent.children, 0, children, 0, parent.children.length);
		children[children.length - 1] = child;
		parent.children = children;

		// Sort the children
		Arrays.sort(parent.children, new Comparator<ClassPathNode>() {
			@Override
			public int compare(ClassPathNode a, ClassPathNode b) {
				return String.CASE_INSENSITIVE_ORDER.compare(a.nodePath, b.nodePath);
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
	 * Children of this {@link ClassPathNode}.
	 */
	private ClassPathNode[] children = new ClassPathNode[0];

	/**
	 * Initiate.
	 * 
	 * @param nodePath     Path of this node relative to its parent.
	 * @param resourcePath {@link HttpResource} path to this node.
	 * @param classPath    Class path to this node.
	 * @param isDirectory  <code>true</code> if this node is a directory (otherwise
	 *                     a file).
	 */
	public ClassPathNode(String nodePath, String resourcePath, String classPath, boolean isDirectory) {
		this.nodePath = nodePath;
		this.resourcePath = resourcePath;
		this.classPath = classPath;
		this.isDirectory = isDirectory;
	}

	/**
	 * Obtains the path of this {@link ClassPathNode} from its parent.
	 * 
	 * @return Path of this {@link ClassPathNode} from its parent.
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
	 * Obtains the child {@link ClassPathNode} instances.
	 * 
	 * @return Child {@link ClassPathNode} instances.
	 */
	public ClassPathNode[] getChildren() {
		return this.children;
	}

	/**
	 * Obtains the child {@link ClassPathNode} by the name.
	 * 
	 * @param childNodePath Child node path.
	 * @return Child {@link ClassPathNode} or <code>null</code> if no child by node
	 *         path.
	 */
	public ClassPathNode getChild(String childNodePath) {

		// Find the corresponding child
		for (ClassPathNode child : this.children) {
			if (child.nodePath.equals(childNodePath)) {
				return child; // found corresponding child
			}
		}

		// As here, no child by node path
		return null;
	}

}
