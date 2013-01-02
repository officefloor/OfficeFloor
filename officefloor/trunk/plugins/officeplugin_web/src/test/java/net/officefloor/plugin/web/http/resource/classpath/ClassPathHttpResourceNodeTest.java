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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.resource.AbstractHttpResourceFactoryTestCase;

/**
 * Tests the {@link ClassPathHttpResourceNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathHttpResourceNodeTest extends OfficeFrameTestCase {

	/**
	 * Original <code>java.class.path</code> property value to reinstate after
	 * test.
	 */
	private String originalJavaClassPath;

	@Override
	protected void setUp() throws Exception {
		// Record original java.class.path to reinstate after test
		this.originalJavaClassPath = System.getProperty("java.class.path");
	}

	@Override
	protected void tearDown() throws Exception {
		// Reinstate the java.class.path
		System.setProperty("java.class.path", this.originalJavaClassPath);
	}

	/**
	 * Ensure correctly loads a directory tree.
	 */
	public void testDirectoryTree() throws IOException {

		// Specify java.class.path for isolation of testing
		File resourceDirectory = new File(
				AbstractHttpResourceFactoryTestCase.getTestResourceDirectory(),
				"directory");
		System.setProperty("java.class.path",
				resourceDirectory.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathHttpResourceNode tree = ClassPathHttpResourceNode
				.createClassPathResourceTree(null);

		// Validate the tree (checking directory, sub directory, file)
		assertNode(tree, "/", "", true, "/index.html", "/sub_directory/");
		ClassPathHttpResourceNode directory = tree.getChild("sub_directory");
		assertNode(directory, "/sub_directory/", "sub_directory", true,
				"/sub_directory/index.html");
		ClassPathHttpResourceNode file = tree.getChild("index.html");
		assertNode(file, "/index.html", "index.html", false);
	}

	/**
	 * Ensure correctly loads a directory tree with class path prefix.
	 */
	public void testDirectoryTreeWithClassPathPrefix() throws IOException {

		// Specify java.class.path for isolation of testing
		File resourceDirectory = AbstractHttpResourceFactoryTestCase
				.getTestResourceDirectory();
		System.setProperty("java.class.path",
				resourceDirectory.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathHttpResourceNode tree = ClassPathHttpResourceNode
				.createClassPathResourceTree("directory");

		// Validate the tree (checking directory, file)
		assertNode(tree, "/", "directory", true, "/index.html",
				"/sub_directory/");
		ClassPathHttpResourceNode file = tree.getChild("index.html");
		assertNode(file, "/index.html", "directory/index.html", false);
	}

	/**
	 * Ensure correctly loads directory not containing the class path prefix
	 * directory.
	 */
	public void testDirectoryTreeNotContainingClassPathPrefix()
			throws IOException {

		final String CLASS_PATH_PREFIX = "not_exist";

		// Ensure test valid by class path prefix directory not existing
		File resourceDirectory = AbstractHttpResourceFactoryTestCase
				.getTestResourceDirectory();
		assertFalse("Invalid test as class path prefix directory exists",
				new File(resourceDirectory, CLASS_PATH_PREFIX).exists());

		// Specify java.class.path for isolation of testing
		System.setProperty("java.class.path",
				resourceDirectory.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathHttpResourceNode tree = ClassPathHttpResourceNode
				.createClassPathResourceTree(CLASS_PATH_PREFIX);

		// Validate the tree (checking directory, file)
		assertNode(tree, "/", CLASS_PATH_PREFIX, true);
	}

	/**
	 * Ensure correctly loads a JAR tree.
	 */
	public void testJarTree() throws IOException {

		// Specify java.class.path for isolation of testing
		File jarFile = new File(
				AbstractHttpResourceFactoryTestCase.getTestResourceDirectory(),
				"test.jar");
		System.setProperty("java.class.path", jarFile.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathHttpResourceNode tree = ClassPathHttpResourceNode
				.createClassPathResourceTree(null);

		// Validate the tree (checking directory, sub directory, file)
		assertNode(tree, "/", "", true, "/directory/", "/empty/",
				"/index.html", "/META-INF/");
		ClassPathHttpResourceNode directory = tree.getChild("directory");
		assertNode(directory, "/directory/", "directory", true,
				"/directory/index.html", "/directory/sub_directory/");
		ClassPathHttpResourceNode file = tree.getChild("index.html");
		assertNode(file, "/index.html", "index.html", false);
	}

	/**
	 * Ensure correctly loads a JAR tree with class path prefix.
	 */
	public void testJarTreeWithClassPathPrefix() throws IOException {

		// Specify java.class.path for isolation of testing
		File jarFile = new File(
				AbstractHttpResourceFactoryTestCase.getTestResourceDirectory(),
				"test.jar");
		System.setProperty("java.class.path", jarFile.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathHttpResourceNode tree = ClassPathHttpResourceNode
				.createClassPathResourceTree("directory");

		// Validate the tree (checking directory, file)
		assertNode(tree, "/", "directory", true, "/index.html",
				"/sub_directory/");
		ClassPathHttpResourceNode file = tree.getChild("index.html");
		assertNode(file, "/index.html", "directory/index.html", false);
	}

	/**
	 * Ensure correctly loads JAR not containing the class path prefix
	 * directory.
	 */
	public void testJarTreeNotContainingClassPathPrefix() throws IOException {

		// Specify java.class.path for isolation of testing
		File jarFile = new File(
				AbstractHttpResourceFactoryTestCase.getTestResourceDirectory(),
				"test.jar");
		System.setProperty("java.class.path", jarFile.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathHttpResourceNode tree = ClassPathHttpResourceNode
				.createClassPathResourceTree("not_exist");

		// Validate the tree (no children as not exist)
		assertNode(tree, "/", "not_exist", true);
	}

	/**
	 * Validates the {@link ClassPathHttpResourceNode}.
	 * 
	 * @param node
	 *            {@link ClassPathHttpResourceNode} to be validated.
	 * @param expectedResourcePath
	 *            Expected resource path.
	 * @param expectedClassPath
	 *            Expected class path.
	 * @param isExpectedToBeDirectory
	 *            <code>true</code> if expecting to be directory.
	 * @param expectedChildren
	 *            Resource paths of the expected children.
	 */
	private static void assertNode(ClassPathHttpResourceNode node,
			String expectedResourcePath, String expectedClassPath,
			boolean isExpectedToBeDirectory, String... expectedChildren) {

		// Validate details of the node
		assertEquals("Incorrect resource path", expectedResourcePath,
				node.getResourcePath());
		assertEquals("Incorrect class path", expectedClassPath,
				node.getClassPath());
		assertEquals("Incorrect indication if directory",
				isExpectedToBeDirectory, node.isDirectory());

		// Obtain the children (ignoring SVN entries)
		ClassPathHttpResourceNode[] children = node.getChildren();
		List<ClassPathHttpResourceNode> svnCleanup = new ArrayList<ClassPathHttpResourceNode>(
				Arrays.asList(children));
		for (Iterator<ClassPathHttpResourceNode> iterator = svnCleanup
				.iterator(); iterator.hasNext();) {
			ClassPathHttpResourceNode child = iterator.next();
			if (child.getResourcePath().toLowerCase().contains(".svn")) {
				iterator.remove(); // remove SVN entry
			}
		}
		children = svnCleanup.toArray(new ClassPathHttpResourceNode[svnCleanup
				.size()]);

		// Validate children of the node
		assertEquals("Incorrect number of children", expectedChildren.length,
				children.length);
		for (int i = 0; i < expectedChildren.length; i++) {
			ClassPathHttpResourceNode child = children[i];
			String expectedChildResourcePath = expectedChildren[i];
			assertEquals("Incorrect child " + i, expectedChildResourcePath,
					child.getResourcePath());
		}
	}

}